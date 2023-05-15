package ru.isu.model;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.isu.model.enums.Type;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class FilesTest {
    @Test
    public void getCode() {
        assertEquals(getAttributeValue("/Users/rdashk/IdeaProjects/semd_vkr/1092367005/1092367005.xml", "ClinicalDocument/code/@code"), "6");
    }
    public String getAttributeValue(String documentName, String xPath) {

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

    @Test
    public void unpackZip() {
        String semdFolder = "86";

        InputStream is;
        ZipInputStream zis;

        try {
            is = new FileInputStream("/Users/rdashk/IdeaProjects/semd_vkr/test.zip");
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;

                String filename = ze.getName();
                if (filename.contains(" ")) {
                    filename = filename.replace(" ", "_");
                }
                if (ze.isDirectory()) {
                    File directPath = new File(semdFolder + "/" + filename);
                    directPath.mkdirs();
                    //System.out.println("dir:  " + filename);
                } else if (!filename.contains("__MACOSX") && !filename.contains(".DS_Store") &&
                        !getFileType(filename).equals(Type.OTHER)) {


                    Type fileType = getFileType(filename);

                    // set SEMD title
                    if (fileType.equals(Type.TXT)) {
                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            System.out.println(new String(bytes, "UTF-8"));
                            byteArray.reset();
                        }
                        zis.closeEntry();

                    }

                }
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Type getFileType(String filename) throws MalformedURLException {
        if (filename.contains(".xsd")) {
            return Type.XSD;
        }
        if (filename.contains(".sch")) {
            return Type.SCH;
        }
        if (filename.contains(".txt")) {
            return Type.TXT;
        }
        return Type.OTHER;
    }

}