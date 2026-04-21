package com.campus.book.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.dto.MessageDTO;
import com.campus.book.entity.Message;
import com.campus.book.service.MessageService;
import com.campus.book.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<Page<MessageVO>> getMessageList(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.getMessageList(type, pageNum, pageSize));
    }

    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        return Result.success(messageService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/readAll")
    public Result<Void> markAllAsRead() {
        messageService.markAllAsRead();
        return Result.success();
    }

    @PostMapping("/private")
    public Result<Message> sendPrivateMessage(@Validated @RequestBody MessageDTO dto) {
        return Result.success(messageService.sendMessage(dto));
    }

    @GetMapping("/private/{userId}")
    public Result<List<MessageVO>> getPrivateMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(messageService.getPrivateMessages(userId, pageNum, pageSize));
    }

    @GetMapping("/unreadByType")
    public Result<Map<String, Integer>> getUnreadCountByType() {
        return Result.success(messageService.getUnreadCountByType());
    }

    @PutMapping("/readByType/{type}")
    public Result<Void> markAsReadByType(@PathVariable String type) {
        messageService.markAsReadByType(type);
        return Result.success();
    }
}
