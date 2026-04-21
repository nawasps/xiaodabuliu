package com.campus.book.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 提供统一的日志记录方法
 */
public class LogUtils {

    /**
     * 获取Logger实例
     *
     * @param clazz 类
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取Logger实例
     *
     * @param name 名称
     * @return Logger实例
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 记录业务操作日志
     *
     * @param logger   日志记录器
     * @param userId   用户ID
     * @param action   操作类型
     * @param resource 资源类型
     * @param detail   详细信息
     */
    public static void logBusiness(Logger logger, Long userId, String action, String resource, String detail) {
        logger.info("[业务日志] 用户ID: {}, 操作: {}, 资源: {}, 详情: {}", userId, action, resource, detail);
    }

    /**
     * 记录访问日志
     *
     * @param logger 日志记录器
     * @param userId 用户ID
     * @param method HTTP方法
     * @param uri    请求URI
     * @param ip     IP地址
     */
    public static void logAccess(Logger logger, Long userId, String method, String uri, String ip) {
        logger.info("[访问日志] 用户ID: {}, 方法: {}, URI: {}, IP: {}", userId, method, uri, ip);
    }

    /**
     * 记录异常日志
     *
     * @param logger      日志记录器
     * @param message     错误消息
     * @param exception   异常对象
     */
    public static void logError(Logger logger, String message, Exception exception) {
        logger.error("[异常日志] {}, 异常信息: {}", message, exception.getMessage(), exception);
    }

    /**
     * 记录性能日志
     *
     * @param logger       日志记录器
     * @param methodName   方法名
     * @param duration     执行时间（毫秒）
     */
    public static void logPerformance(Logger logger, String methodName, long duration) {
        logger.info("[性能日志] 方法: {}, 执行时间: {}ms", methodName, duration);
    }
}
