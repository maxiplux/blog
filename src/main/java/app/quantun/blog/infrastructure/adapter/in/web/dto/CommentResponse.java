package app.quantun.blog.infrastructure.adapter.in.web.dto;



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
}