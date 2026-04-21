package com.campus.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.dto.OrderCreateDTO;
import com.campus.book.entity.Order;
import com.campus.book.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO createOrder(OrderCreateDTO dto);

    List<OrderVO> getSoldOrders();

    OrderVO getOrderById(Long id);

    Page<OrderVO> getOrderList(Integer pageNum, Integer pageSize, String status);

    void payOrder(Long orderId);

    void shipOrder(Long orderId, String trackingNo);

    void receiveOrder(Long orderId);

    void cancelOrder(Long orderId, String reason);

    List<OrderVO> getOrdersBySeller(Long userId);

    void handleExpiredOrders();
}
