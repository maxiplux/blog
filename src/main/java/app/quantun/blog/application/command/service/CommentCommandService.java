package app.quantun.blog.application.command.service;

import app.quantun.blog.application.command.port.in.AddCommentCommand;
import app.quantun.blog.application.command.port.out.CommentCommandRepositoryPort;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CommentCommandService implements AddCommentCommand {

    private final CommentCommandRepositoryPort commentCommandRepository;
    private final BlogPostQueryRepositoryPort blogPostQueryRepository;

    public CommentCommandService(CommentCommandRepositoryPort commentCommandRepository,
                                 BlogPostQueryRepositoryPort blogPostQueryRepository) {
        this.commentCommandRepository = commentCommandRepository;
        this.blogPostQueryRepository = blogPostQueryRepository;
    }

    @Override
    public Comment addComment(AddCommentCommandData command) {
        // Verify blog post exists and is published
        var blogPost = blogPostQueryRepository.findById(command.postId())
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

        return commentCommandRepository.save(comment);
    }
}
