package app.quantun.blog.application.port.in;

import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;
import lombok.Builder;

public interface AddCommentUseCase {
    Comment addComment(AddCommentCommand command);
    @Builder
    record AddCommentCommand(
            PostId postId,
            String content,
            String authorName,
            String authorEmail
    ) {
        public AddCommentCommand {
            if (postId == null) {
                throw new IllegalArgumentException("Post ID cannot be null");
            }
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }
            if (authorName == null || authorName.isBlank()) {
                throw new IllegalArgumentException("Author name cannot be null or empty");
            }
            if (authorEmail == null || authorEmail.isBlank()) {
                throw new IllegalArgumentException("Author email cannot be null or empty");
            }
        }
    }
}