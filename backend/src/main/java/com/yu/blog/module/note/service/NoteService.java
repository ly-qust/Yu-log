package com.yu.blog.module.note.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.blog.auth.AuthenticatedUser;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.common.cache.CacheProperties;
import com.yu.blog.common.cache.CacheService;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.common.util.JsonValueMapper;
import com.yu.blog.module.note.dto.NoteSaveRequest;
import com.yu.blog.module.note.entity.Note;
import com.yu.blog.module.note.mapper.NoteMapper;
import com.yu.blog.module.note.vo.NoteVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NoteService {
    private static final String PUBLISHED = "PUBLISHED";
    private static final String HIDDEN = "HIDDEN";

    private final NoteMapper noteMapper;
    private final JsonValueMapper jsonValueMapper;
    private final CacheService cacheService;
    private final CacheProperties cacheProperties;

    public PageResult<NoteVO> listPublic(String keyword, String topic, long page, long size) {
        String cacheKey = CacheKeys.noteList(keyword, topic, page, size);
        return cacheService.getPage(cacheKey, NoteVO.class)
                .orElseGet(() -> {
                    PageResult<NoteVO> pageResult = loadPublic(keyword, topic, page, size);
                    cacheService.put(cacheKey, pageResult, cacheProperties.noteTtl());
                    return pageResult;
                });
    }

    private PageResult<NoteVO> loadPublic(String keyword, String topic, long page, long size) {
        LambdaQueryWrapper<Note> query = Wrappers.lambdaQuery(Note.class)
                .eq(Note::getIsPublic, true)
                .eq(StringUtils.hasText(topic), Note::getTopic, topic)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getSummary, keyword)
                        .or()
                        .like(Note::getContentMd, keyword))
                .orderByDesc(Note::getIsPinned)
                .orderByAsc(Note::getSortOrder)
                .orderByDesc(Note::getCreatedAt)
                .orderByDesc(Note::getId);
        IPage<Note> result = noteMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public NoteVO getPublicDetail(Long id) {
        Note note = noteMapper.selectOne(Wrappers.lambdaQuery(Note.class)
                .eq(Note::getId, id)
                .eq(Note::getIsPublic, true));
        if (note == null) {
            throw new BusinessException(404, "笔记不存在或未公开");
        }
        return toVO(note);
    }

    public PageResult<NoteVO> listAdmin(String keyword, String topic, Boolean isPublic, long page, long size) {
        LambdaQueryWrapper<Note> query = Wrappers.lambdaQuery(Note.class)
                .eq(StringUtils.hasText(topic), Note::getTopic, topic)
                .eq(isPublic != null, Note::getIsPublic, isPublic)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getSummary, keyword)
                        .or()
                        .like(Note::getContentMd, keyword))
                .orderByDesc(Note::getIsPinned)
                .orderByAsc(Note::getSortOrder)
                .orderByDesc(Note::getCreatedAt)
                .orderByDesc(Note::getId);
        IPage<Note> result = noteMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public NoteVO getAdminDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Transactional
    public NoteVO create(NoteSaveRequest request) {
        ensureSlugAvailable(request.slug(), null);
        Note note = new Note();
        note.setAuthorUserId(currentUserId());
        applyEditableFields(note, request);
        noteMapper.insert(note);
        NoteVO result = toVO(noteMapper.selectById(note.getId()));
        invalidateNoteCaches();
        return result;
    }

    @Transactional
    public NoteVO update(Long id, NoteSaveRequest request) {
        Note note = getExisting(id);
        ensureSlugAvailable(request.slug(), id);
        applyEditableFields(note, request);
        noteMapper.updateById(note);
        NoteVO result = toVO(noteMapper.selectById(id));
        invalidateNoteCaches();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        getExisting(id);
        noteMapper.deleteById(id);
        invalidateNoteCaches();
    }

    public List<NoteVO> latestPublic(int size) {
        return noteMapper.selectPage(new Page<>(1, size), Wrappers.lambdaQuery(Note.class)
                        .eq(Note::getIsPublic, true)
                        .orderByDesc(Note::getIsPinned)
                        .orderByAsc(Note::getSortOrder)
                        .orderByDesc(Note::getCreatedAt)
                        .orderByDesc(Note::getId))
                .getRecords()
                .stream()
                .map(this::toVO)
                .toList();
    }

    public long publicCount() {
        return noteMapper.selectCount(Wrappers.lambdaQuery(Note.class).eq(Note::getIsPublic, true));
    }

    private void applyEditableFields(Note note, NoteSaveRequest request) {
        boolean isPublic = !Boolean.FALSE.equals(request.isPublic());
        String topic = StringUtils.hasText(request.topic()) ? request.topic() : "NOTE";
        note.setTitle(request.title());
        note.setSlug(request.slug());
        note.setSummary(request.summary());
        note.setContentMd(request.content());
        note.setTopic(topic);
        note.setNoteType(topic);
        note.setTagsJson(jsonValueMapper.toJsonArray(request.tags()));
        note.setIsPublic(isPublic);
        note.setStatus(isPublic ? PUBLISHED : HIDDEN);
        note.setIsPinned(false);
        note.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private Note getExisting(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(404, "笔记不存在");
        }
        return note;
    }

    private void ensureSlugAvailable(String slug, Long excludeId) {
        Long count = noteMapper.selectCount(Wrappers.lambdaQuery(Note.class)
                .eq(Note::getSlug, slug)
                .ne(excludeId != null, Note::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "笔记 slug 已存在");
        }
    }

    private List<NoteVO> toList(List<Note> notes) {
        return notes.stream().map(this::toVO).toList();
    }

    private NoteVO toVO(Note note) {
        return NoteVO.from(note, jsonValueMapper.toStringList(note.getTagsJson()));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return 1L;
    }

    private long safePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long safeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private void invalidateNoteCaches() {
        cacheService.evict(CacheKeys.homeOverview());
        cacheService.evictByPattern(CacheKeys.noteListPattern());
    }
}
