package app.quantun.blog.domain.model;



import app.quantun.blog.shared.valueobject.Slug;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder(toBuilder = true)
public class BlogPost {
    private final PostId id;
    private String title;
    private String content;
    private String summary;
    private Slug slug;
    private String authorId;
    private PostStatus status;

    @Singular
    private Set<Tag> tags;

    @Singular
    private List<Comment> comments;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private BlogPost(PostId id, String title, String content, String summary,
                     Slug slug, String authorId, PostStatus status, Set<Tag> tags,
                     List<Comment> comments, LocalDateTime createdAt,
                     LocalDateTime updatedAt, LocalDateTime publishedAt) {
        this.id = Objects.requireNonNull(id, "Post id cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.summary = summary;
        this.slug = Objects.requireNonNull(slug, "Slug cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "Author id cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        this.comments = comments != null ? new ArrayList<>(comments) : new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
        this.publishedAt = publishedAt;
    }

    public static BlogPost createDraft(String title, String content, String summary, String authorId) {
        LocalDateTime now = LocalDateTime.now();
        return BlogPost.builder()
                .id(PostId.generate())
                .title(title)
                .content(content)
                .summary(summary)
                .slug(Slug.fromTitle(title))
                .authorId(authorId)
                .status(PostStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public BlogPost updateContent(String newTitle, String newContent, String newSummary) {
        this.title = Objects.requireNonNull(newTitle, "Title cannot be null");
        this.content = Objects.requireNonNull(newContent, "Content cannot be null");
        this.summary = newSummary;
        this.slug = Slug.fromTitle(newTitle);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BlogPost publish() {
        if (this.status == PostStatus.PUBLISHED) {
            throw new IllegalStateException("Post is already published");
        }
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BlogPost archive() {
        this.status = PostStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BlogPost addTag(Tag tag) {
        this.tags.add(tag);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BlogPost removeTag(Tag tag) {
        this.tags.remove(tag);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public BlogPost addComment(Comment comment) {
        this.comments.add(comment);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == PostStatus.DRAFT;
    }

    // Return defensive copies for collections
    public Set<Tag> getTags() { return new HashSet<>(tags); }
    public List<Comment> getComments() { return new ArrayList<>(comments); }
}
