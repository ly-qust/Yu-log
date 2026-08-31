package com.yu.blog.module.timeline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.cache.CacheKeys;
import com.yu.blog.common.cache.CacheProperties;
import com.yu.blog.common.cache.CacheService;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.common.util.JsonValueMapper;
import com.yu.blog.module.timeline.dto.TimelineSaveRequest;
import com.yu.blog.module.timeline.entity.TimelineEvent;
import com.yu.blog.module.timeline.mapper.TimelineEventMapper;
import com.yu.blog.module.timeline.vo.TimelineEventVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private static final String PUBLISHED = "PUBLISHED";
    private static final String HIDDEN = "HIDDEN";

    private final TimelineEventMapper timelineEventMapper;
    private final JsonValueMapper jsonValueMapper;
    private final CacheService cacheService;
    private final CacheProperties cacheProperties;

    public PageResult<TimelineEventVO> listPublic(String type, long page, long size) {
        String cacheKey = CacheKeys.timelineList(type, page, size);
        return cacheService.getPage(cacheKey, TimelineEventVO.class)
                .orElseGet(() -> {
                    PageResult<TimelineEventVO> pageResult = loadPublic(type, page, size);
                    cacheService.put(cacheKey, pageResult, cacheProperties.timelineTtl());
                    return pageResult;
                });
    }

    private PageResult<TimelineEventVO> loadPublic(String type, long page, long size) {
        LambdaQueryWrapper<TimelineEvent> query = Wrappers.lambdaQuery(TimelineEvent.class)
                .eq(TimelineEvent::getVisible, true)
                .eq(StringUtils.hasText(type), TimelineEvent::getEventType, type)
                .orderByDesc(TimelineEvent::getEventDate)
                .orderByAsc(TimelineEvent::getSortOrder)
                .orderByDesc(TimelineEvent::getId);
        IPage<TimelineEvent> result = timelineEventMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public PageResult<TimelineEventVO> listAdmin(String keyword, String type, long page, long size) {
        LambdaQueryWrapper<TimelineEvent> query = Wrappers.lambdaQuery(TimelineEvent.class)
                .eq(StringUtils.hasText(type), TimelineEvent::getEventType, type)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(TimelineEvent::getTitle, keyword)
                        .or()
                        .like(TimelineEvent::getSummary, keyword))
                .orderByDesc(TimelineEvent::getEventDate)
                .orderByAsc(TimelineEvent::getSortOrder)
                .orderByDesc(TimelineEvent::getId);
        IPage<TimelineEvent> result = timelineEventMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public TimelineEventVO getAdminDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Transactional
    public TimelineEventVO create(TimelineSaveRequest request) {
        TimelineEvent event = new TimelineEvent();
        applyEditableFields(event, request);
        timelineEventMapper.insert(event);
        TimelineEventVO result = toVO(timelineEventMapper.selectById(event.getId()));
        invalidateTimelineCaches();
        return result;
    }

    @Transactional
    public TimelineEventVO update(Long id, TimelineSaveRequest request) {
        TimelineEvent event = getExisting(id);
        applyEditableFields(event, request);
        timelineEventMapper.updateById(event);
        TimelineEventVO result = toVO(timelineEventMapper.selectById(id));
        invalidateTimelineCaches();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        getExisting(id);
        timelineEventMapper.deleteById(id);
        invalidateTimelineCaches();
    }

    public List<TimelineEventVO> latestPublic(int size) {
        return timelineEventMapper.selectPage(new Page<>(1, size), Wrappers.lambdaQuery(TimelineEvent.class)
                        .eq(TimelineEvent::getVisible, true)
                        .orderByDesc(TimelineEvent::getEventDate)
                        .orderByAsc(TimelineEvent::getSortOrder)
                        .orderByDesc(TimelineEvent::getId))
                .getRecords()
                .stream()
                .map(this::toVO)
                .toList();
    }

    private void applyEditableFields(TimelineEvent event, TimelineSaveRequest request) {
        boolean visible = !Boolean.FALSE.equals(request.visible());
        event.setTitle(request.title());
        event.setSummary(request.description());
        event.setContentMd(request.description());
        event.setEventDate(request.eventDate());
        event.setEventType(StringUtils.hasText(request.type()) ? request.type() : "MILESTONE");
        event.setTagsJson(jsonValueMapper.toJsonArray(request.tags()));
        event.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        event.setVisible(visible);
        event.setStatus(visible ? PUBLISHED : HIDDEN);
    }

    private TimelineEvent getExisting(Long id) {
        TimelineEvent event = timelineEventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException(404, "时间线不存在");
        }
        return event;
    }

    private List<TimelineEventVO> toList(List<TimelineEvent> events) {
        return events.stream().map(this::toVO).toList();
    }

    private TimelineEventVO toVO(TimelineEvent event) {
        return TimelineEventVO.from(event, jsonValueMapper.toStringList(event.getTagsJson()));
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

    private void invalidateTimelineCaches() {
        cacheService.evict(CacheKeys.homeOverview());
        cacheService.evictByPattern(CacheKeys.timelineListPattern());
    }
}
