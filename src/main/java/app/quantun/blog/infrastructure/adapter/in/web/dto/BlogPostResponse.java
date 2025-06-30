package app.quantun.blog.infrastructure.adapter.in.web.dto;



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
        PostStatus status,
        Set<String> tags,
        List<CommentResponse> comments,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime publishedAt
) {
    public static BlogPostResponse fromDomain(BlogPost blogPost) {
        return BlogPostResponse.builder()
                .id(blogPost.getId().value())
                .title(blogPost.getTitle())
                .content(blogPost.getContent())
                .summary(blogPost.getSummary())
                .slug(blogPost.getSlug().value())
                .authorId(blogPost.getAuthorId())
                .status(blogPost.getStatus())
                .tags(blogPost.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(java.util.stream.Collectors.toSet()))
                .comments(blogPost.getComments().stream()
                        .map(CommentResponse::fromDomain)
                        .toList())
                .createdAt(blogPost.getCreatedAt())
                .updatedAt(blogPost.getUpdatedAt())
                .publishedAt(blogPost.getPublishedAt())
                .build();
    }
}
