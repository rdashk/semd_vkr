package ru.isu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.isu.model.enums.Type;

@Data
@AllArgsConstructor
public class DocType {
    private String fileName = "";
    private String filePath;
    private Type type;

    public Type getType() {
        return type;
    }
}

