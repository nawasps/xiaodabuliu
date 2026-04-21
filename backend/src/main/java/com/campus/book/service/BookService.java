package com.campus.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.dto.BookDTO;
import com.campus.book.entity.Book;
import com.campus.book.vo.BookVO;

import java.util.List;
import java.util.Map;

public interface BookService {

    BookVO publish(BookDTO dto);

    BookVO updateBook(Long id, BookDTO dto);

    void deleteBook(Long id);

    BookVO getBookById(Long id);

    Page<BookVO> getBookList(Integer pageNum, Integer pageSize, Long categoryId, String condition, 
                              String keyword, Integer minPrice, Integer maxPrice, String sortBy, String sortOrder);

    Page<BookVO> getHomeBooks();

    Page<BookVO> getFeedBooks(Long userId);

    List<BookVO> getHotBooks();

    List<BookVO> getLatestBooks();

    void updateBookStatus(Long id, String status);

    Page<Book> getBookPage(Page<Book> page, String status, String keyword);

    Page<BookVO> getMyBooks(Integer pageNum, Integer pageSize);

    void incrementViewCount(Long id);

    Map<String, Object> getHomeData();
}
