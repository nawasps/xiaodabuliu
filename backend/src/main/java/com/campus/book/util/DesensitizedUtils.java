package com.campus.book.util;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;

public class DesensitizedUtils {

    public static String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return "";
        }
        return DesensitizedUtil.mobilePhone(phone);
    }

    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return "";
        }
        return DesensitizedUtil.email(email);
    }

    public static String desensitizeUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return "";
        }
        if (username.length() <= 2) {
            return "**";
        }
        return username.charAt(0) + "**" + username.charAt(username.length() - 1);
    }

    public static String desensitizeRealName(String realName) {
        if (StrUtil.isBlank(realName)) {
            return "";
        }
        if (realName.length() == 1) {
            return "*";
        }
        if (realName.length() == 2) {
            return realName.charAt(0) + "*";
        }
        return realName.charAt(0) + "*" + realName.charAt(realName.length() - 1);
    }
}
