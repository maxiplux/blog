package app.quantun.blog.content.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleResponse {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String authorId;
    private String status;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private int viewCount;
    private int wordCount;
}

