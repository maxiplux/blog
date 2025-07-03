package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity;

import app.quantun.blog.domain.model.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blog_posts")
public class BlogPostEntity {

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("content")
    private String content;

    @Field("summary")
    private String summary;

    @Field("slug")
    private String slug;

    @Field("author_id")
    private String authorId;

    @Field("status")
    private String status;

    @Field("tags")
    private Set<TagEntity> tags;

    @Field("comments")
    private List<CommentEntity> comments;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("published_at")
    private LocalDateTime publishedAt;

    // ===== MÉTODOS AUXILIARES PARA CQRS =====

    /**
     * Convierte el status string a enum
     */
    public PostStatus getStatusAsEnum() {
        try {
            return PostStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return PostStatus.DRAFT; // Default fallback
        }
    }

    /**
     * Obtiene los nombres de los tags como Set<String>
     * Optimizado para read models
     */
    public Set<String> getTagNames() {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene el conteo de comentarios
     * Optimizado para list items
     */
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    /**
     * Verifica si el post está publicado
     */
    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    /**
     * Verifica si el post es borrador
     */
    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagEntity {
        private String name;
        private String slug;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentEntity {
        private String id;
        private String content;
        private String authorName;
        private String authorEmail;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
