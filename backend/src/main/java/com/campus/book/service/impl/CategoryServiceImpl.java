package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.book.entity.Category;
import com.campus.book.mapper.CategoryMapper;
import com.campus.book.service.CategoryService;
import com.campus.book.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, "ACTIVE")
                        .orderByAsc(Category::getSort));
        return buildTree(allCategories);
    }

    @Override
    public List<CategoryVO> getParentCategories() {
        List<Category> parents = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .isNull(Category::getParentId)
                        .eq(Category::getStatus, "ACTIVE")
                        .orderByAsc(Category::getSort));
        return parents.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getChildCategories(Long parentId) {
        List<Category> children = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, parentId)
                        .eq(Category::getStatus, "ACTIVE")
                        .orderByAsc(Category::getSort));
        return children.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void addCategory(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Category category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    private List<CategoryVO> buildTree(List<Category> categories) {
        List<CategoryVO> tree = new ArrayList<>();
        for (Category category : categories) {
            if (category.getParentId() == null) {
                CategoryVO vo = convertToVO(category);
                vo.setChildren(getChildren(category.getId(), categories));
                tree.add(vo);
            }
        }
        return tree;
    }

    private List<CategoryVO> getChildren(Long parentId, List<Category> allCategories) {
        List<CategoryVO> children = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId() != null && category.getParentId().equals(parentId)) {
                CategoryVO vo = convertToVO(category);
                vo.setChildren(getChildren(category.getId(), allCategories));
                children.add(vo);
            }
        }
        return children;
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
