package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.application.command.port.in.AddCommentCommand;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CommentRequest;
import app.quantun.blog.infrastructure.adapter.in.web.contract.response.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final AddCommentCommand addCommentCommand;

    public CommentController(AddCommentCommand addCommentCommand) {
        this.addCommentCommand = addCommentCommand;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable String postId,
                                                      @Valid @RequestBody CommentRequest request) {
        var command = AddCommentCommand.AddCommentCommandData.builder()
                .postId(PostId.of(postId))
                .content(request.content())
                .authorName(request.authorName())
                .authorEmail(request.authorEmail())
                .build();

        var comment = addCommentCommand.addComment(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommentResponse.fromDomain(comment));
    }
}
