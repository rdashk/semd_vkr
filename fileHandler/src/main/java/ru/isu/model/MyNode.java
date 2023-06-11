package ru.isu.model;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class MyNode {

    /**
     * Getting attribute value from xml
     * @param documentName document name
     * @param xPath xPath
     * @return String attribute value
     * example using: getAttributeValue("file.xml", "ClinicalDocument/code/@code")
     */
    public static String getAttributeValue(String documentName, String xPath) {

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr;
        String value = "";
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(documentName);
            expr = xpath.compile(xPath);
            value = (String) expr.evaluate(document, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            //throw new RuntimeException(e);
            System.out.println("In method getAttributeValue\n"+e.getMessage());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
}
