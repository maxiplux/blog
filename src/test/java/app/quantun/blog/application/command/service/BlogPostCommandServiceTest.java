package app.quantun.blog.application.command.service;

import app.quantun.blog.application.command.port.in.CreateBlogPostCommand.CreateBlogPostCommandData;
import app.quantun.blog.application.command.port.in.UpdateBlogPostCommand.UpdateBlogPostCommandData;
import app.quantun.blog.application.command.port.out.BlogPostCommandRepositoryPort;
import app.quantun.blog.application.command.port.out.TagCommandRepositoryPort;
import app.quantun.blog.application.query.port.out.AuthorQueryRepositoryPort;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.application.query.port.out.TagQueryRepositoryPort;
import app.quantun.blog.domain.model.*;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para BlogPostCommandService
 * Enfocados en lógica de negocio y operaciones de escritura
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BlogPost Command Service")
class BlogPostCommandServiceTest {

    @Mock
    private BlogPostCommandRepositoryPort commandRepository;

    @Mock
    private BlogPostQueryRepositoryPort queryRepository;

    @Mock
    private AuthorQueryRepositoryPort authorQueryRepository;

    @Mock
    private TagCommandRepositoryPort tagCommandRepository;

    @Mock
    private TagQueryRepositoryPort tagQueryRepository;


