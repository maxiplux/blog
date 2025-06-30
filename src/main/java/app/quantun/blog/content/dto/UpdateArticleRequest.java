package app.quantun.blog.content.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateArticleRequest {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(min = 10, message = "Content must have at least 10 characters")
    private String content;

    private Set<String> tags;
}