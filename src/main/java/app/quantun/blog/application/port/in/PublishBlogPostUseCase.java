package app.quantun.blog.application.port.in;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;

public interface PublishBlogPostUseCase {
    BlogPost publishPost(PostId postId);
}