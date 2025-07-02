package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.application.port.in.CreateAuthorUseCase;
import app.quantun.blog.application.port.in.GetAuthorUseCase;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.infrastructure.adapter.in.web.contract.response.AuthorResponse;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CreateAuthorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final CreateAuthorUseCase createAuthorUseCase;
    private final GetAuthorUseCase getAuthorUseCase;

    public AuthorController(CreateAuthorUseCase createAuthorUseCase,
                            GetAuthorUseCase getAuthorUseCase) {
        this.createAuthorUseCase = createAuthorUseCase;
        this.getAuthorUseCase = getAuthorUseCase;
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        var command = CreateAuthorUseCase.CreateAuthorCommand.builder()
                .name(request.name())
                .email(request.email())
                .bio(request.bio())
                .build();

        var author = createAuthorUseCase.createAuthor(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthorResponse.fromDomain(author));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthor(@PathVariable String id) {
        var author = getAuthorUseCase.getAuthorById(AuthorId.of(id));
        return ResponseEntity.ok(AuthorResponse.fromDomain(author));
    }
}