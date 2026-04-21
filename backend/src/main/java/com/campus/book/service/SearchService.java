package com.campus.book.service;

import java.util.List;
import java.util.Map;

public interface SearchService {

    void addSearchHistory(String keyword);

    List<String> getSearchHistory();

    void clearSearchHistory();

    List<String> getHotSearch();

    List<String> getSearchRecommendations(String keyword);

    void incrementSearchCount(String keyword);
}
