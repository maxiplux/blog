package app.quantun.blog.domain.port.in;

import app.quantun.blog.domain.model.BlogPost;

import java.util.Set;

public interface CreateBlogPostUseCase {
    BlogPost createBlogPost(CreateBlogPostCommand command);

    record CreateBlogPostCommand(
            String title,
            String content,
            String summary,
            String authorId,
            Set<String> tagNames
    ) {
        public CreateBlogPostCommand {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }
            if (authorId == null || authorId.isBlank()) {
                throw new IllegalArgumentException("Author ID cannot be null or empty");
            }
        }
    }
}
