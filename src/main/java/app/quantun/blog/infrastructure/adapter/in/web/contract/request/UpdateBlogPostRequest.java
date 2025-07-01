package app.quantun.blog.infrastructure.adapter.in.web.contract.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateBlogPostRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        String summary,

        Set<String> tags
) {}
