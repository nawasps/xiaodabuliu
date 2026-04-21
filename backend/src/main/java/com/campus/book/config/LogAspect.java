package com.campus.book.config;

import com.campus.book.util.LogUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志切面
 * 自动记录Controller层的请求和响应信息
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 环绕通知，记录所有Controller方法的执行
     */
    @Around("execution(* com.campus.book.controller..*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        String ip = request != null ? getClientIp(request) : "unknown";
        
        // 记录请求信息
        logger.info("========== 请求开始 ==========");
        logger.info("请求类名: {}", className);
        logger.info("请求方法: {}", methodName);
        logger.info("请求URI: {} {}", method, uri);
        logger.info("客户端IP: {}", ip);
        logger.info("请求参数: {}", Arrays.toString(joinPoint.getArgs()));
        
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录响应信息
            logger.info("响应结果: {}", result);
            logger.info("执行时间: {}ms", executionTime);
            logger.info("========== 请求结束 ==========");
            
            // 记录性能日志
            if (executionTime > 1000) {
                LogUtils.logPerformance(logger, className + "." + methodName, executionTime);
            }
            
            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("========== 请求异常 ==========");
            logger.error("执行时间: {}ms", executionTime);
            logger.error("异常信息: {}", throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
