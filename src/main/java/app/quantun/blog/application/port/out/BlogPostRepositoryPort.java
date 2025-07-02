package app.quantun.blog.application.port.out;

import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.shared.valueobject.Slug;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepositoryPort {
    BlogPost save(BlogPost blogPost);
    Optional<BlogPost> findById(PostId postId);
    Optional<BlogPost> findBySlug(Slug slug);
    List<BlogPost> findByStatus(PostStatus status);
    List<BlogPost> findByAuthorId(AuthorId authorId);
    List<BlogPost> findByTagName(String tagName);
    void deleteById(PostId postId);
    boolean existsBySlug(Slug slug);
}