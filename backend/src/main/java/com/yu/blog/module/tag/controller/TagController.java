package com.yu.blog.module.tag.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.tag.service.TagService;
import com.yu.blog.module.tag.vo.TagVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> list() {
        return Result.ok(tagService.listPublicTags());
    }
}
