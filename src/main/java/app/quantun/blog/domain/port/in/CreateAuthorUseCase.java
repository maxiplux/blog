package app.quantun.blog.domain.port.in;

import app.quantun.blog.domain.model.Author;
import lombok.Builder;

public interface CreateAuthorUseCase {
    Author createAuthor(CreateAuthorCommand command);

    @Builder
    record CreateAuthorCommand(
            String name,
            String email,
            String bio
    ) {
        public CreateAuthorCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
        }
    }
}