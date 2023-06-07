package ru.isu.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueFromJson {
    @JsonProperty("id")
    String id;//path
    @Column(columnDefinition = "bytea")
    @JsonProperty("content")
    byte[] content;
}
