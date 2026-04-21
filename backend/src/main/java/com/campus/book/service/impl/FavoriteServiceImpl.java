package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.book.common.constants.Constants;
import com.campus.book.entity.Book;
import com.campus.book.entity.Favorite;
import com.campus.book.mapper.BookMapper;
import com.campus.book.mapper.FavoriteMapper;
import com.campus.book.service.FavoriteService;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.BookVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void addFavorite(Long bookId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("书籍不存在");
        }
        if (book.getUserId().equals(userId)) {
            throw new RuntimeException("不能收藏自己发布的书籍");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(bookId);
        try {
            favoriteMapper.insert(favorite);
            book.setFavoriteCount(book.getFavoriteCount() + 1);
            bookMapper.updateById(book);
        } catch (Exception e) {
        }
    }
    @Override
    @Transactional
    public void removeFavorite(Long bookId) {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        favoriteMapper.delete(wrapper);

        Book book = bookMapper.selectById(bookId);
        if (book != null && book.getFavoriteCount() > 0) {
            book.setFavoriteCount(book.getFavoriteCount() - 1);
            bookMapper.updateById(book);
        }
    }

    @Override
    public List<BookVO> getFavoriteList() {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        List<BookVO> books = new ArrayList<>();
        for (Favorite fav : favorites) {
            Book book = bookMapper.selectById(fav.getBookId());
            if (book != null && Constants.STATUS_ON_SALE.equals(book.getStatus())) {
                BookVO vo = new BookVO();
                BeanUtils.copyProperties(book, vo);
                try {
                    vo.setImages(objectMapper.readValue(book.getImages(), new TypeReference<List<String>>() {}));
                } catch (Exception e) {
                    vo.setImages(new ArrayList<>());
                }
                vo.setIsFavorite(true);
                books.add(vo);
            }
        }
        return books;
    }

    @Override
    public boolean isFavorite(Long bookId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
