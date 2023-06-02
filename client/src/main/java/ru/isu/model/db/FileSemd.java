package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*@Table(name = "files")
@Entity*/
@Document(collection = "files")
public class FileSemd {

    @Id
    @JsonProperty("id")
    String id;//path
    @JsonProperty("code")
    String code;
    @Column(columnDefinition = "bytea")
    @JsonProperty("content")
    byte[] content;
}
