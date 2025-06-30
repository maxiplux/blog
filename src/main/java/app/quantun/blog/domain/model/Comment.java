package app.quantun.blog.domain.model;


import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class Comment {
    private final String id;
    private final PostId postId;
    private String content;
    private String authorName;
    private String authorEmail;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comment(String id, PostId postId, String content, String authorName,
                   String authorEmail, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Comment id cannot be null");
        this.postId = Objects.requireNonNull(postId, "Post id cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.authorName = Objects.requireNonNull(authorName, "Author name cannot be null");
        this.authorEmail = Objects.requireNonNull(authorEmail, "Author email cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
    }

    public static Comment create(PostId postId, String content, String authorName, String authorEmail) {
        LocalDateTime now = LocalDateTime.now();
        return Comment.builder()
                .id(java.util.UUID.randomUUID().toString())
                .postId(postId)
                .content(content)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Comment updateContent(String newContent) {
        this.content = Objects.requireNonNull(newContent, "Content cannot be null");
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}