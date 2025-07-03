package app.quantun.blog.infrastructure.adapter.in.web.contract.response;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record BlogPostResponse(
        String id,
        String title,
        String content,
        String summary,
        String slug,
        String authorId,
        String authorName,
        String authorEmail,
        PostStatus status,
        Set<String> tags,
        List<CommentResponse> comments,
        Integer commentCount,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime publishedAt
) {
    /**
     * Factory method para crear respuesta desde entidad de dominio
     * Usado principalmente en operaciones de comando
     */
    public static BlogPostResponse fromDomain(BlogPost blogPost) {
        return BlogPostResponse.builder()
                .id(blogPost.getId().value())
                .title(blogPost.getTitle())
                .content(blogPost.getContent())
                .summary(blogPost.getSummary())
                .slug(blogPost.getSlug().value())
                .authorId(blogPost.getAuthorId().value())
                .authorName(null) // No disponible en domain model
                .authorEmail(null) // No disponible en domain model
                .status(blogPost.getStatus())
                .tags(blogPost.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(java.util.stream.Collectors.toSet()))
                .comments(blogPost.getComments().stream()
                        .map(CommentResponse::fromDomain)
                        .toList())
                .commentCount(blogPost.getComments().size())
                .createdAt(blogPost.getCreatedAt())
                .updatedAt(blogPost.getUpdatedAt())
                .publishedAt(blogPost.getPublishedAt())
                .build();
    }

    /**
     * Factory method para crear respuesta desde read model completo
     * Usado en operaciones de query detalladas
     */
    public static BlogPostResponse fromReadModel(BlogPostReadModel readModel) {
        if (readModel == null) {
            throw new app.quantun.blog.shared.exception.BlogPostNotFoundException("Blog post not found");
        }

        return BlogPostResponse.builder()
                .id(readModel.id())
                .title(readModel.title())
                .content(readModel.content())
                .summary(readModel.summary())
                .slug(readModel.slug().value())
                .authorId(readModel.authorId())
                .authorName(readModel.authorName())
                .authorEmail(readModel.authorEmail())
                .status(readModel.status())
                .tags(readModel.tagNames())
                .comments(readModel.comments().stream()
                        .map(CommentResponse::fromReadModel)
                        .toList())
                .commentCount(readModel.getCommentCount())
                .createdAt(readModel.createdAt())
                .updatedAt(readModel.updatedAt())
                .publishedAt(readModel.publishedAt())
                .build();
    }

    /**
     * Factory method para crear respuesta desde item de lista
     * Usado en operaciones de query con listados (sin contenido completo)
     */
    public static BlogPostResponse fromListItem(BlogPostListItem listItem) {
        return BlogPostResponse.builder()
                .id(listItem.id())
                .title(listItem.title())
                .content(null) // No incluido en list items por performance
                .summary(listItem.summary())
                .slug(listItem.slug().value())
                .authorId(listItem.authorId())
                .authorName(listItem.authorName())
                .authorEmail(null) // No incluido en list items
                .status(listItem.status())
                .tags(listItem.tagNames())
                .comments(null) // No incluido en list items
                .commentCount(listItem.commentCount())
                .createdAt(listItem.createdAt())
                .updatedAt(null) // No incluido en list items
                .publishedAt(listItem.publishedAt())
                .build();
    }
}
