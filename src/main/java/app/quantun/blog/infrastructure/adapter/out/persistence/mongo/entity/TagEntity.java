package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tags")
public class TagEntity {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("slug")
    private String slug;
}
