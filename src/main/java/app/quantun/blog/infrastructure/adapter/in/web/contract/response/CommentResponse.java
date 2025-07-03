package app.quantun.blog.infrastructure.adapter.in.web.contract.response;

import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.domain.model.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentResponse(
        String id,
        String postId,
        String content,
        String authorName,
        String authorEmail,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    /**
     * Factory method para crear respuesta desde entidad de dominio
     */
    public static CommentResponse fromDomain(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId().value())
                .content(comment.getContent())
                .authorName(comment.getAuthorName())
                .authorEmail(comment.getAuthorEmail())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * Factory method para crear respuesta desde read model
     */
    public static CommentResponse fromReadModel(BlogPostReadModel.CommentReadModel commentReadModel) {
        return CommentResponse.builder()
                .id(commentReadModel.id())
                .postId(null) // Ya está en el contexto del post
                .content(commentReadModel.content())
                .authorName(commentReadModel.authorName())
                .authorEmail(commentReadModel.authorEmail())
                .createdAt(commentReadModel.createdAt())
                .updatedAt(null) // No incluido en read model
                .build();
    }
}
