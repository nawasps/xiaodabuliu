package com.campus.book.service;

import com.campus.book.entity.Favorite;
import com.campus.book.entity.Book;
import com.campus.book.vo.BookVO;

import java.util.List;

public interface FavoriteService {

    void addFavorite(Long bookId);

    void removeFavorite(Long bookId);

    List<BookVO> getFavoriteList();

    boolean isFavorite(Long bookId);
}
