package app.quantun.blog.content.infrastructure.web;

import app.quantun.blog.content.application.usecase.*;
import app.quantun.blog.content.dto.ArticleResponse;
import app.quantun.blog.content.dto.ArticleSummaryResponse;
import app.quantun.blog.content.dto.CreateArticleRequest;
import app.quantun.blog.content.dto.UpdateArticleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final CreateArticleUseCase createArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final PublishArticleUseCase publishArticleUseCase;
    private final GetArticleUseCase getArticleUseCase;
    private final ListArticlesUseCase listArticlesUseCase;

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        ArticleResponse response = createArticleUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable String articleId,
            @Valid @RequestBody UpdateArticleRequest request,
            @RequestHeader("X-Author-Id") String authorId) {

        ArticleResponse response = updateArticleUseCase.execute(articleId, request, authorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{articleId}/publish")
    public ResponseEntity<ArticleResponse> publishArticle(
            @PathVariable String articleId,
            @RequestHeader("X-Author-Id") String authorId) {

        ArticleResponse response = publishArticleUseCase.execute(articleId, authorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable String articleId) {
        ArticleResponse response = getArticleUseCase.execute(articleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<ArticleResponse> getArticleBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "true") boolean incrementView) {

        ArticleResponse response = getArticleUseCase.executeBySlug(slug, incrementView);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ArticleSummaryResponse>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ArticleSummaryResponse> response = listArticlesUseCase.executePublished(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tag/{tag}")
    public ResponseEntity<List<ArticleSummaryResponse>> getArticlesByTag(@PathVariable String tag) {
        List<ArticleSummaryResponse> response = listArticlesUseCase.executeByTag(tag);
        return ResponseEntity.ok(response);
    }
}