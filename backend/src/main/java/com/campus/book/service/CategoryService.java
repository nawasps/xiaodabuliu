package com.campus.book.service;

import com.campus.book.entity.Category;
import com.campus.book.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> getCategoryTree();

    List<CategoryVO> getParentCategories();

    List<CategoryVO> getChildCategories(Long parentId);

    Category getCategoryById(Long id);

    void addCategory(Category category);

    void updateCategory(Category category);

    void deleteCategory(Long id);
}
