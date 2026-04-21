package com.campus.book.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.entity.Book;
import com.campus.book.mapper.BookMapper;
import com.campus.book.service.BookService;
import com.campus.book.vo.BookVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/book")
public class AdminBookController {

    private static final Set<String> AUDIT_STATUS_SET = new HashSet<>(Arrays.asList("ON_SALE", "OFFLINE"));

    @Autowired
    private BookService bookService;
    @Autowired
    private BookMapper bookMapper;

    @GetMapping("/list")
    public Result<Page<BookVO>> getBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        Page<Book> bookPage = bookService.getBookPage(page, status, keyword);

        Page<BookVO> voPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        voPage.setRecords(bookPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @GetMapping("/{id}")
    public Result<BookVO> getBookById(@PathVariable Long id) {
        BookVO book = bookService.getBookById(id);
        return Result.success(book);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateBookStatus(@PathVariable Long id, @RequestParam String status) {
        bookService.updateBookStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    private BookVO convertToVO(Book book) {
        if (book == null) return null;
        BookVO vo = new BookVO();
        BeanUtils.copyProperties(book, vo);
        return vo;
    }
    @PutMapping("/{id}/offline")
    public Result<Void> offlineBook(@PathVariable Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            return Result.error("商品不存在");
        }
        book.setStatus("OFFLINE");
        bookMapper.updateById(book);
        return Result.success();
    }
    @PutMapping("/{id}/audit")
    public Result<Void> auditBook(@PathVariable Long id, @RequestParam String status) {
        if (!AUDIT_STATUS_SET.contains(status)) {
            return Result.error("审核状态不合法，仅支持 ON_SALE 或 OFFLINE");
        }
        Book book = bookMapper.selectById(id);
        if (book == null) {
            return Result.error("商品不存在");
        }
        book.setStatus(status);
        bookMapper.updateById(book);
        return Result.success();
    }
}
