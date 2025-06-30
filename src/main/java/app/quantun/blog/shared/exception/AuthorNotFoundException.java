package app.quantun.blog.shared.exception;

public class AuthorNotFoundException extends RuntimeException  {
    public AuthorNotFoundException(String message) {
        super(message);
    }
}
