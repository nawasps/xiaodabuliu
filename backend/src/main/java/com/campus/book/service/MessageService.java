package com.campus.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.dto.MessageDTO;
import com.campus.book.entity.Message;
import com.campus.book.vo.MessageVO;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Message sendMessage(MessageDTO dto);

    Message sendSystemMessage(Long toUserId, String title, String content);

    Page<MessageVO> getMessageList(String type, Integer pageNum, Integer pageSize);

    Integer getUnreadCount();

    void markAsRead(Long messageId);

    void markAllAsRead();

    List<MessageVO> getPrivateMessages(Long toUserId, Integer pageNum, Integer pageSize);

    Map<String, Integer> getUnreadCountByType();

    void markAsReadByType(String type);
}
