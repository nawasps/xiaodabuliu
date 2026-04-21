package com.campus.book.util;

import com.campus.book.security.JWT.JwtTokenUtil;
import com.campus.book.util.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return (UserPrincipal) principal;
            } else if (principal instanceof UserDetails) {
                // 从 UserDetails 构造
                return new UserPrincipal(/* 从 authentication 获取用户信息 */);
            }
        }
        throw new RuntimeException("用户未登录");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUser().getRole());
    }

    public static boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }
}
