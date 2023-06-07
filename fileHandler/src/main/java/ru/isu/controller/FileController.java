package ru.isu.controller;

import name.dmaus.schxslt.SchematronException;
import org.springframework.web.bind.annotation.RestController;
import ru.isu.model.DocType;
import ru.isu.model.Files;
import ru.isu.model.Node;
import ru.isu.model.db.OnePackageFile;
import ru.isu.model.db.SemdPackage;
import ru.isu.model.enums.Type;

import java.io.IOException;
import java.util.List;

import static ru.isu.model.enums.Type.*;

@RestController
public class FileController {
    Files files = new Files();

    final String DESCR_GET_XML = "Файл <b>xml</b> успешно загружен! ";
    final String DESCR_CHECK = "\n\n<b>Команды:</b>\n\n" +
            "/checkXML - проверка xml-документа на соответствие пакету спецификации\n" +
            "/checkXML_body - проверка <b>тела</b> xml-документа на соответствие схематрону";
    final String DESCR_ANS = "<b>Результат проверки</b>\n\n";
    /**
     * Bot send user all semds list
     *
     * @return all semds
     */
    public String getAllSemds(List<SemdPackage> list) {
        String answer = "";
        if (list.isEmpty()) {
            return "Список пакетов спецификации пустой!";
        }
        for (SemdPackage s : list) {
            answer += s.getCode() + ". " + s.getName() + "("+s.getStringDate()+")\n";
        }
        return "<b>Список доступных пакетов спецификации</b>\n\n"+answer;
    }

    /**
     * Templates for check is exist, check existing user file (xml)
     * @param body full document or only body
     * @return result of checking or error message
     * @throws SchematronException exception
     */
    public String readyToChecking(boolean body, String semdCode, String chatId) throws SchematronException {
        String answer = DESCR_ANS;
        if (body) {
            answer+=files.checkBodyBySchematron(semdCode, chatId);
        } else {
            answer+=files.checkDocByPackage(semdCode, chatId);
        }
        try {
            files.deleteFolder(semdCode);
            if (body) {
                files.deleteFile(chatId+"/"+chatId+"_b.xml");
            } else {
                files.deleteFile(chatId+"/"+chatId+".xml");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return answer;
    }

    public Type getDocType(String mimeType, String docPath) {
        System.out.println("path="+docPath);
        switch (mimeType) {
            case "text/xml" -> {return XML;}
            case "application/octet-stream" -> {
                if (docPath.contains(".xsd")) {
                    return XSD;
                } else {
                    return SCH;
                }
            }
            case "application/zip" -> {
                return ZIP;
            }
        }
        return OTHER;
    }

    public SemdPackage getZip(DocType docType) {
        try {
            return files.unpackZip(docType);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new SemdPackage();
    }

    public String getXml(String chatId, String text) {
        files.setChatID(chatId);
        DocType docType = new DocType("", text, XML);
        files.saveNewFile(docType);

        files.setCurrentSEMDcode(Node.getAttributeValue(chatId+"/"+chatId+".xml", "ClinicalDocument/code/@code"));
        return DESCR_GET_XML + "\nТекущий код СЭМД = " + files.getCurrentSEMDcode() + ". \n" + DESCR_CHECK;
    }

    public String addFileToSemdFiles(String filename, String path, Type type) {
        //files.setChatID(chatId);
        files.saveNewFile(new DocType(filename, path, type));

        return "Файл " + filename + "."+type.getName()+" успешно сохранен!";
    }

    public String getSemdCode(String chatId) {
        return Node.getAttributeValue(chatId+"/"+chatId+".xml", "ClinicalDocument/code/@code");
    }

    public String getListFiles(List<OnePackageFile> list) {
        String answer = "";
        for (OnePackageFile f : list) {
            //answer += s.substring(s.indexOf(" "), s.indexOf("}")) + "\n";
            answer += f.getId() + "\n";
        }
        if (answer.isEmpty()) {
            return "Список пакетов спецификации пустой!";
        }
        return answer;
    }

    public List<String> getFilesFromZip() {
        //System.out.println(list.toString());
        return files.getPathList();
    }

    public void clearZipContent(String semdCode) {
        try {
            files.deleteFolder(semdCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        files.getPathList().clear();
    }
}
