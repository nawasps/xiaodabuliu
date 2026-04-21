package com.campus.book.service;

import java.util.Map;

public interface PaymentService {
    String createAlipayPagePayForm(Long orderId);

    boolean handleAlipayNotify(Map<String, String> params);

    boolean handleAlipayReturn(Map<String, String> params);
}
