package app.quantun.blog.domain.service;



import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.port.in.AddCommentUseCase;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.domain.port.out.CommentRepositoryPort;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;

public class CommentService implements AddCommentUseCase {

    private final CommentRepositoryPort commentRepositoryPort;
    private final BlogPostRepositoryPort blogPostRepositoryPort;

    public CommentService(CommentRepositoryPort commentRepositoryPort,
                          BlogPostRepositoryPort blogPostRepositoryPort) {
        this.commentRepositoryPort = commentRepositoryPort;
        this.blogPostRepositoryPort = blogPostRepositoryPort;
    }

    @Override
    public Comment addComment(AddCommentCommand command) {
        // Verify blog post exists and is published
        var blogPost = blogPostRepositoryPort.findById(command.postId())
                .orElseThrow(() -> new BlogPostNotFoundException("Blog post not found: " + command.postId().value()));

        if (!blogPost.isPublished()) {
            throw new IllegalStateException("Cannot add comments to unpublished posts");
        }

        Comment comment = Comment.create(
                command.postId(),
                command.content(),
                command.authorName(),
                command.authorEmail()
        );

        return commentRepositoryPort.save(comment);
    }
}