package com.campus.book.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String nickname;
    private String role;
    private Integer creditScore;
    private String status;
    private LocalDateTime createTime;
}
