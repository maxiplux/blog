package app.quantun.blog.application.command.port.out;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;

public interface BlogPostCommandRepositoryPort {
    BlogPost save(BlogPost blogPost);

    void deleteById(PostId postId);
}