package app.quantun.blog.domain.service;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.domain.port.in.CreateBlogPostUseCase;
import app.quantun.blog.domain.port.in.GetBlogPostUseCase;
import app.quantun.blog.domain.port.in.PublishBlogPostUseCase;
import app.quantun.blog.domain.port.out.AuthorRepositoryPort;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlogPostService implements CreateBlogPostUseCase, GetBlogPostUseCase, PublishBlogPostUseCase {

    private final BlogPostRepositoryPort blogPostRepositoryPort;
    private final AuthorRepositoryPort authorRepositoryPort;

    public BlogPostService(BlogPostRepositoryPort blogPostRepositoryPort,
                           AuthorRepositoryPort authorRepositoryPort) {
        this.blogPostRepositoryPort = blogPostRepositoryPort;
        this.authorRepositoryPort = authorRepositoryPort;
    }

    @Override
    public BlogPost createBlogPost(CreateBlogPostCommand command) {
        // Verify author exists
        authorRepositoryPort.findById(command.authorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + command.authorId()));

        // Create blog post
        BlogPost blogPost = BlogPost.createDraft(
                command.title(),
                command.content(),
                command.summary(),
                command.authorId()
        );

        // Add tags
        if (command.tagNames() != null) {
            Set<Tag> tags = command.tagNames().stream()
                    .map(Tag::create)
                    .collect(Collectors.toSet());

            tags.forEach(blogPost::addTag);
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
    public List<BlogPost> getAllByAuthor(String authorId) {
        return blogPostRepositoryPort.findByAuthorId(authorId);
    }

    @Override
    public BlogPost publishPost(PostId postId) {
        BlogPost blogPost = getById(postId);
        blogPost.publish();
        return blogPostRepositoryPort.save(blogPost);
    }
}
