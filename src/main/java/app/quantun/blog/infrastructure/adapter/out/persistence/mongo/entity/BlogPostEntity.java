package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
