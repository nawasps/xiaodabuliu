package com.campus.book.scheduler;

import com.campus.book.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 * * * * ?")
    public void handleExpiredOrders() {
        orderService.handleExpiredOrders();
    }
}