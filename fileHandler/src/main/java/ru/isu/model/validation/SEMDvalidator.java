package ru.isu.model.validation;

import lombok.Getter;
import name.dmaus.schxslt.Result;
import name.dmaus.schxslt.Schematron;
import name.dmaus.schxslt.SchematronException;
import name.dmaus.schxslt.cli.Application;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SEMDvalidator implements SchematronValidator, ShemaValidator{

    List<String> errors;

    @Override
    public StreamSource getResourceAsStream(String resource) {
        return new StreamSource(getClass().getResourceAsStream(resource), resource);
    }

    @Override
    public boolean validateSchematron(String schPath, String xmlDocument) {
        Application app;
        Result r;

        try {
            app = new Application(new Schematron(getResourceAsStream(schPath)));
            r = app.execute(new File(xmlDocument));
            if (r.isValid()) {
                return true;
            }
            this.errors = r.getValidationMessages();

            /*
            BufferedWriter writer = new BufferedWriter(new FileWriter(ans, true));

            for (String mes: r.getValidationMessages()) {
                // mes = String.format("%s %s %s", element.getLocalName(), element.getAttribute("location"), element.getTextContent());
                int end1 = mes.indexOf(" ");
                String s1 = mes.substring(0, end1);
                mes = mes.substring(end1+1);
                end1 = mes.indexOf(" ");

                writer.append("element.getLocalName = "+s1+
                        "\nelement.getAttribute(\"location\") = "+mes.substring(0, end1)+
                        "\nelement.getTextContent() = "+mes.substring(end1+1)+
                        "\n----\n");
            }
            writer.close();*/
        } catch (SchematronException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public List<String> resultOfSchematronChecking(String schPath, String xmlDocument) {

        validateSchematron(schPath, xmlDocument);
        return getErrors();
    }

    @Override
    public boolean validateXMLSchema(String xsdPath, String xmlPath) {
        //System.out.println("xsd: "+xsdPath+"\nxml: "+xmlPath);
        errors = new ArrayList<>();
        try {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));

        } catch (IOException | SAXException e) {
            this.errors.add(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public List<String> resultOfSchemaChecking(String xsdPath, String xmlDocument) {
        validateXMLSchema(xsdPath, xmlDocument);
        return getErrors();
    }
}
