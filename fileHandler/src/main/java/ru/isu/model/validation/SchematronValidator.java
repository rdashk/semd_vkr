package ru.isu.model.validation;

import name.dmaus.schxslt.SchematronException;

import javax.xml.transform.stream.StreamSource;
import java.util.List;

public interface SchematronValidator {

    StreamSource getResourceAsStream (String resource);

    boolean validateSchematron(String schPath, String xmlDocument) throws SchematronException;

    List<String> resultOfSchematronChecking(String s, String xmlFile) throws SchematronException;
}
