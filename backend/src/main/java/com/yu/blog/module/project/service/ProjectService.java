package com.yu.blog.module.project.service;

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
import com.yu.blog.module.project.dto.ProjectSaveRequest;
import com.yu.blog.module.project.entity.Project;
import com.yu.blog.module.project.mapper.ProjectMapper;
import com.yu.blog.module.project.vo.ProjectDetailVO;
import com.yu.blog.module.project.vo.ProjectVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final String PLANNING = "PLANNING";
    private static final String DEVELOPING = "DEVELOPING";
    private static final String COMPLETED = "COMPLETED";

    private final ProjectMapper projectMapper;
    private final JsonValueMapper jsonValueMapper;
    private final CacheService cacheService;
    private final CacheProperties cacheProperties;

    public PageResult<ProjectVO> listPublic(String keyword, String techStack, String status, long page, long size) {
        String cacheKey = CacheKeys.projectList(keyword, techStack, status, page, size);
        return cacheService.getPage(cacheKey, ProjectVO.class)
                .orElseGet(() -> {
                    PageResult<ProjectVO> pageResult = loadPublic(keyword, techStack, status, page, size);
                    cacheService.put(cacheKey, pageResult, cacheProperties.projectTtl());
                    return pageResult;
                });
    }

    private PageResult<ProjectVO> loadPublic(String keyword, String techStack, String status, long page, long size) {
        LambdaQueryWrapper<Project> query = Wrappers.lambdaQuery(Project.class)
                .eq(Project::getVisible, true)
                .eq(StringUtils.hasText(status), Project::getStatus, status)
                .like(StringUtils.hasText(techStack), Project::getTechStackJson, techStack)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Project::getName, keyword)
                        .or()
                        .like(Project::getDescription, keyword))
                .orderByDesc(Project::getIsFeatured)
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getCreatedAt)
                .orderByDesc(Project::getId);
        IPage<Project> result = projectMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public ProjectDetailVO getPublicDetail(Long id) {
        Project project = projectMapper.selectOne(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getId, id)
                .eq(Project::getVisible, true));
        if (project == null) {
            throw new BusinessException(404, "项目不存在或未公开");
        }
        return toDetail(project);
    }

    public PageResult<ProjectVO> listAdmin(String keyword, String status, long page, long size) {
        LambdaQueryWrapper<Project> query = Wrappers.lambdaQuery(Project.class)
                .eq(StringUtils.hasText(status), Project::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Project::getName, keyword)
                        .or()
                        .like(Project::getDescription, keyword))
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getCreatedAt)
                .orderByDesc(Project::getId);
        IPage<Project> result = projectMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    public ProjectDetailVO getAdminDetail(Long id) {
        return toDetail(getExisting(id));
    }

    @Transactional
    public ProjectDetailVO create(ProjectSaveRequest request) {
        validateStatus(request.status());
        ensureSlugAvailable(request.slug(), null);

        Project project = new Project();
        applyEditableFields(project, request);
        project.setIsFeatured(false);
        projectMapper.insert(project);
        ProjectDetailVO result = toDetail(projectMapper.selectById(project.getId()));
        invalidateProjectCaches();
        return result;
    }

    @Transactional
    public ProjectDetailVO update(Long id, ProjectSaveRequest request) {
        validateStatus(request.status());
        Project project = getExisting(id);
        ensureSlugAvailable(request.slug(), id);
        applyEditableFields(project, request);
        projectMapper.updateById(project);
        ProjectDetailVO result = toDetail(projectMapper.selectById(id));
        invalidateProjectCaches();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        getExisting(id);
        projectMapper.deleteById(id);
        invalidateProjectCaches();
    }

    public List<ProjectVO> latestFeatured(int size) {
        return projectMapper.selectPage(new Page<>(1, size), Wrappers.lambdaQuery(Project.class)
                        .eq(Project::getVisible, true)
                        .orderByDesc(Project::getIsFeatured)
                        .orderByAsc(Project::getSortOrder)
                        .orderByDesc(Project::getCreatedAt)
                        .orderByDesc(Project::getId))
                .getRecords()
                .stream()
                .map(this::toVO)
                .toList();
    }

    public long publicCount() {
        return projectMapper.selectCount(Wrappers.lambdaQuery(Project.class).eq(Project::getVisible, true));
    }

    private void applyEditableFields(Project project, ProjectSaveRequest request) {
        project.setName(request.name());
        project.setSlug(request.slug());
        project.setDescription(request.description());
        project.setDetailContent(request.detailContent());
        project.setLearningSummary(request.detailContent());
        project.setCoverImageUrl(request.coverImage());
        project.setTechStackJson(jsonValueMapper.toJsonArray(request.techStack()));
        project.setStatus(request.status());
        project.setRepoUrl(request.githubUrl());
        project.setDemoUrl(request.demoUrl());
        project.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        project.setVisible(!Boolean.FALSE.equals(request.visible()));
    }

    private Project getExisting(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private void ensureSlugAvailable(String slug, Long excludeId) {
        Long count = projectMapper.selectCount(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getSlug, slug)
                .ne(excludeId != null, Project::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(400, "项目 slug 已存在");
        }
    }

    private void validateStatus(String status) {
        if (!PLANNING.equals(status) && !DEVELOPING.equals(status) && !COMPLETED.equals(status)) {
            throw new BusinessException(400, "项目状态不合法");
        }
    }

    private List<ProjectVO> toList(List<Project> projects) {
        return projects.stream().map(this::toVO).toList();
    }

    private ProjectVO toVO(Project project) {
        return ProjectVO.from(project, jsonValueMapper.toStringList(project.getTechStackJson()));
    }

    private ProjectDetailVO toDetail(Project project) {
        return ProjectDetailVO.from(project, jsonValueMapper.toStringList(project.getTechStackJson()));
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

    private void invalidateProjectCaches() {
        cacheService.evict(CacheKeys.homeOverview());
        cacheService.evictByPattern(CacheKeys.projectListPattern());
    }
}
