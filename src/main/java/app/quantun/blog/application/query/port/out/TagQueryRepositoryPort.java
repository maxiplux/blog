package app.quantun.blog.application.query.port.out;

import app.quantun.blog.domain.model.Tag;

import java.util.Optional;

public interface TagQueryRepositoryPort {
    Optional<Tag> findBySlug(String slug);

    boolean existsBySlug(String slug);
}