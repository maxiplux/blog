package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.port.in.CreateBlogPostUseCase;
import app.quantun.blog.domain.port.in.GetBlogPostUseCase;
import app.quantun.blog.domain.port.in.PublishBlogPostUseCase;
import app.quantun.blog.infrastructure.adapter.in.web.contract.request.CreateBlogPostRequest;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlogPostController.class)
@AutoConfigureMockMvc
@Import(BlogPostControllerTest.TestConfig.class)
class BlogPostControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public CreateBlogPostUseCase createBlogPostUseCase() {
            return Mockito.mock(CreateBlogPostUseCase.class);
        }

        @Bean
        public GetBlogPostUseCase getBlogPostUseCase() {
            return Mockito.mock(GetBlogPostUseCase.class);
        }

        @Bean
        public PublishBlogPostUseCase publishBlogPostUseCase() {
            return Mockito.mock(PublishBlogPostUseCase.class);
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
                createBlogPostUseCase(),
                getBlogPostUseCase(),
                publishBlogPostUseCase()
            );
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateBlogPostUseCase createBlogPostUseCase;

    @Autowired
    private GetBlogPostUseCase getBlogPostUseCase;

    @Autowired
    private PublishBlogPostUseCase publishBlogPostUseCase;

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

        BlogPost createdPost = BlogPost.createDraft(title, content, summary, authorId);
        when(createBlogPostUseCase.createBlogPost(any(CreateBlogPostUseCase.CreateBlogPostCommand.class)))
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
        PostId postId = PostId.generate();
        BlogPost blogPost = createSampleBlogPost(postId);

        when(getBlogPostUseCase.getById(postId)).thenReturn(blogPost);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", postId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postId.value())))
                .andExpect(jsonPath("$.title", is(blogPost.getTitle())))
                .andExpect(jsonPath("$.content", is(blogPost.getContent())))
                .andExpect(jsonPath("$.status", is(blogPost.getStatus().name())));
    }

    @Test
    void shouldReturnNotFoundWhenBlogPostDoesNotExist() throws Exception {
        // Arrange
        PostId postId = PostId.generate();
        when(getBlogPostUseCase.getById(postId))
                .thenThrow(new BlogPostNotFoundException("Blog post not found: " + postId.value()));

        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", postId.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(postId.value())));
    }

    @Test
    void shouldGetBlogPostBySlug() throws Exception {
        // Arrange
        Slug slug = Slug.fromTitle("test-post");
        BlogPost blogPost = createSampleBlogPost(PostId.generate());

        when(getBlogPostUseCase.getBySlug(slug)).thenReturn(blogPost);

        // Act & Assert
        mockMvc.perform(get("/api/posts/slug/{slug}", slug.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(blogPost.getId().value())))
                .andExpect(jsonPath("$.title", is(blogPost.getTitle())))
                .andExpect(jsonPath("$.slug", is(slug.value())));
    }

    @Test
    void shouldGetAllPublishedPosts() throws Exception {
        // Arrange
        List<BlogPost> publishedPosts = List.of(
                createSampleBlogPost(PostId.generate()),
                createSampleBlogPost(PostId.generate())
        );

        when(getBlogPostUseCase.getAllPublished()).thenReturn(publishedPosts);

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(publishedPosts.get(0).getId().value())))
                .andExpect(jsonPath("$[1].id", is(publishedPosts.get(1).getId().value())));
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
                .authorId("author-123")
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
                .authorId("author-123")
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();

        when(publishBlogPostUseCase.publishPost(postId)).thenReturn(publishedPost);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{id}/publish", postId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postId.value())))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
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
                .authorId("author-123")
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();
    }
}
