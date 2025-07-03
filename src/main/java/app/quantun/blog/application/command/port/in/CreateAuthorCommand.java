package app.quantun.blog.application.command.port.in;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.shared.valueobject.Email;
import lombok.Builder;

public interface CreateAuthorCommand {
    Author createAuthor(CreateAuthorCommandData command);

    @Builder
    record CreateAuthorCommandData(
            String name,
            Email email,
            String bio
    ) {
        public CreateAuthorCommandData {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            if (email == null) {
                throw new IllegalArgumentException("Email cannot be null");
            }
            if (bio == null) {
                throw new IllegalArgumentException("Bio cannot be null");
            }
        }
    }
}
