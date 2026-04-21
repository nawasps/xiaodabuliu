package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.dto.MessageDTO;
import com.campus.book.entity.Message;
import com.campus.book.entity.User;
import com.campus.book.mapper.MessageMapper;
import com.campus.book.mapper.UserMapper;
import com.campus.book.service.MessageService;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.MessageVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public Message sendMessage(MessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        Message message = new Message();
        message.setType(Constants.MESSAGE_TYPE_PRIVATE);
        message.setContent(dto.getContent());
        message.setFromUserId(userId);
        message.setFromUsername(user.getUsername());
        message.setToUserId(dto.getToUserId());
        message.setIsRead(false);

        try {
            message.setImages(objectMapper.writeValueAsString(dto.getImages()));
        } catch (JsonProcessingException e) {
            message.setImages("[]");
        }

        messageMapper.insert(message);
        pushRealtimeMessage(message);
        return message;
    }

    @Override
    public Message sendSystemMessage(Long toUserId, String title, String content) {
        Message message = new Message();
        message.setType(Constants.MESSAGE_TYPE_SYSTEM);
        message.setTitle(title);
        message.setContent(content);
        message.setToUserId(toUserId);
        message.setIsRead(false);
        messageMapper.insert(message);
        pushRealtimeMessage(message);
        return message;
    }

    @Override
    public Page<MessageVO> getMessageList(String type, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        Long userId = SecurityUtils.getCurrentUserId();
        wrapper.eq(Message::getToUserId, userId);

        if (StringUtils.hasText(type)) {
            wrapper.eq(Message::getType, type);
        }

        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);

        Page<MessageVO> voPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Integer getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId).eq(Message::getIsRead, false);
        return messageMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null && message.getToUserId().equals(SecurityUtils.getCurrentUserId())) {
            message.setIsRead(true);
            messageMapper.updateById(message);
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Message> unreadMessages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getIsRead, false));

        for (Message message : unreadMessages) {
            message.setIsRead(true);
            messageMapper.updateById(message);
        }
    }

    @Override
    public List<MessageVO> getPrivateMessages(Long toUserId, Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getFromUserId, userId).eq(Message::getToUserId, toUserId)
                .or()
                .eq(Message::getFromUserId, toUserId).eq(Message::getToUserId, userId));
        wrapper.orderByAsc(Message::getCreateTime);
        List<Message> messages = messageMapper.selectList(wrapper);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getUnreadCountByType() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Integer> result = new HashMap<>();
        result.put("SYSTEM", messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getType, Constants.MESSAGE_TYPE_SYSTEM)
                        .eq(Message::getIsRead, false)).intValue());
        result.put("ORDER", messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getType, Constants.MESSAGE_TYPE_ORDER)
                        .eq(Message::getIsRead, false)).intValue());
        result.put("PRIVATE", messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getType, Constants.MESSAGE_TYPE_PRIVATE)
                        .eq(Message::getIsRead, false)).intValue());
        return result;
    }

    @Override
    public void markAsReadByType(String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        messageMapper.update(null,
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getType, type)
                        .eq(Message::getIsRead, false)
                        .set(Message::getIsRead, true));
    }
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        try {
            vo.setImages(objectMapper.readValue(message.getImages(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            vo.setImages(new ArrayList<>());
        }
        return vo;
    }

    private void pushRealtimeMessage(Message message) {
        if (simpMessagingTemplate == null || message == null || message.getToUserId() == null) {
            return;
        }
        User toUser = userMapper.selectById(message.getToUserId());
        if (toUser == null || !StringUtils.hasText(toUser.getUsername())) {
            return;
        }
        MessageVO messageVO = convertToVO(message);
        simpMessagingTemplate.convertAndSendToUser(toUser.getUsername(), "/queue/message", messageVO);
        if (Constants.MESSAGE_TYPE_PRIVATE.equals(message.getType())) {
            simpMessagingTemplate.convertAndSendToUser(toUser.getUsername(), "/queue/chat", messageVO);
        }
    }
}
