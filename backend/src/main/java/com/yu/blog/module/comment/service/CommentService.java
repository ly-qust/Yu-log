package com.yu.blog.module.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yu.blog.common.api.PageResult;
import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.common.service.RateLimitService;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.comment.dto.CommentReplyRequest;
import com.yu.blog.module.comment.dto.CommentSubmitRequest;
import com.yu.blog.module.comment.entity.Comment;
import com.yu.blog.module.comment.mapper.CommentMapper;
import com.yu.blog.module.comment.vo.AdminCommentVO;
import com.yu.blog.module.comment.vo.PublicCommentVO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String SUBMITTED_MESSAGE = "\u8bc4\u8bba\u5df2\u63d0\u4ea4\uff0c\u5ba1\u6838\u901a\u8fc7\u540e\u5c55\u793a";

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final RateLimitService rateLimitService;

    @Transactional
    public String submit(Long articleId, CommentSubmitRequest request, String clientIp, String userAgent) {
        rateLimitService.check("comment", clientIp, 3, Duration.ofSeconds(60));
        Article article = getPublishedArticle(articleId);
        if (Boolean.FALSE.equals(article.getAllowComment())) {
            throw new BusinessException(400, "Article comments are disabled");
        }

        Comment comment = new Comment();
        comment.setArticleId(article.getId());
        comment.setNickname(trim(request.nickname()));
        comment.setEmail(normalize(request.email()));
        comment.setContent(trim(request.content()));
        comment.setStatus(PENDING);
        comment.setIpHash(rateLimitService.ipHash(clientIp));
        comment.setUserAgent(truncate(userAgent, 512));
        commentMapper.insert(comment);
        return SUBMITTED_MESSAGE;
    }

    public List<PublicCommentVO> listPublic(Long articleId) {
        getPublishedArticle(articleId);
        return commentMapper.selectList(Wrappers.lambdaQuery(Comment.class)
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, APPROVED)
                        .orderByAsc(Comment::getCreatedAt)
                        .orderByAsc(Comment::getId))
                .stream()
                .map(PublicCommentVO::from)
                .toList();
    }

    public PageResult<AdminCommentVO> listAdmin(
            Long articleId,
            String status,
            String keyword,
            long page,
            long size
    ) {
        if (StringUtils.hasText(status)) {
            validateStatus(status);
        }
        LambdaQueryWrapper<Comment> query = Wrappers.lambdaQuery(Comment.class)
                .eq(articleId != null, Comment::getArticleId, articleId)
                .eq(StringUtils.hasText(status), Comment::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Comment::getNickname, keyword)
                        .or()
                        .like(Comment::getEmail, keyword)
                        .or()
                        .like(Comment::getContent, keyword))
                .orderByDesc(Comment::getCreatedAt)
                .orderByDesc(Comment::getId);
        IPage<Comment> result = commentMapper.selectPage(new Page<>(safePage(page), safeSize(size)), query);
        return PageResult.of(toAdminList(result.getRecords()), result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Transactional
    public AdminCommentVO updateStatus(Long id, String status) {
        validateStatus(status);
        Comment comment = getExisting(id);
        comment.setStatus(status);
        commentMapper.updateById(comment);
        refreshArticleCommentCount(comment.getArticleId());
        return toAdmin(commentMapper.selectById(id));
    }

    @Transactional
    public AdminCommentVO reply(Long id, CommentReplyRequest request) {
        Comment comment = getExisting(id);
        comment.setAdminReply(trim(request.adminReply()));
        comment.setRepliedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
        return toAdmin(commentMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = getExisting(id);
        Long articleId = comment.getArticleId();
        commentMapper.deleteById(id);
        refreshArticleCommentCount(articleId);
    }

    private Article getPublishedArticle(Long articleId) {
        Article article = articleMapper.selectOne(Wrappers.lambdaQuery(Article.class)
                .eq(Article::getId, articleId)
                .eq(Article::getStatus, PUBLISHED));
        if (article == null) {
            throw new BusinessException(404, "Article does not exist or is not published");
        }
        return article;
    }

    private Comment getExisting(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "Comment does not exist");
        }
        return comment;
    }

    private void refreshArticleCommentCount(Long articleId) {
        Long approvedCount = commentMapper.selectCount(Wrappers.lambdaQuery(Comment.class)
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, APPROVED));
        articleMapper.update(null, Wrappers.lambdaUpdate(Article.class)
                .eq(Article::getId, articleId)
                .set(Article::getCommentCount, approvedCount));
    }

    private List<AdminCommentVO> toAdminList(List<Comment> comments) {
        Map<Long, Article> articles = articlesById(comments);
        return comments.stream()
                .map(comment -> AdminCommentVO.from(comment, articles.get(comment.getArticleId())))
                .toList();
    }

    private AdminCommentVO toAdmin(Comment comment) {
        return AdminCommentVO.from(comment, articleMapper.selectById(comment.getArticleId()));
    }

    private Map<Long, Article> articlesById(List<Comment> comments) {
        List<Long> articleIds = comments.stream()
                .map(Comment::getArticleId)
                .distinct()
                .toList();
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        return articleMapper.selectBatchIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Function.identity()));
    }

    private void validateStatus(String status) {
        if (!PENDING.equals(status) && !APPROVED.equals(status) && !REJECTED.equals(status)) {
            throw new BusinessException(400, "Invalid comment status");
        }
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

    private String normalize(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
