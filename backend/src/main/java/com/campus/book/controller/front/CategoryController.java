package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.entity.Category;
import com.campus.book.service.CategoryService;
import com.campus.book.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/parents")
    public Result<List<CategoryVO>> getParentCategories() {
        return Result.success(categoryService.getParentCategories());
    }

    @GetMapping("/{id}/children")
    public Result<List<CategoryVO>> getChildCategories(@PathVariable Long id) {
        return Result.success(categoryService.getChildCategories(id));
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }
}
