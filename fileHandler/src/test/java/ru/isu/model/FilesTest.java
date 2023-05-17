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

import static org.junit.Assert.assertEquals;

public class FilesTest {

    String chatId = "1092367005";
    String documentName = "/Users/rdashk/IdeaProjects/semd_vkr/1092367005/1092367005.xml";
    @Test
    public void getCode() {
        assertEquals(getAttributeValue(documentName, "ClinicalDocument[1]/code[1]/@code[1]"), "86");
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
            is = new FileInputStream("/Users/rdashk/IdeaProjects/semd_vkr/86.zip");
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
                    File directPath = new File(filename);
                    directPath.mkdirs();
                    //System.out.println("dir:  " + filename);
                } else if (!filename.contains("__MACOSX") && !filename.contains(".DS_Store") &&
                        !getFileType(filename).equals(Type.OTHER)) {

                    //System.out.println("file:  " + filename);

                    Type fileType = getFileType(filename);

                    // set SEMD title, not save txt file
                    if (fileType.equals(Type.TXT)) {

                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            //setCurrentSEMDtitle(new String(bytes, "UTF-8"));
                            byteArray.reset();
                        }
                        zis.closeEntry();

                    } else {// it's shema or schematron

                        if (fileType.equals(Type.SCH)) {
                            filename = semdFolder +"/schematron.sch";
                            System.out.println("Schematron="+filename);
                            //setSchematron(filename);
                        }

                        // reading and writing files
                        FileOutputStream fout = new FileOutputStream(filename);
                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            fout.write(bytes);
                            byteArray.reset();
                        }
                        fout.close();
                        zis.closeEntry();

                        // add filename to list files
                        //this.listFiles.add(new DocType(filename, fileType));
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

    @Test
    public void rightPath() {
        String s = "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}patientRole[1]";
        String[] arr = s.replace("[1]","").split("/Q");
        System.out.println(arr[3]);
        s = s.replace("Q{urn:hl7-org:v3}","");
        assertEquals(s,"/ClinicalDocument[1]/recordTarget[1]/patientRole[1]");
    }

}