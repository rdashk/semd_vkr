package ru.isu.model;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import ru.isu.model.enums.Type;

import javax.xml.stream.*;
import java.io.*;
import java.util.List;

public class Stax {

    private XMLStreamReader xmlr;
    private XMLOutputFactory output;
    private XMLStreamWriter writer;

    public boolean saveBody(String fileName, String nameForSave, Type type) {
        if (type.equals(Type.XML) || type.equals(Type.SCH)) {

            try {
                xmlr = XMLInputFactory.newInstance().createXMLStreamReader(fileName, new FileInputStream(fileName));
                output = XMLOutputFactory.newInstance();
                if (type.equals(Type.XML)) {
                    writer = output.createXMLStreamWriter(new FileWriter(nameForSave+".xml"));
                    writeClinicalDocumentAttr();

                } else {
                    writer = output.createXMLStreamWriter(new FileWriter(nameForSave+".sch"));
                    writeSchematronAttr();
                }
                StaxWrite();
            } catch (XMLStreamException | IOException e) {
                return false;
            }
        }
        return false;
    }

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

    private void writeSchematronAttr() {
        StaxReadToComment("У2");
        try {
            // Открываем XML-документ и Пишем корневой элемент schema
            writer.writeStartElement("schema");
            writer.writeAttribute("xmlns", "http://purl.oclc.org/dsdl/schematron");
            writer.writeAttribute("queryBinding", "xslt2");

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void StaxReadToTag(String tagName) {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (xmlr.getLocalName().equals(tagName)) {
                        System.out.println(xmlr.getLocalName());
                        break;
                    }
                }
            }

        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void StaxReadToComment(String commentName) {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                if (xmlr.getEventType() == XMLStreamConstants.COMMENT) {
                    if (xmlr.getText().trim().contains(commentName)) {
                        System.out.println(xmlr.getText());
                        break;
                    }
                }
            }

        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void StaxWrite() throws XMLStreamException {
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                switch (xmlr.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        //TODO need????
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
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        writer.writeCharacters(xmlr.getText());
                        break;
                    }
                    case XMLStreamConstants.COMMENT: {
                        writer.writeComment(xmlr.getText());
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        //TODO need????
                        //writer.setPrefix(xmlr.getPrefix(), xmlr.getLocalName());
                        writer.writeEndElement();
                        break;
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

    public void errorsFilesCreate(List<String> listErrors, String fileName, String currentXML) {
        BufferedWriter writer = null;

        String location, txtFile = fileName + ".txt", error_descr;
        System.out.println("list errors="+listErrors);


        try {
            writer = new BufferedWriter(new FileWriter(txtFile, true));
            xmlr = XMLInputFactory.newInstance().createXMLStreamReader("ii.xml", new FileInputStream("ii.xml"));

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

            for (String mes : listErrors) {

                location = "providerOrganization"; // error path
                error_descr = "Core06-1. Элемент //identity:Props должен иметь 1 элемент identity:Ogrnip.";

                writer.append(mes); // write error to .txt

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
                                        str.append(xmlr.getAttributePrefix(i))
                                                .append(":")
                                                .append(xmlr.getAttributeLocalName(i))
                                                .append("=\"")
                                                .append(xmlr.getAttributeValue(i)).append("\" ");
                                    }
                                }
                            }
                            str.append(">");

                            if (xmlr.getLocalName().equals(location)) {
                                System.out.println(xmlr.getName());

                                currentColor = BaseColor.PINK;
                                chunk = new Chunk("Строка №" + xmlr.getLocation().getLineNumber() +
                                        "\n"+error_descr+"\n", new Font(baseFont, 9, Font.NORMAL));
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
                            if (xmlr.getLocalName().equals(location)) {
                                currentColor = BaseColor.WHITE;
                            }
                            break;
                        }
                    }
                }
            }
            document.add(paragraph);
            document.close();
            writer.close();
        } catch (IOException | DocumentException e) {
            System.out.println(e.getMessage());
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
