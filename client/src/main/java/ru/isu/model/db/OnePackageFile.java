package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePackageFile {

    @Id
    @JsonProperty("id")
    String id;//path
    @Column(columnDefinition = "bytea")
    @JsonProperty("content")
    byte[] content;
}
