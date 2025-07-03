package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.application.command.port.in.CreateAuthorCommand;
import app.quantun.blog.application.query.port.in.GetAuthorQuery;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CreateAuthorRequest;
import app.quantun.blog.infrastructure.adapter.in.web.contract.response.AuthorResponse;
import app.quantun.blog.shared.valueobject.Email;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final CreateAuthorCommand createAuthorCommand;
    private final GetAuthorQuery getAuthorQuery;

    public AuthorController(CreateAuthorCommand createAuthorCommand,
                            GetAuthorQuery getAuthorQuery) {
        this.createAuthorCommand = createAuthorCommand;
        this.getAuthorQuery = getAuthorQuery;
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        var command = CreateAuthorCommand.CreateAuthorCommandData.builder()
                .name(request.name())
                .email(Email.of(request.email()))
                .bio(request.bio())
                .build();

        var author = createAuthorCommand.createAuthor(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthorResponse.fromDomain(author));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthor(@PathVariable String id) {
        var author = getAuthorQuery.getAuthorById(AuthorId.of(id));
        return ResponseEntity.ok(AuthorResponse.fromDomain(author));
    }
}
