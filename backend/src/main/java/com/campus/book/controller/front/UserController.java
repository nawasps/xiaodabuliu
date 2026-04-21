package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.dto.UserLoginDTO;
import com.campus.book.dto.UserRegisterDTO;
import com.campus.book.entity.User;
import com.campus.book.service.UserService;
import com.campus.book.util.LogUtils;
import com.campus.book.vo.UserVO;
import com.campus.book.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody UserRegisterDTO dto) {
        logger.info("用户注册请求: username={}", dto.getUsername());
        
        if (userService.isUsernameExists(dto.getUsername())) {
            logger.warn("注册失败: 用户名已存在 - {}", dto.getUsername());
            return Result.error(400, "用户名已存在");
        }
        if (userService.isPhoneExists(dto.getPhone())) {
            logger.warn("注册失败: 手机号已存在 - {}", dto.getPhone());
            return Result.error(400, "手机号已存在");
        }
        if (dto.getEmail() != null && userService.isEmailExists(dto.getEmail())) {
            logger.warn("注册失败: 邮箱已存在 - {}", dto.getEmail());
            return Result.error(400, "邮箱已存在");
        }
        
        User user = userService.register(dto);
        String token = userService.login(new UserLoginDTO() {{
            setUsername(dto.getUsername());
            setPassword(dto.getPassword());
        }});
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        
        LogUtils.logBusiness(logger, user.getId(), "注册", "用户", 
            String.format("用户名: %s, 手机号: %s", dto.getUsername(), dto.getPhone()));
        logger.info("用户注册成功: userId={}, username={}", user.getId(), dto.getUsername());
        
        return Result.success(data);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody UserLoginDTO dto) {
        try {
            logger.info("用户登录请求: username={}", dto.getUsername());
            String token = userService.login(dto);
            // login方法已设置SecurityContext，直接获取当前用户
            UserVO userInfo = userService.getCurrentUserInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", userInfo);
            
            LogUtils.logBusiness(logger, userInfo.getId(), "登录", "用户", 
                String.format("用户名: %s", dto.getUsername()));
            logger.info("用户登录成功: userId={}, username={}", userInfo.getId(), dto.getUsername());
            
            return Result.success(data);
        } catch (Exception e) {
            logger.warn("用户登录失败: username={}, 原因: {}", dto.getUsername(), e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getCurrentUserInfo());
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("更新用户信息请求: userId={}", userId);
        
        userService.updateUserInfo(user);
        
        LogUtils.logBusiness(logger, userId, "更新", "用户信息", 
            String.format("昵称: %s, 手机: %s", user.getNickname(), user.getPhone()));
        logger.info("用户信息更新成功: userId={}", userId);
        
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("修改密码请求: userId={}", userId);
        
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.updatePassword(oldPassword, newPassword);
        
        LogUtils.logBusiness(logger, userId, "修改", "密码", "用户修改密码");
        logger.info("密码修改成功: userId={}", userId);
        
        return Result.success();
    }

    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String verifyCode = params.get("verifyCode");
        userService.resetPassword(phone,  verifyCode);
        return Result.success();
    }
    @PutMapping("/phone")
    public Result<Void> updatePhone(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String verifyCode = params.get("verifyCode");
        userService.updatePhone(phone, verifyCode);
        return Result.success();
    }

    @PostMapping("/sendCode")
    public Result<Void> sendVerifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        userService.sendVerifyCode(phone);
        return Result.success();
    }



    @GetMapping("/exists/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.success(userService.isUsernameExists(username));
    }

    @GetMapping("/exists/phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        return Result.success(userService.isPhoneExists(phone));
    }

}
