package ru.isu.model.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
@Entity
public class FileSemd {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    String path;
    Long code;
    @Column(columnDefinition = "bytea")
    byte[] content;

    public FileSemd(Long code, String path, byte[] bytes) {
        this.code = code;
        this.path = path;
        this.content = bytes;
    }
}
