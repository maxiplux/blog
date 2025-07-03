package app.quantun.blog.application.query.model;

import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.shared.valueobject.Slug;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Read model optimizado para listados de blog posts
 * Contiene solo los campos necesarios para vistas de lista
 */
public record BlogPostListItem(
        String id,
        String title,
        String summary,
        Slug slug,
        String authorId,
        String authorName,
        PostStatus status,
        Set<String> tagNames,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
    public BlogPostListItem {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
    }

    public boolean isPublished() {
        return status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return status == PostStatus.DRAFT;
    }
}
