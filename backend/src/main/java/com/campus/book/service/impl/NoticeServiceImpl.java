package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.entity.Message;
import com.campus.book.entity.Notice;
import com.campus.book.entity.User;
import com.campus.book.mapper.MessageMapper;
import com.campus.book.mapper.NoticeMapper;
import com.campus.book.mapper.UserMapper;
import com.campus.book.service.NoticeService;
import com.campus.book.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Notice createNotice(Notice notice) {
        notice.setCreateBy(SecurityUtils.getCurrentUserId());
        noticeMapper.insert(notice);

        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<>());
        for (User user : allUsers) {
            Message message = new Message();
            message.setType(Constants.MESSAGE_TYPE_SYSTEM);
            message.setTitle(notice.getTitle());
            message.setContent(notice.getContent());
            message.setToUserId(user.getId());
            message.setIsRead(false);
            messageMapper.insert(message);
        }

        return notice;
    }

    @Override
    public Notice updateNotice(Notice notice) {
        noticeMapper.updateById(notice);
        return notice;
    }

    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public Notice getNoticeById(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public Page<Notice> getNoticeList(Integer pageNum, Integer pageSize) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notice::getCreateTime);
        return noticeMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Notice> getActiveNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, "ACTIVE")
                .orderByDesc(Notice::getPriority)
                .orderByDesc(Notice::getCreateTime)
                .last("LIMIT 10");
        return noticeMapper.selectList(wrapper);
    }
}