    private BlogPostCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new BlogPostCommandService(
                commandRepository,
                queryRepository,
                authorQueryRepository,
                tagCommandRepository,
                tagQueryRepository

        );
    }

    @Nested
    @DisplayName("Create Blog Post Command")
    class CreateBlogPostCommandTest {

        @Test
        @DisplayName("Should create blog post with valid data")
        void shouldCreateBlogPostWithValidData() {
            // Given
            AuthorId authorId = AuthorId.generate();
            Author author = mock(Author.class);
            when(authorQueryRepository.findById(authorId)).thenReturn(Optional.of(author));
            when(queryRepository.existsBySlug(any(Slug.class))).thenReturn(false);

            CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                    "Test Blog Post",
                    "This is test content for the blog post",
                    "Test summary",
                    authorId,
                    Set.of("test", "blog")
            );

            Tag testTag = Tag.create("test");
            Tag blogTag = Tag.create("blog");
            when(tagQueryRepository.findBySlug("test")).thenReturn(Optional.of(testTag));
            when(tagQueryRepository.findBySlug("blog")).thenReturn(Optional.of(blogTag));

            BlogPost savedPost = BlogPost.createDraft(
                    command.title(),
                    command.content(),
                    command.summary(),
                    command.authorId()
            );
            when(commandRepository.save(any(BlogPost.class))).thenReturn(savedPost);

            // When
            BlogPost result = commandService.createBlogPost(command);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getTitle()).isEqualTo("Test Blog Post"),
                    () -> assertThat(result.getContent()).isEqualTo("This is test content for the blog post"),
                    () -> assertThat(result.getAuthorId()).isEqualTo(authorId),
                    () -> assertThat(result.getStatus()).isEqualTo(PostStatus.DRAFT)
            );

            verify(authorQueryRepository).findById(authorId);
            verify(queryRepository).existsBySlug(any(Slug.class));
            verify(commandRepository).save(any(BlogPost.class));
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void shouldThrowExceptionWhenAuthorNotFound() {
            // Given
            AuthorId nonExistentAuthorId = AuthorId.generate();
            when(authorQueryRepository.findById(nonExistentAuthorId)).thenReturn(Optional.empty());

            CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                    "Test Post",
                    "Test content",
                    "Test summary",
                    nonExistentAuthorId,
                    null
            );

            // When & Then
            assertThatThrownBy(() -> commandService.createBlogPost(command))
                    .isInstanceOf(AuthorNotFoundException.class)
                    .hasMessageContaining(nonExistentAuthorId.value());

            verify(authorQueryRepository).findById(nonExistentAuthorId);
            verify(commandRepository, never()).save(any(BlogPost.class));
        }

        @Test
        @DisplayName("Should generate unique slug when slug already exists")
        void shouldGenerateUniqueSlugWhenSlugAlreadyExists() {
            // Given
            AuthorId authorId = AuthorId.generate();
            Author author = mock(Author.class);
            when(authorQueryRepository.findById(authorId)).thenReturn(Optional.of(author));

            // First slug exists, second doesn't
            when(queryRepository.existsBySlug(Slug.fromTitle("Test Title")))
                    .thenReturn(true);
            when(queryRepository.existsBySlug(Slug.of("test-title-1")))
                    .thenReturn(false);

            CreateBlogPostCommandData command = new CreateBlogPostCommandData(
                    "Test Title",
                    "Test content",
                    "Test summary",
                    authorId,
                    null
            );

            BlogPost savedPost = BlogPost.createDraft(
                    command.title(),
                    command.content(),
                    command.summary(),
                    command.authorId()
            );
            when(commandRepository.save(any(BlogPost.class))).thenReturn(savedPost);

            // When
            BlogPost result = commandService.createBlogPost(command);

            // Then
            assertThat(result).isNotNull();

            // Verify unique slug generation was attempted
            verify(queryRepository).existsBySlug(Slug.fromTitle("Test Title"));
            verify(queryRepository).existsBySlug(Slug.of("test-title-1"));
        }
    }

    @Nested
    @DisplayName("Update Blog Post Command")
    class UpdateBlogPostCommandTest {

        @Test
        @DisplayName("Should update blog post with new content")
        void shouldUpdateBlogPostWithNewContent() {
            // Given
            PostId postId = PostId.generate();
            AuthorId authorId = AuthorId.generate();

            BlogPost existingPost = BlogPost.builder()
                    .id(postId)
                    .title("Old Title")
                    .content("Old content")
                    .summary("Old summary")
                    .slug(Slug.fromTitle("Old Title"))
                    .authorId(authorId)
                    .status(PostStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(queryRepository.findDomainById(postId)).thenReturn(Optional.of(existingPost));

            UpdateBlogPostCommandData command = new UpdateBlogPostCommandData(
                    postId,
                    "New Title",
                    "New content",
                    "New summary",
                    Set.of("new", "tags")
            );

            Tag newTag = Tag.create("new");
            Tag tagsTag = Tag.create("tags");
            when(tagQueryRepository.findBySlug("new")).thenReturn(Optional.of(newTag));
            when(tagQueryRepository.findBySlug("tags")).thenReturn(Optional.of(tagsTag));

            BlogPost updatedPost = existingPost.updateContent(
                    command.title(),
                    command.content(),
                    command.summary()
            );
            when(commandRepository.save(any(BlogPost.class))).thenReturn(updatedPost);

            // When
            BlogPost result = commandService.updateBlogPost(command);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> verify(queryRepository).findDomainById(postId),
                    () -> verify(commandRepository).save(any(BlogPost.class))
            );
        }

        @Test
        @DisplayName("Should throw exception when blog post not found for update")
        void shouldThrowExceptionWhenBlogPostNotFoundForUpdate() {
            // Given
            PostId nonExistentPostId = PostId.generate();
            when(queryRepository.findDomainById(nonExistentPostId)).thenReturn(Optional.empty());

            UpdateBlogPostCommandData command = new UpdateBlogPostCommandData(
                    nonExistentPostId,
                    "New Title",
                    "New content",
                    "New summary",
                    null
            );

            // When & Then
            assertThatThrownBy(() -> commandService.updateBlogPost(command))
                    .isInstanceOf(BlogPostNotFoundException.class)
                    .hasMessageContaining(nonExistentPostId.value());

            verify(queryRepository).findDomainById(nonExistentPostId);
            verify(commandRepository, never()).save(any(BlogPost.class));
        }
    }

    @Nested
    @DisplayName("Publish Blog Post Command")
    class PublishBlogPostCommandTest {

        @Test
        @DisplayName("Should publish draft blog post")
        void shouldPublishDraftBlogPost() {
            // Given
            PostId postId = PostId.generate();
            AuthorId authorId = AuthorId.generate();

            BlogPost draftPost = BlogPost.builder()
                    .id(postId)
                    .title("Draft Post")
                    .content("This is a draft post with content that is long enough to be published. It needs to be at least 100 characters long to pass validation.")
                    .summary("Draft summary")
                    .slug(Slug.fromTitle("Draft Post"))
                    .authorId(authorId)
                    .status(PostStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(queryRepository.findDomainById(postId)).thenReturn(Optional.of(draftPost));
            when(authorQueryRepository.findById(authorId)).thenReturn(Optional.of(mock(Author.class)));

            BlogPost publishedPost = draftPost.publish();
            when(commandRepository.save(any(BlogPost.class))).thenReturn(publishedPost);

            // When
            BlogPost result = commandService.publishPost(postId);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED),
                    () -> assertThat(result.getPublishedAt()).isNotNull()
            );

            verify(queryRepository).findDomainById(postId);
            verify(authorQueryRepository).findById(authorId);
            verify(commandRepository).save(any(BlogPost.class));

        }

        @Test
        @DisplayName("Should archive published blog post")
        void shouldArchivePublishedBlogPost() {
            // Given
            PostId postId = PostId.generate();
            AuthorId authorId = AuthorId.generate();

            BlogPost publishedPost = BlogPost.builder()
                    .id(postId)
                    .title("Published Post")
                    .content("Published content")
                    .summary("Published summary")
                    .slug(Slug.fromTitle("Published Post"))
                    .authorId(authorId)
                    .status(PostStatus.PUBLISHED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .publishedAt(LocalDateTime.now())
                    .build();

            when(queryRepository.findDomainById(postId)).thenReturn(Optional.of(publishedPost));

            BlogPost archivedPost = publishedPost.archive();
            when(commandRepository.save(any(BlogPost.class))).thenReturn(archivedPost);

            // When
            BlogPost result = commandService.archivePost(postId);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getStatus()).isEqualTo(PostStatus.ARCHIVED)
            );

            verify(queryRepository).findDomainById(postId);
            verify(commandRepository).save(any(BlogPost.class));
        }

        @Test
        @DisplayName("Should throw exception when blog post not found for publish")
        void shouldThrowExceptionWhenBlogPostNotFoundForPublish() {
            // Given
            PostId nonExistentPostId = PostId.generate();
            when(queryRepository.findDomainById(nonExistentPostId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commandService.publishPost(nonExistentPostId))
                    .isInstanceOf(BlogPostNotFoundException.class)
                    .hasMessageContaining(nonExistentPostId.value());

            verify(queryRepository).findDomainById(nonExistentPostId);
            verify(commandRepository, never()).save(any(BlogPost.class));
        }
    }
}
