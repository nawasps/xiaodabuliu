package com.campus.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.entity.Notice;

import java.util.List;

public interface NoticeService {

    Notice createNotice(Notice notice);

    Notice updateNotice(Notice notice);

    void deleteNotice(Long id);

    Notice getNoticeById(Long id);

    Page<Notice> getNoticeList(Integer pageNum, Integer pageSize);

    List<Notice> getActiveNotices();
}
