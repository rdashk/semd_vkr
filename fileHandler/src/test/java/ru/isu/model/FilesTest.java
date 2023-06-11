package ru.isu.model;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.isu.model.enums.Type;
import ru.isu.model.validation.Line;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.xpath.*;
import java.io.*;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;

public class FilesTest {

    String chatId = "1092367005";
    String documentName = "/Users/rdashk/IdeaProjects/semd_vkr/1092367005/1092367005.xml";
    String currentXML = "/Users/rdashk/Downloads/isu/vkr/РЕЦЕПТ_НА_ЛЕКАРСТВЕННЫЙ_ПРЕПАРАТ/Я_mist(Рец на лек преп).xml";
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
    String s = "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}patientRole[1]";
    String s2 = "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}author[1]";

    @Test
    public void rightPath() {
        String[] arr = s.replace("[1]","").split("/Q");
        System.out.println(arr[3]);
        s = s.replace("Q{urn:hl7-org:v3}","");
        assertEquals(s,"/ClinicalDocument[1]/recordTarget[1]/patientRole[1]");
    }

    @Test
    public void getMonthAndYear() {
        Date date = new Date();
        SimpleDateFormat DateFor = new SimpleDateFormat("MM.yyyy");
        String stringDate= DateFor.format(date);
        assertEquals(stringDate, "05.2023");
    }

    private XMLStreamReader xmlr;
    private XMLOutputFactory output;
    private XMLStreamWriter writer;

    @Test
    public void errorsSchematronFilesCreate() {
        String[] errors = {s, s};
        String[] descrErrors = {"add", s};

        try {

            xmlr = XMLInputFactory.newInstance().createXMLStreamReader(currentXML, new FileInputStream(currentXML));
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfWriter.getInstance(document, new FileOutputStream("test.pdf"));
            document.open();

            // create own font with russian letters
            BaseFont baseFont = BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 9, Font.NORMAL);

            StringBuilder str;
            BaseColor currentColor = BaseColor.WHITE; // install background color
            Chunk chunk = new Chunk();
            Paragraph paragraph = new Paragraph();
            int ind = 0;
            Line line = finderLine(errors[ind]);

            // read .xml and write to pdf
            while (xmlr.hasNext()) {
                xmlr.next();
                switch (xmlr.getEventType()) { // choose xml element type
                    case XMLStreamConstants.START_ELEMENT: {
                        if (xmlr.getPrefix().isEmpty()) {
                            str = new StringBuilder(xmlr.getLocalName());
                        } else {
                            str = new StringBuilder(xmlr.getPrefix() + ":" + xmlr.getLocalName());
                        }
                        if (xmlr.getAttributeCount() > 0) {
                            str.append(" ");
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                if (xmlr.getAttributePrefix(i).isEmpty()) {
                                    str.append(xmlr.getAttributeLocalName(i)).append("=\"").append(xmlr.getAttributeValue(i)).append("\" ");
                                } else {
                                    str.append(xmlr.getAttributePrefix(i)).append(":").append(xmlr.getAttributeLocalName(i)).append("=\"").append(xmlr.getAttributeValue(i)).append("\" ");
                                }
                            }
                        }
                        str.append(">");

                        if (xmlr.getLocation().getLineNumber() == line.getStart()) {

                            currentColor = BaseColor.PINK;
                            // add mistake description and linenumber
                            chunk = new Chunk("Строка №" + line.getStart() +
                                    "\n" + descrErrors[ind] + "\n", new Font(baseFont, 9, Font.NORMAL));
                            chunk.getFont().setColor(BaseColor.RED);
                            paragraph.add(chunk);
                        }
                        chunk = new Chunk("<" + str.toString(), font);
                        chunk.setBackground(currentColor);
                        chunk.getFont().setColor(BaseColor.BLUE);
                        paragraph.add(chunk);
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        chunk = new Chunk(xmlr.getText(), new Font(baseFont, 9, Font.NORMAL));
                        chunk.setBackground(BaseColor.WHITE);
                        chunk.getFont().setColor(BaseColor.BLACK);
                        paragraph.add(chunk);
                        break;
                    }
                    case XMLStreamConstants.COMMENT: {
                        chunk = new Chunk("<!-- " + xmlr.getText().trim() + " -->", new Font(baseFont, 8, Font.ITALIC));
                        chunk.getFont().setColor(BaseColor.LIGHT_GRAY);
                        chunk.setBackground(BaseColor.WHITE);
                        paragraph.add(chunk);
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (xmlr.getPrefix().isEmpty()) {
                            chunk = new Chunk("</" + xmlr.getLocalName() + ">", font);
                        } else {
                            chunk = new Chunk("</" + xmlr.getPrefix() + ":" + xmlr.getLocalName() + ">", font);
                        }
                        chunk.setBackground(currentColor);
                        chunk.getFont().setColor(BaseColor.BLUE);
                        paragraph.add(chunk);
                        if (xmlr.getLocation().getLineNumber() == line.getEnd()) {
                            currentColor = BaseColor.WHITE;
                            ind++;
                            if (ind < errors.length) {
                                line = finderLine(errors[ind]);
                            }
                        }
                        break;
                    }
                }
            }

            document.add(paragraph);
            document.close();

        } catch (IOException | DocumentException e) {
            System.out.println(e.getMessage());
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Line finderLine(String path) {
        String needPath = path.replace("[1]", "").replace("Q", "");
        int start = 0;
        System.out.println(needPath);

        String[] errors = needPath.substring(1).split("/");
        String pathStr = "/";
        System.out.println(Arrays.toString(errors));

        try {

            XMLStreamReader xmlr2 = XMLInputFactory.newInstance().createXMLStreamReader(currentXML, new FileInputStream(currentXML));
            int ind = 0;

            // read .xml and write to pdf
            while (xmlr2.hasNext()) {
                xmlr2.next();
                switch (xmlr2.getEventType()) { // choose xml element type
                    case XMLStreamConstants.START_ELEMENT: {
                        if (ind < errors.length && xmlr2.getName().toString().equals(errors[ind])) {
                            pathStr += xmlr2.getName();
                            if (pathStr.equals(needPath)) {
                                start = xmlr2.getLocation().getLineNumber();
                            } else {
                                pathStr += "/";
                            }
                            System.out.println(pathStr);

                            ind++;
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (xmlr2.getName().toString().equals(errors[errors.length-1])) {
                            return new Line(start, xmlr2.getLocation().getLineNumber());
                        }
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        return new Line();
    }


}