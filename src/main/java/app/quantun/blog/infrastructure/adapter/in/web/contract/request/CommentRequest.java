package app.quantun.blog.infrastructure.adapter.in.web.contract.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CommentRequest(
        @NotBlank(message = "Content is required")
        String content,

        @NotBlank(message = "Author name is required")
        String authorName,

        @NotBlank(message = "Author email is required")
        String authorEmail
) {}
