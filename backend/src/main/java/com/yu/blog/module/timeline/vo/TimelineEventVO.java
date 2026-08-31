package com.yu.blog.module.timeline.vo;

import com.yu.blog.module.timeline.entity.TimelineEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TimelineEventVO(
        String id,
        String title,
        String description,
        LocalDate eventDate,
        String type,
        List<String> tags,
        Integer sortOrder,
        Boolean visible,
        LocalDateTime createdAt
) {
    public static TimelineEventVO from(TimelineEvent event, List<String> tags) {
        return new TimelineEventVO(
                String.valueOf(event.getId()),
                event.getTitle(),
                event.getSummary(),
                event.getEventDate(),
                event.getEventType(),
                tags == null ? List.of() : tags,
                event.getSortOrder(),
                event.getVisible(),
                event.getCreatedAt()
        );
    }
}
