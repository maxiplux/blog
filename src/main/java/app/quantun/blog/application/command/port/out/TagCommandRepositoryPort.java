package app.quantun.blog.application.command.port.out;

import app.quantun.blog.domain.model.Tag;

public interface TagCommandRepositoryPort {
    Tag save(Tag tag);
}