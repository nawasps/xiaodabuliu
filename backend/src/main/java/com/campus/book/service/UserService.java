package com.campus.book.service;

import com.campus.book.dto.UserLoginDTO;
import com.campus.book.dto.UserRegisterDTO;
import com.campus.book.entity.User;
import com.campus.book.vo.UserVO;

public interface UserService {

    User register(UserRegisterDTO dto);

    String login(UserLoginDTO dto);

    UserVO getUserInfo(Long userId);

    UserVO getCurrentUserInfo();

    void updateUserInfo(User user);

    void updatePassword(String oldPassword, String newPassword);

    void resetPassword(String phone,  String verifyCode);

    void sendVerifyCode(String phone);

    UserVO getUserById(Long userId);

    boolean isUsernameExists(String username);

    boolean isPhoneExists(String phone);

    boolean isEmailExists(String email);

    void updatePhone(String phone, String verifyCode);
}
