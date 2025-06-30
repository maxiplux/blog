package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.AggregateRoot;
import app.quantun.blog.shared.domain.DomainException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "articles")
@Getter

public class Article extends AggregateRoot {

    private ArticleId articleId;
    private Title title;
    private Slug slug;
    private Content content;
    private AuthorId authorId;
    private ArticleStatus status;
    private Set<String> tags;
    private LocalDateTime publishedAt;
    private int viewCount;

    // Constructor privado para la creación controlada
    private Article() {
        super();
        this.tags = new HashSet<>();
        this.viewCount = 0;
        this.status = ArticleStatus.DRAFT;
    }

    // Factory method para crear un nuevo artículo
    public static Article create(
            ArticleId articleId,
            Title title,
            Content content,
            AuthorId authorId) {

        Article article = new Article();
        article.articleId = articleId;
        article.title = title;
        article.slug = Slug.fromTitle(title);
        article.content = content;
        article.authorId = authorId;

        return article;
    }

    // Métodos de negocio
    public void updateTitle(Title newTitle) {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new DomainException("Cannot update title of published article");
        }
        this.title = newTitle;
        this.slug = Slug.fromTitle(newTitle);
        updateTimestamp();
    }

    public void updateContent(Content newContent) {
        this.content = newContent;
        updateTimestamp();
    }

    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new DomainException("Article is already published");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        updateTimestamp();
    }

    public void archive() {
        if (this.status == ArticleStatus.DRAFT) {
            throw new DomainException("Cannot archive draft article");
        }
        this.status = ArticleStatus.ARCHIVED;
        updateTimestamp();
    }

    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        this.tags.add(tag.toLowerCase().trim());
        updateTimestamp();
    }

    public void removeTag(String tag) {
        this.tags.remove(tag.toLowerCase().trim());
        updateTimestamp();
    }

    public void incrementViewCount() {
        this.viewCount++;
        updateTimestamp();
    }

    public boolean isDraft() {
        return this.status == ArticleStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == ArticleStatus.PUBLISHED;
    }

    public boolean isArchived() {
        return this.status == ArticleStatus.ARCHIVED;
    }

    public boolean isOwnedBy(AuthorId authorId) {
        return this.authorId.equals(authorId);
    }
}

