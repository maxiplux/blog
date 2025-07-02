package app.quantun.blog.application.port.in;

import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.shared.valueobject.Slug;

import java.util.List;

public interface GetBlogPostUseCase {
    BlogPost getById(PostId postId);
    BlogPost getBySlug(Slug slug);
    List<BlogPost> getAllPublished();
    List<BlogPost> getAllByAuthor(AuthorId authorId);
}