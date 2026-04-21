package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/history")
    public Result<List<String>> getSearchHistory() {
        return Result.success(searchService.getSearchHistory());
    }

    @DeleteMapping("/history")
    public Result<Void> clearSearchHistory() {
        searchService.clearSearchHistory();
        return Result.success();
    }

    @GetMapping("/hot")
    public Result<List<String>> getHotSearch() {
        return Result.success(searchService.getHotSearch());
    }

    @GetMapping("/recommend")
    public Result<List<String>> getSearchRecommendations(@RequestParam String keyword) {
        return Result.success(searchService.getSearchRecommendations(keyword));
    }

    @PostMapping("/history")
    public Result<Void> addSearchHistory(@RequestBody java.util.Map<String, String> params) {
        searchService.addSearchHistory(params.get("keyword"));
        return Result.success();
    }
}
