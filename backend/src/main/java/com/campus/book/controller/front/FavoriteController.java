package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.service.FavoriteService;
import com.campus.book.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{bookId}")
    public Result<Void> addFavorite(@PathVariable Long bookId) {
        favoriteService.addFavorite(bookId);
        return Result.success();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> removeFavorite(@PathVariable Long bookId) {
        favoriteService.removeFavorite(bookId);
        return Result.success();
    }

    @GetMapping
    public Result<List<BookVO>> getFavoriteList() {
        return Result.success(favoriteService.getFavoriteList());
    }

    @GetMapping("/{bookId}/check")
    public Result<Boolean> isFavorite(@PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorite(bookId));
    }
}
