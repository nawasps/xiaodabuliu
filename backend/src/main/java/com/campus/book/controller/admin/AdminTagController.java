package com.campus.book.controller.admin;

import com.campus.book.common.result.Result;
import com.campus.book.entity.Tag;
import com.campus.book.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tag")
public class AdminTagController {

    @Autowired
    private TagMapper tagMapper;

    @GetMapping("/list")
    public Result<List<Tag>> getTagList() {
        return Result.success(tagMapper.selectList(null));
    }

    @PostMapping
    public Result<Void> addTag(@RequestBody Tag tag) {
        tagMapper.insert(tag);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagMapper.updateById(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagMapper.deleteById(id);
        return Result.success();
    }
}
