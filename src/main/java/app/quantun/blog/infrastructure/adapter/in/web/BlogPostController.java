package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.application.command.port.in.CreateBlogPostCommand;
import app.quantun.blog.application.command.port.in.PublishBlogPostCommand;
import app.quantun.blog.application.query.port.in.GetBlogPostQuery;
import app.quantun.blog.application.query.port.in.SearchBlogPostQuery;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CreateBlogPostRequest;
import app.quantun.blog.infrastructure.adapter.in.web.contract.response.BlogPostResponse;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para blog posts implementando CQRS
 * Separa claramente entre operaciones de comando (escritura) y query (lectura)
 */
@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    // Commands (Write operations)
    private final CreateBlogPostCommand createBlogPostCommand;
    private final PublishBlogPostCommand publishBlogPostCommand;

    // Queries (Read operations)
    private final GetBlogPostQuery getBlogPostQuery;
    private final SearchBlogPostQuery searchBlogPostQuery;

    public BlogPostController(CreateBlogPostCommand createBlogPostCommand,
                              PublishBlogPostCommand publishBlogPostCommand,
                              GetBlogPostQuery getBlogPostQuery,
                              SearchBlogPostQuery searchBlogPostQuery) {
        this.createBlogPostCommand = createBlogPostCommand;
        this.publishBlogPostCommand = publishBlogPostCommand;
        this.getBlogPostQuery = getBlogPostQuery;
        this.searchBlogPostQuery = searchBlogPostQuery;
    }

    // ===== COMMAND OPERATIONS (Write) =====

    @PostMapping
    public ResponseEntity<BlogPostResponse> createPost(@Valid @RequestBody CreateBlogPostRequest request) {
        var command = new CreateBlogPostCommand.CreateBlogPostCommandData(
                request.title(),
                request.content(),
                request.summary(),
                AuthorId.of(request.authorId()),
                request.tags()
        );

        var blogPost = createBlogPostCommand.createBlogPost(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BlogPostResponse.fromDomain(blogPost));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<BlogPostResponse> publishPost(@PathVariable String id) {
        var blogPost = publishBlogPostCommand.publishPost(PostId.of(id));
        return ResponseEntity.ok(BlogPostResponse.fromDomain(blogPost));
    }

    // ===== QUERY OPERATIONS (Read) =====

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getPost(@PathVariable String id) {
        var blogPostReadModel = getBlogPostQuery.getById(PostId.of(id));
        return ResponseEntity.ok(BlogPostResponse.fromReadModel(blogPostReadModel));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogPostResponse> getPostBySlug(@PathVariable String slug) {
        var blogPostReadModel = getBlogPostQuery.getBySlug(Slug.of(slug));
        return ResponseEntity.ok(BlogPostResponse.fromReadModel(blogPostReadModel));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BlogPostResponse>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") PageRequest.SortDirection sortDirection) {

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        var postsPage = searchBlogPostQuery.getAllPublished(pageRequest);

        var responsePage = new PageResponse<>(
                postsPage.content().stream()
                        .map(BlogPostResponse::fromListItem)
                        .toList(),
                postsPage.page(),
                postsPage.size(),
                postsPage.totalElements(),
                postsPage.totalPages(),
                postsPage.hasNext(),
                postsPage.hasPrevious()
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<PageResponse<BlogPostResponse>> getPostsByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") PageRequest.SortDirection sortDirection) {

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        var postsPage = searchBlogPostQuery.getAllByAuthor(AuthorId.of(authorId), pageRequest);

        var responsePage = new PageResponse<>(
                postsPage.content().stream()
                        .map(BlogPostResponse::fromListItem)
                        .toList(),
                postsPage.page(),
                postsPage.size(),
                postsPage.totalElements(),
                postsPage.totalPages(),
                postsPage.hasNext(),
                postsPage.hasPrevious()
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<BlogPostResponse>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") PageRequest.SortDirection sortDirection) {

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        var postsPage = searchBlogPostQuery.searchByTitleOrContent(q, pageRequest);

        var responsePage = new PageResponse<>(
                postsPage.content().stream()
                        .map(BlogPostResponse::fromListItem)
                        .toList(),
                postsPage.page(),
                postsPage.size(),
                postsPage.totalElements(),
                postsPage.totalPages(),
                postsPage.hasNext(),
                postsPage.hasPrevious()
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<PageResponse<BlogPostResponse>> getPostsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") PageRequest.SortDirection sortDirection) {

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        var postsPage = searchBlogPostQuery.getByTag(tagName, pageRequest);

        var responsePage = new PageResponse<>(
                postsPage.content().stream()
                        .map(BlogPostResponse::fromListItem)
                        .toList(),
                postsPage.page(),
                postsPage.size(),
                postsPage.totalElements(),
                postsPage.totalPages(),
                postsPage.hasNext(),
                postsPage.hasPrevious()
        );

        return ResponseEntity.ok(responsePage);
    }
}
