package ru.isu.model;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import ru.isu.model.enums.Type;
import ru.isu.model.validation.Line;

import javax.xml.stream.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Class for read and write content xml-file
 */
public class Stax {

    private XMLStreamReader xmlr;

    private XMLOutputFactory output;
    private XMLStreamWriter writer;

    /**
     * Save body of XML-file or SCH-file to new doc
     *
     * @param fileName    file path
     * @param nameForSave path for new created file
     * @param type        file type (only xml or sch)
     */
    public void saveBody(String fileName, String nameForSave, Type type) {
        if (type.equals(Type.XML) || type.equals(Type.SCH)) {

            try {
                xmlr = XMLInputFactory.newInstance().createXMLStreamReader(fileName, new FileInputStream(fileName));
                output = XMLOutputFactory.newInstance();
                if (type.equals(Type.XML)) {
                    writer = output.createXMLStreamWriter(new FileWriter(nameForSave + ".xml"));
                    writeClinicalDocumentAttr();
                } else {
                    writer = output.createXMLStreamWriter(new FileWriter(nameForSave + ".sch"));
                    writeSchematronAttr();

                }
                StaxWrite();
            } catch (XMLStreamException | IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Writing attributes to XML doc
     */
    private void writeClinicalDocumentAttr() {
        String tagName = "component";
        StaxReadToTag(tagName);
        try {
            // Открываем XML-документ и Пишем корневой элемент ClinicalDocument
            //writer.writeStartDocument("1.0");
            writer.writeStartElement("ClinicalDocument");
            writer.writeAttribute("xmlns", "urn:hl7-org:v3");
            writer.writeAttribute("xmlns", "xsi", "xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("xmlns", "identity", "identity", "urn:hl7-ru:identity");
            writer.writeAttribute("xmlns", "address", "address", "urn:hl7-ru:address");
            writer.writeAttribute("xmlns", "medService", "medService", "urn:hl7-ru:medService");
            writer.writeAttribute("xmlns", "fias", "fias", "urn:hl7-ru:fias");

            writer.writeStartElement(tagName);

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writing attributes to SCH doc
     */
    private void writeSchematronAttr() {
        StaxReadToComment("У2");
        try {
            // Открываем XML-документ и Пишем корневой элемент schema
            writer.writeStartElement("schema");
            writer.writeAttribute("xmlns", "http://purl.oclc.org/dsdl/schematron");
            writer.writeAttribute("queryBinding", "xslt2");

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "xsi");
            writer.writeAttribute("uri", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeEndElement();

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "identity");
            writer.writeAttribute("uri", "urn:hl7-ru:identity");
            writer.writeEndElement();

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "address");
            writer.writeAttribute("uri", "urn:hl7-ru:address");
            writer.writeEndElement();

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "medService");
            writer.writeAttribute("uri", "urn:hl7-ru:medService");
            writer.writeEndElement();

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "PII");
            writer.writeAttribute("uri", "urn:hl7-ru:PII");
            writer.writeEndElement();

            writer.writeStartElement("ns");
            writer.writeAttribute("prefix", "fias");
            writer.writeAttribute("uri", "urn:hl7-ru:fias");
            writer.writeEndElement();

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read file and stop on tagName
     * @param tagName tag name
     */
    private void StaxReadToTag(String tagName) {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (xmlr.getLocalName().equals(tagName)) {
                        //System.out.println(xmlr.getLocalName());
                        break;
                    }
                }
            }

        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Read file and stop on next line than commentName
     * @param commentName comment name
     */
    private void StaxReadToComment(String commentName) {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                if (xmlr.getEventType() == XMLStreamConstants.COMMENT) {
                    if (xmlr.getText().trim().contains(commentName)) {
                        //System.out.println(xmlr.getText());
                        xmlr.next();
                        break;
                    }
                }
            }

        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Write content to xml or sch files
     * @throws XMLStreamException exception
     */
    private void StaxWrite() throws XMLStreamException {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                switch (xmlr.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        //writer.setPrefix(xmlr.getPrefix(), xmlr.getLocalName());
                        writer.writeStartElement(xmlr.getLocalName());
                        if (xmlr.getAttributeCount() > 0) {
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                if (xmlr.getAttributePrefix(i).isEmpty()) {
                                    writer.writeAttribute(xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                                } else {
                                    writer.writeAttribute(xmlr.getAttributePrefix(i), xmlr.getAttributeLocalName(i), xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                                }
                            }
                        }
                    }
                    case XMLStreamConstants.CHARACTERS -> {
                        writer.writeCharacters(xmlr.getText());
                    }
                    case XMLStreamConstants.COMMENT -> {
                        writer.writeComment(xmlr.getText());
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        //writer.setPrefix(xmlr.getPrefix(), xmlr.getLocalName());
                        writer.writeEndElement();
                    }
                }
            }

            // Закрываем XML-документ
            //writer.writeEndDocument();

            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create errors from schematron (pdf and txt)
     * @param listErrors list with errors description
     * @param fileName name for save pdf and txt files
     * @param currentXML xml path
     */
    public void errorsSchematronFilesCreate(List<String> listErrors, String fileName, String currentXML) {
        BufferedWriter writer = null;

        File txtFile = new File(fileName + ".txt");
        System.out.println("list errors=" + listErrors);


        String[] errors = new String[listErrors.size()];
        String[] descrErrors = new String[listErrors.size()];

        try {
            writer = new BufferedWriter(new FileWriter(txtFile, false));

            for (int i = 0; i < listErrors.size(); i++) {
                // mes = String.format("%s %s %s", element.getLocalName(), element.getAttribute("location"), element.getTextContent());
                String mes = listErrors.get(i);
                int end1 = mes.indexOf(" ");
                String s1 = mes.substring(0, end1);
                mes = mes.substring(end1 + 1);
                end1 = mes.indexOf(" ");

                writer.append(s1)
                        .append("\nlocation:    ")
                        .append(mes.substring(0, end1))
                        .append("\ndescription: ")
                        .append(mes.substring(end1 + 1))
                        .append("\n----\n");

                errors[i] = mes.substring(0, end1);
                descrErrors[i] = mes.substring(end1 + 1);

            }
            writer.close();

            /* TODO: delete this
            String ss1 = "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}patientRole[1]";
            String ss2 = "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}author[1]";
            errors = new String[]{ss1, ss2};
            descrErrors = new String[]{"ss1", "ss2"};*/

            xmlr = XMLInputFactory.newInstance().createXMLStreamReader(currentXML, new FileInputStream(currentXML));
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName + ".pdf"));
            document.open();

            // create own font with russian letters
            BaseFont baseFont = BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 9, Font.NORMAL);

            StringBuilder str;
            BaseColor currentColor = BaseColor.WHITE; // install background color
            Chunk chunk = new Chunk();
            Paragraph paragraph = new Paragraph();
            int ind = 0;
            Line line = finderLine(errors[ind], currentXML);

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
                                line = finderLine(errors[ind], currentXML);
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

    public Line finderLine(String path, String currentXML) {
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
                            //System.out.println(pathStr);

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

    /**
     * Create errors from schema (pdf and txt)
     * @param listErrors list with errors description
     * @param fileName name for save pdf and txt files
     */
    public void errorsSchemaFileCreate(List<String> listErrors, String fileName) {
        BufferedWriter writer = null;

        File txtFile = new File(fileName + ".txt");

        try {
            writer = new BufferedWriter(new FileWriter(txtFile, false));

            for (String s: listErrors) {

                writer.append(s+"\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
