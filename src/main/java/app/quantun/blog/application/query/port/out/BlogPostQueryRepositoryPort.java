package app.quantun.blog.application.query.port.out;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;

import java.util.Optional;

public interface BlogPostQueryRepositoryPort {
    Optional<BlogPostReadModel> findById(PostId postId);

    Optional<BlogPostReadModel> findBySlug(Slug slug);

    Optional<BlogPost> findDomainById(PostId postId);

    Optional<BlogPost> findDomainBySlug(Slug slug);

    PageResponse<BlogPostListItem> findAllPublished(PageRequest pageRequest);

    PageResponse<BlogPostListItem> findByAuthorId(AuthorId authorId, PageRequest pageRequest);

    PageResponse<BlogPostListItem> findByTagName(String tagName, PageRequest pageRequest);

    PageResponse<BlogPostListItem> searchByTitleOrContent(String searchTerm, PageRequest pageRequest);

    boolean existsBySlug(Slug slug);

    long countPublishedPosts();

    long countPostsByAuthor(AuthorId authorId);
}
