package app.quantun.blog.application.query.model;

import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.shared.valueobject.Slug;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Read model completo para visualización de blog posts
 * Incluye toda la información necesaria para mostrar el post completo
 */
public record BlogPostReadModel(
        String id,
        String title,
        String content,
        String summary,
        Slug slug,
        String authorId,
        String authorName,
        String authorEmail,
        PostStatus status,
        Set<String> tagNames,
        List<CommentReadModel> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt
) {
    public BlogPostReadModel {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
    }

    public boolean isPublished() {
        return status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return status == PostStatus.DRAFT;
    }

    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    /**
     * Read model para comentarios dentro del blog post
     */
    public record CommentReadModel(
            String id,
            String content,
            String authorName,
            String authorEmail,
            LocalDateTime createdAt
    ) {
    }
}
