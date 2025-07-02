package app.quantun.blog.application.service;

import app.quantun.blog.application.port.in.CreateAuthorUseCase.CreateAuthorCommand;
import app.quantun.blog.application.port.out.AuthorRepositoryPort;
import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorApplicationServiceTest {

    @Mock
    private AuthorRepositoryPort authorRepositoryPort;

    private AuthorApplicationService authorApplicationService;

    @BeforeEach
    void setUp() {
        authorApplicationService = new AuthorApplicationService(authorRepositoryPort);
    }

    // CreateAuthorUseCase tests

    @Test
    void shouldCreateAuthor() {
        // Arrange
        String name = "John Doe";
        String email = "john.doe@example.com";
        String bio = "Software developer";

        CreateAuthorCommand command = CreateAuthorCommand.builder()
                .name(name)
                .email(email)
                .bio(bio)
                .build();

        Email emailObj = Email.of(email);
        when(authorRepositoryPort.existsByEmail(emailObj)).thenReturn(false);

        Author savedAuthor = Author.create(name, emailObj, bio);
        when(authorRepositoryPort.save(any(Author.class))).thenReturn(savedAuthor);

        // Act
        Author result = authorApplicationService.createAuthor(command);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(emailObj, result.getEmail());
        assertEquals(bio, result.getBio());

        verify(authorRepositoryPort).existsByEmail(emailObj);
        verify(authorRepositoryPort).save(any(Author.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String name = "John Doe";
        String email = "existing@example.com";
        String bio = "Software developer";

        CreateAuthorCommand command = CreateAuthorCommand.builder()
                .name(name)
                .email(email)
                .bio(bio)
                .build();

        Email emailObj = Email.of(email);
        when(authorRepositoryPort.existsByEmail(emailObj)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authorApplicationService.createAuthor(command)
        );

        assertTrue(exception.getMessage().contains(email));
        verify(authorRepositoryPort).existsByEmail(emailObj);
        verify(authorRepositoryPort, never()).save(any(Author.class));
    }

    // GetAuthorUseCase tests

    @Test
    void shouldGetAuthorById() {
        // Arrange
        AuthorId authorId = AuthorId.of("author-123");
        Author author = mock(Author.class);
        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));

        // Act
        Author result = authorApplicationService.getAuthorById(authorId);

        // Assert
        assertNotNull(result);
        assertEquals(author, result);
        verify(authorRepositoryPort).findById(authorId);
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        // Arrange
        AuthorId authorId = AuthorId.of("non-existent-author");
        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        AuthorNotFoundException exception = assertThrows(
                AuthorNotFoundException.class,
                () -> authorApplicationService.getAuthorById(authorId)
        );

        assertTrue(exception.getMessage().contains(authorId.value()));
        verify(authorRepositoryPort).findById(authorId);
    }

    @Test
    void shouldValidateCreateAuthorCommandWithBlankName() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CreateAuthorCommand.builder()
                        .name("")
                        .email("test@example.com")
                        .bio("Bio")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Name cannot be null or empty"));
    }

    @Test
    void shouldValidateCreateAuthorCommandWithBlankEmail() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CreateAuthorCommand.builder()
                        .name("John Doe")
                        .email("")
                        .bio("Bio")
                        .build()
        );

        assertTrue(exception.getMessage().contains("Email cannot be null or empty"));
    }
}
