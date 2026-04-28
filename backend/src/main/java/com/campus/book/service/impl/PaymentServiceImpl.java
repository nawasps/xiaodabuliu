package com.campus.book.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.book.common.constants.Constants;
import com.campus.book.config.AlipayProperties;
import com.campus.book.entity.Book;
import com.campus.book.entity.Message;
import com.campus.book.entity.Order;
import com.campus.book.entity.OrderItem;
import com.campus.book.mapper.BookMapper;
import com.campus.book.mapper.MessageMapper;
import com.campus.book.mapper.OrderMapper;
import com.campus.book.mapper.OrderItemMapper;
import com.campus.book.service.PaymentService;
import com.campus.book.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String CHARSET = "UTF-8";
    private static final String FORMAT = "json";
    private static final String SIGN_TYPE = "RSA2";

    private final AlipayProperties alipayProperties;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final BookMapper bookMapper;
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(AlipayProperties alipayProperties,
                              OrderMapper orderMapper,
                              OrderItemMapper orderItemMapper,
                              BookMapper bookMapper,
                              MessageMapper messageMapper,
                              ObjectMapper objectMapper) {
        this.alipayProperties = alipayProperties;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.bookMapper = bookMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createAlipayPagePayForm(Long orderId) {
        if (!alipayProperties.isEnabled()) {
            throw new RuntimeException("支付宝沙箱未启用，请先在配置中开启");
        }
        validateConfig();

        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (!Constants.ORDER_STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不支持发起支付");
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                FORMAT,
                CHARSET,
                alipayProperties.getAlipayPublicKey(),
                SIGN_TYPE
        );

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (StringUtils.hasText(alipayProperties.getReturnUrl())) {
            request.setReturnUrl(alipayProperties.getReturnUrl());
        }
        if (StringUtils.hasText(alipayProperties.getNotifyUrl())) {
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
        }

        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", order.getOrderNo());
        bizContent.put("total_amount", order.getTotalAmount().toPlainString());
        bizContent.put("subject", "校园二手书订单-" + order.getOrderNo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        try {
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("支付参数序列化失败");
        }

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
            if (!response.isSuccess() || !StringUtils.hasText(response.getBody())) {
                throw new RuntimeException("支付宝调用失败: " + response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝调用异常: " + e.getErrMsg());
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(alipayProperties.getGatewayUrl())
                || !StringUtils.hasText(alipayProperties.getAppId())
                || !StringUtils.hasText(alipayProperties.getPrivateKey())
                || !StringUtils.hasText(alipayProperties.getAlipayPublicKey())) {
            throw new RuntimeException("支付宝配置不完整，请先填写 appId/密钥/网关");
        }
    }

    @Override
    public boolean handleAlipayNotify(Map<String, String> params) {
        return handleAlipayCallback(params);
    }

    @Override
    public boolean handleAlipayReturn(Map<String, String> params) {
        if (handleAlipayCallback(params)) {
            return true;
        }

        if (params == null || params.isEmpty()) {
            return false;
        }

        String orderNo = params.get("out_trade_no");
        if (!StringUtils.hasText(orderNo)) {
            return false;
        }

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return false;
        }

        return Constants.ORDER_STATUS_PAID.equals(order.getStatus())
                || Constants.ORDER_STATUS_SHIPPED.equals(order.getStatus())
                || Constants.ORDER_STATUS_COMPLETED.equals(order.getStatus());
    }

    private boolean handleAlipayCallback(Map<String, String> params) {
        if (!alipayProperties.isEnabled()) {
            return false;
        }
        validateConfig();
        if (params == null || params.isEmpty()) {
            return false;
        }

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    CHARSET,
                    SIGN_TYPE
            );
            if (!signVerified) {
                return false;
            }
        } catch (AlipayApiException e) {
            return false;
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return false;
        }

        String orderNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        if (!StringUtils.hasText(orderNo) || !StringUtils.hasText(totalAmount)) {
            return false;
        }

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return false;
        }

        BigDecimal paidAmount;
        try {
            paidAmount = new BigDecimal(totalAmount);
        } catch (NumberFormatException e) {
            return false;
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(paidAmount) != 0) {
            return false;
        }

        if (Constants.ORDER_STATUS_PAID.equals(order.getStatus())
                || Constants.ORDER_STATUS_SHIPPED.equals(order.getStatus())
                || Constants.ORDER_STATUS_COMPLETED.equals(order.getStatus())) {
            return true;
        }
        if (!Constants.ORDER_STATUS_PENDING_PAY.equals(order.getStatus())) {
            return false;
        }

        order.setStatus(Constants.ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : orderItems) {
            Book book = bookMapper.selectById(item.getBookId());
            if (book != null) {
                book.setStatus(Constants.STATUS_RESERVED);
                bookMapper.updateById(book);
            }
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

        return true;
    }
}
