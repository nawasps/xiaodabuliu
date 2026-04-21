package com.campus.book.service.impl;

import com.campus.book.common.constants.Constants;
import com.campus.book.entity.SearchHistory;
import com.campus.book.mapper.SearchHistoryMapper;
import com.campus.book.service.SearchService;
import com.campus.book.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void addSearchHistory(String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        searchHistoryMapper.insert(history);

        String cacheKey = Constants.SEARCH_HISTORY_KEY + userId;
        redisTemplate.opsForList().leftPush(cacheKey, keyword);

        incrementSearchCount(keyword);
    }

    @Override
    public List<String> getSearchHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        String cacheKey = Constants.SEARCH_HISTORY_KEY + userId;

        List<Object> cached = redisTemplate.opsForList().range(cacheKey, 0, 9);
        if (cached != null && !cached.isEmpty()) {
            return cached.stream().map(Object::toString).collect(Collectors.toList());
        }

        List<SearchHistory> histories = searchHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistory>()
                        .eq(SearchHistory::getUserId, userId)
                        .orderByDesc(SearchHistory::getCreateTime)
                        .last("LIMIT 10"));

        List<String> keywords = histories.stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .collect(Collectors.toList());

        for (String keyword : keywords) {
        redisTemplate.opsForList().leftPush(cacheKey, keyword);
        }

        return keywords;
    }

    @Override
    @Transactional
    public void clearSearchHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        searchHistoryMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistory>()
                        .eq(SearchHistory::getUserId, userId));

        String cacheKey = Constants.SEARCH_HISTORY_KEY + userId;
        redisTemplate.delete(cacheKey);
    }

    @Override
    public List<String> getHotSearch() {
        String cacheKey = Constants.HOT_SEARCH_KEY;
        Set<Object> hotKeywords = redisTemplate.opsForZSet().reverseRange(cacheKey, 0, 9);

        if (hotKeywords != null && !hotKeywords.isEmpty()) {
            return hotKeywords.stream().map(Object::toString).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public List<String> getSearchRecommendations(String keyword) {
        List<String> hotSearch = getHotSearch();
        return hotSearch.stream()
                .filter(k -> k.contains(keyword))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementSearchCount(String keyword) {
        String cacheKey = Constants.HOT_SEARCH_KEY;
        redisTemplate.opsForZSet().incrementScore(cacheKey, keyword, 1);
    }
}
