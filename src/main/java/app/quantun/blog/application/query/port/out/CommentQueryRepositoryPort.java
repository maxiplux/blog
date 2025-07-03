package app.quantun.blog.application.query.port.out;

import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;

import java.util.List;
import java.util.Optional;

public interface CommentQueryRepositoryPort {
    Optional<Comment> findById(String id);

    List<Comment> findByPostId(PostId postId);
}