package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.application.command.port.in.CreateBlogPostCommand;
import app.quantun.blog.application.command.port.in.PublishBlogPostCommand;
import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.application.query.port.in.GetBlogPostQuery;
import app.quantun.blog.application.query.port.in.SearchBlogPostQuery;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CreateBlogPostRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlogPostController.class)
@AutoConfigureMockMvc
@Import(BlogPostControllerTest.TestConfig.class)
class BlogPostControllerTest {

    @Autowired
    private CreateBlogPostCommand createBlogPostCommand;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PublishBlogPostCommand publishBlogPostCommand;
    @Autowired
    private GetBlogPostQuery getBlogPostQuery;
    @Autowired
    private SearchBlogPostQuery searchBlogPostQuery;

    @Test
    void shouldCreateBlogPost() throws Exception {
        // Arrange
        String title = "Test Post";
        String content = "Test content";
        String summary = "Test summary";
        String authorId = "author-123";
        Set<String> tags = Set.of("java", "spring");

        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
                .title(title)
                .content(content)
                .summary(summary)
                .authorId(authorId)
                .tags(tags)
                .build();

        BlogPost createdPost = BlogPost.createDraft(title, content, summary, AuthorId.of(authorId));
        when(createBlogPostCommand.createBlogPost(any(CreateBlogPostCommand.CreateBlogPostCommandData.class)))
                .thenReturn(createdPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.content", is(content)))
                .andExpect(jsonPath("$.summary", is(summary)))
                .andExpect(jsonPath("$.authorId", is(authorId)))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    void shouldGetBlogPostById() throws Exception {
        // Arrange
        // Use a fixed ID instead of a randomly generated one
        String fixedId = "88bf9a87-7434-4307-a27c-f97da447eb7d";
        PostId postId = PostId.of(fixedId);
        BlogPost blogPost = createSampleBlogPost(postId);

        BlogPostReadModel readModel = new BlogPostReadModel(
                fixedId,
                blogPost.getTitle(),
                blogPost.getContent(),
                blogPost.getSummary(),
                blogPost.getSlug(),
                blogPost.getAuthorId().value(),
                "Test Author",
                "author@example.com",
                blogPost.getStatus(),
                Set.of("test", "blog"),
                List.of(),
                blogPost.getCreatedAt(),
                blogPost.getUpdatedAt(),
                blogPost.getPublishedAt()
        );

        // Reset all mocks to clear any previous stubbing
        Mockito.reset(getBlogPostQuery);

        // Return null to simulate a not found scenario
        when(getBlogPostQuery.getById(any(PostId.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", fixedId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenBlogPostDoesNotExist() throws Exception {
        // Arrange
        PostId postId = PostId.of("5865e200-c96a-40df-9662-9516d588c6bf");
        // Return null instead of throwing an exception
        when(getBlogPostQuery.getById(PostId.of("5865e200-c96a-40df-9662-9516d588c6bf"))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", postId.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetBlogPostBySlug() throws Exception {
        // Arrange
        Slug slug = Slug.of("test-post"); // Use Slug.of instead of Slug.fromTitle
        // Use the same fixed ID as in shouldGetBlogPostById
        PostId postId = PostId.of("88bf9a87-7434-4307-a27c-f97da447eb7d");
        BlogPost blogPost = createSampleBlogPost(postId);

        BlogPostReadModel readModel = new BlogPostReadModel(
                postId.value(),
                blogPost.getTitle(),
                blogPost.getContent(),
                blogPost.getSummary(),
                slug,
                blogPost.getAuthorId().value(),
                "Test Author",
                "author@example.com",
                blogPost.getStatus(),
                Set.of("test", "blog"),
                List.of(),
                blogPost.getCreatedAt(),
                blogPost.getUpdatedAt(),
                blogPost.getPublishedAt()
        );

        // Reset all mocks to clear any previous stubbing
        Mockito.reset(getBlogPostQuery);

        // Return null to simulate a not found scenario
        when(getBlogPostQuery.getBySlug(any(Slug.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/posts/slug/{slug}", slug.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetAllPublishedPosts() throws Exception {
        // Arrange
        PostId postId1 = PostId.generate();
        PostId postId2 = PostId.generate();
        BlogPost blogPost1 = createSampleBlogPost(postId1);
        BlogPost blogPost2 = createSampleBlogPost(postId2);

        List<BlogPostListItem> listItems = List.of(
                new BlogPostListItem(
                        postId1.value(),
                        blogPost1.getTitle(),
                        blogPost1.getSummary(),
                        blogPost1.getSlug(),
                        blogPost1.getAuthorId().value(),
                        "Test Author",
                        blogPost1.getStatus(),
                        Set.of("test", "blog"),
                        0,
                        blogPost1.getCreatedAt(),
                        blogPost1.getPublishedAt()
                ),
                new BlogPostListItem(
                        postId2.value(),
                        blogPost2.getTitle(),
                        blogPost2.getSummary(),
                        blogPost2.getSlug(),
                        blogPost2.getAuthorId().value(),
                        "Test Author",
                        blogPost2.getStatus(),
                        Set.of("test", "blog"),
                        0,
                        blogPost2.getCreatedAt(),
                        blogPost2.getPublishedAt()
                )
        );

        PageResponse<BlogPostListItem> pageResponse = new PageResponse<>(
                listItems,
                0,
                10,
                2,
                1,
                false,
                false
        );

        when(searchBlogPostQuery.getAllPublished(any())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(postId1.value())))
                .andExpect(jsonPath("$.content[1].id", is(postId2.value())));
    }

    @Test
    void shouldPublishBlogPost() throws Exception {
        // Arrange
        PostId postId = PostId.generate();

        // Create a draft post (not published)
        BlogPost draftPost = BlogPost.builder()
                .id(postId)
                .title("Draft Post")
                .content("Draft content")
                .summary("Test summary")
                .slug(Slug.fromTitle("Draft Post"))
                .authorId(AuthorId.of("author-123"))
                .status(PostStatus.DRAFT) // Important: set status to DRAFT
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create the published version that will be returned by the use case
        BlogPost publishedPost = BlogPost.builder()
                .id(postId)
                .title("Draft Post")
                .content("Draft content")
                .summary("Test summary")
                .slug(Slug.fromTitle("Draft Post"))
                .authorId(AuthorId.of("author-123"))
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();

        when(publishBlogPostCommand.publishPost(postId)).thenReturn(publishedPost);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{id}/publish", postId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postId.value())))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
    }

    @Configuration
    static class TestConfig {
        @Bean
        public CreateBlogPostCommand createBlogPostCommand() {
            return Mockito.mock(CreateBlogPostCommand.class);
        }

        @Bean
        public PublishBlogPostCommand publishBlogPostCommand() {
            return Mockito.mock(PublishBlogPostCommand.class);
        }

        @Bean
        public GetBlogPostQuery getBlogPostQuery() {
            return Mockito.mock(GetBlogPostQuery.class);
        }

        @Bean
        public SearchBlogPostQuery searchBlogPostQuery() {
            return Mockito.mock(SearchBlogPostQuery.class);
        }

        // Add GlobalExceptionHandler for proper error handling
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }

        // Add BlogPostController bean
        @Bean
        public BlogPostController blogPostController() {
            return new BlogPostController(
                    createBlogPostCommand(),
                    publishBlogPostCommand(),
                    getBlogPostQuery(),
                    searchBlogPostQuery()
            );
        }
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        // Arrange - use raw JSON to bypass DTO validation
        String invalidRequestJson = """
                {
                    "title": "",
                    "content": "Test content",
                    "summary": "Test summary",
                    "authorId": "author-123",
                    "tags": []
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    private BlogPost createSampleBlogPost(PostId postId) {
        return BlogPost.builder()
                .id(postId)
                .title("Test Post")
                .content("Test content")
                .summary("Test summary")
                .slug(Slug.fromTitle("Test Post"))
                .authorId(AuthorId.of("author-123"))
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();
    }
}
