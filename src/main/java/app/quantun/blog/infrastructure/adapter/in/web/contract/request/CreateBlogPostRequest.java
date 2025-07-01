package app.quantun.blog.infrastructure.adapter.in.web.contract.request;

import lombok.Builder;

import java.util.Set;

@Builder
public record CreateBlogPostRequest(
        String title,
        String content,
        String summary,
        String authorId,
        Set<String> tags
) {
    public CreateBlogPostRequest {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Summary cannot be null or empty");
        }
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Author ID cannot be null or empty");
        }
    }
}

