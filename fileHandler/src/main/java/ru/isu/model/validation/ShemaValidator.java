package ru.isu.model.validation;

import java.util.List;

public interface ShemaValidator {
    boolean validateXMLSchema(String xsdPath, String xmlPath);

    List<String> resultOfSchemaChecking(String s, String xmlFile);
}
