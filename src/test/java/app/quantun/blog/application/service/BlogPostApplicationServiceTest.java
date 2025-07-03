package app.quantun.blog.application.service;

import app.quantun.blog.application.command.port.in.CreateBlogPostCommand;
import app.quantun.blog.application.command.port.in.PublishBlogPostCommand;
import app.quantun.blog.application.command.port.out.BlogPostCommandRepositoryPort;
import app.quantun.blog.application.query.port.in.GetBlogPostQuery;
import app.quantun.blog.application.query.port.in.SearchBlogPostQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test has been deprecated and replaced by:
 * - BlogPostCommandServiceTest
 * - BlogPostQueryServiceTest
 * <p>
 * These tests follow the CQRS pattern with separate command and query services.
 */
@ExtendWith(MockitoExtension.class)
class BlogPostApplicationServiceTest {

    @Mock
    private BlogPostCommandRepositoryPort commandRepository;

    @Mock
    private GetBlogPostQuery getBlogPostQuery;

    @Mock
    private SearchBlogPostQuery searchBlogPostQuery;

    @Mock
    private CreateBlogPostCommand createBlogPostCommand;

    @Mock
    private PublishBlogPostCommand publishBlogPostCommand;

    @Test
    void shouldMigrateToNewCQRSArchitecture() {
        // This test is a placeholder to indicate that the application has been migrated to CQRS
        // The actual tests are now in BlogPostCommandServiceTest and BlogPostQueryServiceTest
        assertTrue(true, "Migration to CQRS architecture is complete");
    }
}
