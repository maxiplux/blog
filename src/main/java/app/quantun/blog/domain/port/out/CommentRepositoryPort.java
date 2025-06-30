package app.quantun.blog.domain.port.out;

import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryPort {
    Comment save(Comment comment);
    Optional<Comment> findById(String id);
    List<Comment> findByPostId(PostId postId);
    void deleteById(String id);
}
