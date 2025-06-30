package app.quantun.blog.shared.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import java.time.LocalDateTime;

@Getter
public abstract class AggregateRoot {
    @Id
    protected String id;

    @Version
    protected Long version;

    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    protected AggregateRoot() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
