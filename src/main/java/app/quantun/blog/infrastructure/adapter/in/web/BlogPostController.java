package app.quantun.blog.infrastructure.adapter.in.web;


import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.port.in.CreateBlogPostUseCase;
import app.quantun.blog.domain.port.in.GetBlogPostUseCase;
import app.quantun.blog.domain.port.in.PublishBlogPostUseCase;
import app.quantun.blog.infrastructure.adapter.in.web.dto.BlogPostResponse;
import app.quantun.blog.infrastructure.adapter.in.web.dto.CreateBlogPostRequest;
import app.quantun.blog.shared.valueobject.Slug;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    private final CreateBlogPostUseCase createBlogPostUseCase;
    private final GetBlogPostUseCase getBlogPostUseCase;
    private final PublishBlogPostUseCase publishBlogPostUseCase;

    public BlogPostController(CreateBlogPostUseCase createBlogPostUseCase,
                              GetBlogPostUseCase getBlogPostUseCase,
                              PublishBlogPostUseCase publishBlogPostUseCase) {
        this.createBlogPostUseCase = createBlogPostUseCase;
        this.getBlogPostUseCase = getBlogPostUseCase;
        this.publishBlogPostUseCase = publishBlogPostUseCase;
    }

    @PostMapping
    public ResponseEntity<BlogPostResponse> createPost(@Valid @RequestBody CreateBlogPostRequest request) {
        var command = new CreateBlogPostUseCase.CreateBlogPostCommand(
                request.title(),
                request.content(),
                request.summary(),
                request.authorId(),
                request.tags()
        );

        var blogPost = createBlogPostUseCase.createBlogPost(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BlogPostResponse.fromDomain(blogPost));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getPost(@PathVariable String id) {
        var blogPost = getBlogPostUseCase.getById(PostId.of(id));
        return ResponseEntity.ok(BlogPostResponse.fromDomain(blogPost));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogPostResponse> getPostBySlug(@PathVariable String slug) {
        var blogPost = getBlogPostUseCase.getBySlug(Slug.of(slug));
        return ResponseEntity.ok(BlogPostResponse.fromDomain(blogPost));
    }

    @GetMapping
    public ResponseEntity<List<BlogPostResponse>> getAllPublishedPosts() {
        var posts = getBlogPostUseCase.getAllPublished();
        var response = posts.stream()
                .map(BlogPostResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<BlogPostResponse> publishPost(@PathVariable String id) {
        var blogPost = publishBlogPostUseCase.publishPost(PostId.of(id));
        return ResponseEntity.ok(BlogPostResponse.fromDomain(blogPost));
    }
}
