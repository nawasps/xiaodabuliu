package com.campus.book.common.constants;

public class Constants {
    public static final String USER_CACHE_KEY = "user:";
    public static final String BOOK_CACHE_KEY = "book:";
    public static final String CART_CACHE_KEY = "cart:";
    public static final String SEARCH_HISTORY_KEY = "search:history:";
    public static final String HOT_SEARCH_KEY = "hot:search";
    public static final String FEED_STREAM_KEY = "feed:stream:";

    public static final Integer ORDER_EXPIRE_TIME = 30 * 60;

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String STATUS_ON_SALE = "ON_SALE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_OFFLINE = "OFFLINE";

    public static final String ORDER_STATUS_PENDING_PAY = "PENDING_PAY";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_RECEIVED = "RECEIVED";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    public static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    public static final String MESSAGE_TYPE_ORDER = "ORDER";
    public static final String MESSAGE_TYPE_PRIVATE = "PRIVATE";

    public static final String REPORT_STATUS_PENDING = "PENDING";
    public static final String REPORT_STATUS_HANDLED = "HANDLED";

    public static final Integer CREDIT_SCORE_MAX = 100;
    public static final Integer CREDIT_SCORE_MIN = 0;
}
