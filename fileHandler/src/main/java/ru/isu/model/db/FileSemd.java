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

    public FileSemd(Long code, String path) {
        this.code = code;
        this.path = path;
    }
}
