package com.campus.book.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.dto.OrderCreateDTO;
import com.campus.book.service.OrderService;
import com.campus.book.service.PaymentService;
import com.campus.book.util.LogUtils;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<OrderVO> createOrder(@Validated @RequestBody OrderCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("创建订单请求: userId={}, 商品数量={}", userId, dto.getBookIds().size());
        
        OrderVO order = orderService.createOrder(dto);
        
        LogUtils.logBusiness(logger, userId, "创建", "订单", 
            String.format("订单号: %s, 金额: %.2f", order.getOrderNo(), order.getTotalAmount()));
        logger.info("订单创建成功: orderId={}, orderNo={}, amount={}", 
            order.getId(), order.getOrderNo(), order.getTotalAmount());
        
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @GetMapping("/list")
    public Result<Page<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.getOrderList(pageNum, pageSize, status));
    }

    @PutMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("支付订单请求: userId={}, orderId={}", userId, id);
        
        orderService.payOrder(id);
        
        LogUtils.logBusiness(logger, userId, "支付", "订单", 
            String.format("订单ID: %d", id));
        logger.info("订单支付成功: orderId={}, userId={}", id, userId);
        
        return Result.success();
    }

    @GetMapping("/{id}/pay/alipay/form")
    public Result<Map<String, String>> createAlipayPayForm(@PathVariable Long id) {
        String payForm = paymentService.createAlipayPagePayForm(id);
        Map<String, String> data = new HashMap<>();
        data.put("provider", "ALIPAY_SANDBOX");
        data.put("payForm", payForm);
        return Result.success(data);
    }

    @PutMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String trackingNo = params.get("trackingNo");
        orderService.shipOrder(id, trackingNo);
        return Result.success();
    }
    @GetMapping("/sold")
    public Result<List<OrderVO>> getSoldOrders() {
        return Result.success(orderService.getOrdersBySeller(SecurityUtils.getCurrentUserId()));
    }
    @PutMapping("/{id}/receive")
    public Result<Void> receiveOrder(@PathVariable Long id) {
        orderService.receiveOrder(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        String reason = params.getOrDefault("reason", "用户取消");
        
        logger.info("取消订单请求: userId={}, orderId={}, reason={}", userId, id, reason);
        
        orderService.cancelOrder(id, reason);
        
        LogUtils.logBusiness(logger, userId, "取消", "订单", 
            String.format("订单ID: %d, 原因: %s", id, reason));
        logger.info("订单取消成功: orderId={}, userId={}", id, userId);
        
        return Result.success();
    }

    @GetMapping("/seller")
    public Result<List<OrderVO>> getOrdersBySeller() {
        return Result.success(orderService.getOrdersBySeller(SecurityUtils.getCurrentUserId()));
    }

}
