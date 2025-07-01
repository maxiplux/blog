package app.quantun.blog.domain.service;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.port.in.CreateAuthorUseCase.CreateAuthorCommand;
import app.quantun.blog.domain.port.out.AuthorRepositoryPort;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepositoryPort authorRepositoryPort;

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorService = new AuthorService(authorRepositoryPort);
    }

    @Test
    void shouldCreateAuthor() {
        // Arrange
        String name = "John Doe";
        String emailStr = "john@example.com";
        String bio = "Test bio";

        CreateAuthorCommand command = CreateAuthorCommand.builder()
                .name(name)
                .email(emailStr)
                .bio(bio)
                .build();

        Email email = Email.of(emailStr);
        when(authorRepositoryPort.existsByEmail(email)).thenReturn(false);

        Author savedAuthor = Author.create(name, email, bio);
        when(authorRepositoryPort.save(any(Author.class))).thenReturn(savedAuthor);

        // Act
        Author result = authorService.createAuthor(command);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(bio, result.getBio());

        verify(authorRepositoryPort).existsByEmail(email);
        verify(authorRepositoryPort).save(any(Author.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String name = "John Doe";
        String emailStr = "john@example.com";
        String bio = "Test bio";

        CreateAuthorCommand command = CreateAuthorCommand.builder()
                .name(name)
                .email(emailStr)
                .bio(bio)
                .build();

        Email email = Email.of(emailStr);
        when(authorRepositoryPort.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(command)
        );

        assertTrue(exception.getMessage().contains(emailStr));
        verify(authorRepositoryPort).existsByEmail(email);
        verify(authorRepositoryPort, never()).save(any(Author.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange
        String name = "John Doe";
        String invalidEmail = "invalid-email";
        String bio = "Test bio";

        CreateAuthorCommand command = CreateAuthorCommand.builder()
                .name(name)
                .email(invalidEmail)
                .bio(bio)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(command)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(authorRepositoryPort, never()).existsByEmail(any(Email.class));
        verify(authorRepositoryPort, never()).save(any(Author.class));
    }

    @Test
    void shouldGetAuthorById() {
        // Arrange
        String authorId = "author-123";
        Author author = mock(Author.class);
        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));

        // Act
        Author result = authorService.getAuthorById(authorId);

        // Assert
        assertNotNull(result);
        assertEquals(author, result);
        verify(authorRepositoryPort).findById(authorId);
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        // Arrange
        String authorId = "non-existent-author";
        when(authorRepositoryPort.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        AuthorNotFoundException exception = assertThrows(
                AuthorNotFoundException.class,
                () -> authorService.getAuthorById(authorId)
        );

        assertTrue(exception.getMessage().contains(authorId));
        verify(authorRepositoryPort).findById(authorId);
    }
}