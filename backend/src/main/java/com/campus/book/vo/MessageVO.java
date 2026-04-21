package com.campus.book.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private List<String> images;
    private Long fromUserId;
    private String fromUsername;
    private Long toUserId;
    private String toUsername;
    private Boolean isRead;
    private String relatedType;
    private Long relatedId;
    private LocalDateTime createTime;
}
