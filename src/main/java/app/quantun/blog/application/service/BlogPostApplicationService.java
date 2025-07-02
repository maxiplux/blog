package app.quantun.blog.application.service;

import app.quantun.blog.application.port.in.CreateBlogPostUseCase;
import app.quantun.blog.application.port.in.GetBlogPostUseCase;
import app.quantun.blog.application.port.in.PublishBlogPostUseCase;
import app.quantun.blog.application.port.out.AuthorRepositoryPort;
import app.quantun.blog.application.port.out.BlogPostRepositoryPort;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogPostApplicationService implements CreateBlogPostUseCase, GetBlogPostUseCase, PublishBlogPostUseCase {

    private final BlogPostRepositoryPort blogPostRepositoryPort;
    private final AuthorRepositoryPort authorRepositoryPort;

    public BlogPostApplicationService(BlogPostRepositoryPort blogPostRepositoryPort,
                                     AuthorRepositoryPort authorRepositoryPort) {
        this.blogPostRepositoryPort = blogPostRepositoryPort;
        this.authorRepositoryPort = authorRepositoryPort;
    }

    @Override
    public BlogPost createBlogPost(CreateBlogPostCommand command) {
        // Verify author exists
        authorRepositoryPort.findById(command.authorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + command.authorId().value()));

        // Create blog post
        BlogPost blogPost = BlogPost.createDraft(
                command.title(),
                command.content(),
                command.summary(),
                command.authorId()
        );

        // Add tags if provided
        if (command.tagNames() != null && !command.tagNames().isEmpty()) {
            BlogPost finalBlogPost = blogPost;
            Set<Tag> tags = command.tagNames().stream()
                    .map(Tag::create)
                    .collect(Collectors.toSet());

            for (Tag tag : tags) {
                finalBlogPost = finalBlogPost.addTag(tag);
            }
            blogPost = finalBlogPost;
        }

        return blogPostRepositoryPort.save(blogPost);
    }

    @Override
    public BlogPost getById(PostId postId) {
        return blogPostRepositoryPort.findById(postId)
                .orElseThrow(() -> new BlogPostNotFoundException("Blog post not found: " + postId.value()));
    }

    @Override
    public BlogPost getBySlug(Slug slug) {
        return blogPostRepositoryPort.findBySlug(slug)
                .orElseThrow(() -> new BlogPostNotFoundException("Blog post not found: " + slug.value()));
    }

    @Override
    public List<BlogPost> getAllPublished() {
        return blogPostRepositoryPort.findByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<BlogPost> getAllByAuthor(AuthorId authorId) {
        return blogPostRepositoryPort.findByAuthorId(authorId);
    }

    @Override
    public BlogPost publishPost(PostId postId) {
        BlogPost blogPost = getById(postId);
        BlogPost publishedPost = blogPost.publish();
        return blogPostRepositoryPort.save(publishedPost);
    }
}
