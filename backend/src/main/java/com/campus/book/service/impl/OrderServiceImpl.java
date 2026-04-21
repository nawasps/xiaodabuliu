package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.dto.OrderCreateDTO;
import com.campus.book.entity.*;
import com.campus.book.mapper.*;
import com.campus.book.service.OrderService;
import com.campus.book.util.LogUtils;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.OrderItemVO;
import com.campus.book.vo.OrderVO;
import com.campus.book.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @Transactional
    @Override
    public OrderVO createOrder(OrderCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("开始创建订单: userId={}, 商品数量={}", userId, dto.getBookIds().size());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> sellerIds = new ArrayList<>();

        for (Long bookId : dto.getBookIds()) {
            Book book = bookMapper.selectById(bookId);
            if (book == null) {
                logger.warn("创建订单失败: 商品不存在 - bookId={}", bookId);
                throw new RuntimeException("商品不存在: " + bookId);
            }
            if (!Constants.STATUS_ON_SALE.equals(book.getStatus())) {
                logger.warn("创建订单失败: 商品不可售 - bookId={}, title={}, status={}", 
                    bookId, book.getTitle(), book.getStatus());
                throw new RuntimeException("商品不可售: " + book.getTitle());
            }
            if (book.getUserId().equals(userId)) {
                logger.warn("创建订单失败: 不能购买自己的商品 - bookId={}, userId={}", bookId, userId);
                throw new RuntimeException("不能购买自己的商品");
            }

            OrderItem item = new OrderItem();
            item.setBookId(bookId);
            item.setBookTitle(book.getTitle());
            item.setPrice(book.getPrice());
            item.setQuantity(1);
            orderItems.add(item);

            totalAmount = totalAmount.add(book.getPrice());

            if (!sellerIds.contains(book.getUserId())) {
                sellerIds.add(book.getUserId());
            }

            Cart cartItem = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getBookId, bookId));
            if (cartItem != null) {
                cartMapper.deleteById(cartItem);
                logger.debug("从购物车移除商品: cartId={}, bookId={}", cartItem.getId(), bookId);
            }
        }

        for (Long sellerId : sellerIds) {
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setSellerId(sellerId);
            order.setTotalAmount(totalAmount.divide(BigDecimal.valueOf(sellerIds.size()), 2, BigDecimal.ROUND_HALF_UP));
            order.setStatus(Constants.ORDER_STATUS_PENDING_PAY);
            order.setReceiverName(dto.getReceiverName());
            order.setReceiverPhone(dto.getReceiverPhone());
            order.setReceiverAddress(dto.getReceiverAddress());
            orderMapper.insert(order);
            
            logger.info("订单创建成功: orderId={}, orderNo={}, sellerId={}, amount={}", 
                order.getId(), order.getOrderNo(), sellerId, order.getTotalAmount());

            for (OrderItem item : orderItems) {
                Book book = bookMapper.selectById(item.getBookId());
                if (book.getUserId().equals(sellerId)) {
                    item.setOrderId(order.getId());
                    orderItemMapper.insert(item);
                }
            }

            redisTemplate.opsForValue().set("order:expire:" + order.getId(), order.getId(),
                    Constants.ORDER_EXPIRE_TIME, TimeUnit.SECONDS);
            logger.debug("设置订单过期时间: orderId={}, expireTime={}s", order.getId(), Constants.ORDER_EXPIRE_TIME);

            Message message = new Message();
            message.setType(Constants.MESSAGE_TYPE_ORDER);
            message.setTitle("新订单提醒");
            message.setContent("您有一笔新订单，订单号：" + order.getOrderNo());
            message.setFromUserId(userId);
            message.setToUserId(sellerId);
            message.setRelatedType("ORDER");
            message.setRelatedId(order.getId());
            message.setIsRead(false);
            messageMapper.insert(message);
            logger.debug("发送订单通知消息: fromUserId={}, toUserId={}, orderId={}", userId, sellerId, order.getId());

            // 返回订单信息
            OrderVO vo = new OrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus());
            return vo;
        }

        return null;
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    public List<OrderVO> getSoldOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, userId)
                .orderByDesc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Page<OrderVO> getOrderList(Integer pageNum, Integer pageSize, String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        Long userId = SecurityUtils.getCurrentUserId();
        wrapper.eq(Order::getUserId, userId);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("支付订单: userId={}, orderId={}", userId, orderId);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.warn("支付失败: 订单不存在 - orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (!Constants.ORDER_STATUS_PENDING_PAY.equals(order.getStatus())) {
            logger.warn("支付失败: 订单状态不正确 - orderId={}, status={}", orderId, order.getStatus());
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus(Constants.ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        logger.info("订单支付成功: orderId={}, orderNo={}", orderId, order.getOrderNo());

        redisTemplate.delete("order:expire:" + orderId);

        for (Book book : getOrderBooks(orderId)) {
            book.setStatus(Constants.STATUS_RESERVED);
            bookMapper.updateById(book);
            logger.debug("更新商品状态为已预订: bookId={}, status={}", book.getId(), book.getStatus());
        }

        Message message = new Message();
        message.setType(Constants.MESSAGE_TYPE_ORDER);
        message.setTitle("支付成功");
        message.setContent("您的订单 " + order.getOrderNo() + " 已支付成功");
        message.setToUserId(order.getUserId());
        message.setRelatedType("ORDER");
        message.setRelatedId(order.getId());
        message.setIsRead(false);
        messageMapper.insert(message);
        
        LogUtils.logBusiness(logger, userId, "支付", "订单", 
            String.format("订单号: %s, 金额: %.2f", order.getOrderNo(), order.getTotalAmount()));
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, String trackingNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!Constants.ORDER_STATUS_PAID.equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus(Constants.ORDER_STATUS_SHIPPED);
        order.setShipTime(LocalDateTime.now());
        order.setTrackingNo(trackingNo);
        orderMapper.updateById(order);

        Message message = new Message();
        message.setType(Constants.MESSAGE_TYPE_ORDER);
        message.setTitle("商品已发货");
        message.setContent("您的订单 " + order.getOrderNo() + " 已发货，快递单号：" + trackingNo);
        message.setToUserId(order.getUserId());
        message.setRelatedType("ORDER");
        message.setRelatedId(order.getId());
        message.setIsRead(false);
        messageMapper.insert(message);
    }

    @Override
    @Transactional
    public void receiveOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!Constants.ORDER_STATUS_SHIPPED.equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus(Constants.ORDER_STATUS_COMPLETED);
        order.setReceiveTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);

        for (Book book : getOrderBooks(orderId)) {
            book.setStatus(Constants.STATUS_SOLD);
            bookMapper.updateById(book);
        }

        Message message = new Message();
        message.setType(Constants.MESSAGE_TYPE_ORDER);
        message.setTitle("交易完成");
        message.setContent("您的订单 " + order.getOrderNo() + " 已确认收货，交易完成");
        message.setToUserId(order.getUserId());
        message.setRelatedType("ORDER");
        message.setRelatedId(order.getId());
        message.setIsRead(false);
        messageMapper.insert(message);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!Constants.ORDER_STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new RuntimeException("只有待付款订单可以取消");
        }

        order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        redisTemplate.delete("order:expire:" + orderId);
    }

    @Override
    public List<OrderVO> getOrdersBySeller(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, userId)
                .orderByDesc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void handleExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, Constants.ORDER_STATUS_PENDING_PAY)
                        .lt(Order::getCreateTime, LocalDateTime.now().minusMinutes(30)));

        for (Order order : expiredOrders) {
            cancelOrder(order.getId(), "订单超时自动取消");
        }
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<Book> getOrderBooks(Long orderId) {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        return items.stream()
                .map(item -> bookMapper.selectById(item.getBookId()))
                .collect(Collectors.toList());
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        vo.setItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            Book book = bookMapper.selectById(item.getBookId());
            if (book != null) {
                try {
                    itemVO.setBookImage(objectMapper.readValue(book.getImages(), new TypeReference<List<String>>() {}).get(0));
                } catch (Exception e) {
                    itemVO.setBookImage("");
                }
            }
            return itemVO;
        }).collect(Collectors.toList()));

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            UserVO sellerVO = new UserVO();
            BeanUtils.copyProperties(seller, sellerVO);
            vo.setSeller(sellerVO);
        }

        return vo;
    }
}
