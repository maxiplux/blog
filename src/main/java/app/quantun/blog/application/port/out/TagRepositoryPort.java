package app.quantun.blog.application.port.out;

import app.quantun.blog.domain.model.Tag;

import java.util.Optional;

public interface TagRepositoryPort {
    Tag save(Tag tag);
    Optional<Tag> findBySlug(String slug);
    boolean existsBySlug(String slug);
}