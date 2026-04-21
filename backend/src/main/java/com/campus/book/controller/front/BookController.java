package com.campus.book.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.dto.BookDTO;
import com.campus.book.service.BookService;
import com.campus.book.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @PostMapping
    public Result<BookVO> publish(@Validated @RequestBody BookDTO dto) {
        return Result.success(bookService.publish(dto));
    }

    @PutMapping("/{id}")
    public Result<BookVO> updateBook(@PathVariable Long id, @Validated @RequestBody BookDTO dto) {
        return Result.success(bookService.updateBook(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<BookVO> getBookById(@PathVariable Long id) {
        bookService.incrementViewCount(id);
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping("/list")
    public Result<Page<BookVO>> getBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return Result.success(bookService.getBookList(pageNum, pageSize, categoryId, condition, keyword, 
                minPrice, maxPrice, sortBy, sortOrder));
    }

    @GetMapping("/home")
    public Result<Map<String, Object>> getHomeData() {
        return Result.success(bookService.getHomeData());
    }

    @GetMapping("/feed")
    public Result<Page<BookVO>> getFeedBooks(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bookService.getFeedBooks(userId));
    }

    @GetMapping("/hot")
    public Result<Object> getHotBooks() {
        return Result.success(bookService.getHotBooks());
    }

    @GetMapping("/latest")
    public Result<Object> getLatestBooks() {
        return Result.success(bookService.getLatestBooks());
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateBookStatus(@PathVariable Long id, @RequestParam String status) {
        bookService.updateBookStatus(id, status);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<BookVO>> getMyBooks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bookService.getMyBooks(pageNum, pageSize));
    }
}
