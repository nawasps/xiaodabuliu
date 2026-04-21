package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.entity.Tag;
import com.campus.book.mapper.TagMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    private final TagMapper tagMapper;

    public TagController(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @GetMapping("/list")
    public Result<List<Tag>> getTagList() {
        return Result.success(tagMapper.selectList(null));
    }
}
