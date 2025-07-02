package app.quantun.blog.domain.model;

import app.quantun.blog.shared.valueobject.Slug;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder(toBuilder = true)
public class BlogPost {
    private final PostId id;
    private final String title;
    private final String content;
    private final String summary;
    private final Slug slug;
    private final AuthorId authorId;
    private final PostStatus status;
    private final Set<Tag> tags;
    private final List<Comment> comments;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime publishedAt;

    private BlogPost(PostId id, String title, String content, String summary,
                     Slug slug, AuthorId authorId, PostStatus status, Set<Tag> tags,
                     List<Comment> comments, LocalDateTime createdAt,
                     LocalDateTime updatedAt, LocalDateTime publishedAt) {
        this.id = Objects.requireNonNull(id, "Post id cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.summary = summary;
        this.slug = Objects.requireNonNull(slug, "Slug cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "Author id cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.tags = tags != null ? Set.copyOf(tags) : Set.of();
        this.comments = comments != null ? List.copyOf(comments) : List.of();
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
        this.publishedAt = publishedAt;
    }

    public static BlogPost createDraft(String title, String content, String summary, AuthorId authorId) {
        LocalDateTime now = LocalDateTime.now();
        return BlogPost.builder()
                .id(PostId.generate())
                .title(title)
                .content(content)
                .summary(summary)
                .slug(Slug.fromTitle(title))
                .authorId(authorId)
                .status(PostStatus.DRAFT)
                .tags(Set.of())
                .comments(List.of())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public BlogPost updateContent(String newTitle, String newContent, String newSummary) {
        Objects.requireNonNull(newTitle, "Title cannot be null");
        Objects.requireNonNull(newContent, "Content cannot be null");
        
        return this.toBuilder()
                .title(newTitle)
                .content(newContent)
                .summary(newSummary)
                .slug(Slug.fromTitle(newTitle))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public BlogPost publish() {
        if (this.status == PostStatus.PUBLISHED) {
            throw new IllegalStateException("Post is already published");
        }
        
        LocalDateTime now = LocalDateTime.now();
        return this.toBuilder()
                .status(PostStatus.PUBLISHED)
                .publishedAt(now)
                .updatedAt(now)
                .build();
    }

    public BlogPost archive() {
        return this.toBuilder()
                .status(PostStatus.ARCHIVED)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public BlogPost addTag(Tag tag) {
        Set<Tag> newTags = new HashSet<>(this.tags);
        newTags.add(tag);
        
        return this.toBuilder()
                .tags(newTags)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public BlogPost removeTag(Tag tag) {
        Set<Tag> newTags = new HashSet<>(this.tags);
        newTags.remove(tag);
        
        return this.toBuilder()
                .tags(newTags)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public BlogPost addComment(Comment comment) {
        List<Comment> newComments = new ArrayList<>(this.comments);
        newComments.add(comment);
        
        return this.toBuilder()
                .comments(newComments)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == PostStatus.DRAFT;
    }

    // Return defensive copies for collections
    public Set<Tag> getTags() { 
        return Set.copyOf(tags); 
    }
    
    public List<Comment> getComments() { 
        return List.copyOf(comments); 
    }
}
