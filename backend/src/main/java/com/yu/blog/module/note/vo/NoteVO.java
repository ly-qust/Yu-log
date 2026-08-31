package com.yu.blog.module.note.vo;

import com.yu.blog.module.note.entity.Note;
import java.time.LocalDateTime;
import java.util.List;

public record NoteVO(
        String id,
        String title,
        String slug,
        String summary,
        String content,
        String topic,
        List<String> tags,
        Boolean isPublic,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoteVO from(Note note, List<String> tags) {
        return new NoteVO(
                String.valueOf(note.getId()),
                note.getTitle(),
                note.getSlug(),
                note.getSummary(),
                note.getContentMd(),
                note.getTopic(),
                tags == null ? List.of() : tags,
                note.getIsPublic(),
                note.getSortOrder(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
