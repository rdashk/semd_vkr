package ru.isu.controller;

import name.dmaus.schxslt.SchematronException;
import org.springframework.web.bind.annotation.RestController;
import ru.isu.model.DocType;
import ru.isu.model.Files;
import ru.isu.model.Node;
import ru.isu.model.db.Semd;
import ru.isu.model.enums.Type;

import java.io.IOException;
import java.util.List;

import static ru.isu.model.enums.Type.*;

@RestController
public class FileController {
    Files files = new Files();

    final String DESCR_ADD_XML = "\nЗагрузите файл в формате <b>xml</b>." +
            "\nПРОВЕРЯЙТЕ РАЗРЕШЕНИЕ ФАЙЛА ПЕРЕД ЗАГРУЗКОЙ!";
    final String DESCR_ADD_ZIP = "\nЗагрузите пакет спецификации СЭМД (имя архива = <b>КОД_СЭМД.zip</b>). В архиве обязательно наличие:" +
            "1) схем (<b>xsd</b>)" +
            "2) текстового документа с названием СЭМД." +
            "\nДля проверки на соответствие схематрону - наличие схематрона (файл <b>sch</b>).";
    final String DESCR_GET_XML = "Файл <b>xml</b> успешно загружен! ";
    final String DESCR_CHECK = "\n\n<b>Команды:</b>\n\n" +
            "/checkXML - проверка xml-документа на соответствие пакету спецификации\n" +
            "/checkXML_body - проверка <b>тела</b> xml-документа на соответствие схематрону";
    final String DESCR_ANS = "<b>Результат проверки</b>\n\n";
    final String DESCR_SEMD = "Пакет спецификации не определен! \nВыбор нужного пакета осуществляется " +
            "при загрузке xml-документа.\nДля просмотра списка доступных пакетов спецификации - команда /listSEMD ";


    public String getText(String messageText) {

        switch (messageText) {
            case "/checkXML" -> {
                try {
                    return readyToChecking(false);
                } catch (SchematronException e) {
                    throw new RuntimeException(e);
                }
            }
            case "/checkXML_body" -> {
                try {
                    return readyToChecking(true);
                } catch (SchematronException e) {
                    throw new RuntimeException(e);
                }
            }
            case "/deleteMyXML" -> {
                String chatId = files.getChatID();
                if (chatId.isEmpty()) return "xml-документ отсутствует в системе!";
                try {
                    // delete all files from user folder
                    files.deleteFolder(chatId);
                    return "xml-документ успешно удален.";
                } catch (IOException e) {
                    return "xml-документ отсутствует в системе!";
                }
            }
        }
        return "Выберете другую команду!";
    }

    /**
     * Bot send user all semds list
     *
     * @return all semds
     */
    public String getAllSemds(List<Semd> list) {
        String answer = "";
        if (list.isEmpty()) {
            return "Список СЭМД пустой!";
        }
        for (Semd s : list) {
            answer += s.getCode() + ". " + s.getName() + "("+s.getStringDate()+")\n";
        }
        return "<b>Список доступных пакетов спецификации</b>\n\n"+answer;
    }

    /**
     * Bot send user all semds list for table on web page
     *
     * @return all semds for add to the table
     */
    public String getAllSemdsForTable(List<Semd> list) {
        String answer = "";
        for (Semd s : list) {
            answer += s.getCode() + "}" + s.getName() + "}" + s.getStringDate()+"!";
        }
        return answer;
    }

    /**
     * User have all files and can get conformity check
     */
    private String readyToChecking(boolean body) throws SchematronException {
        String chatId = files.getChatID();
        
        if (files.getCurrentSEMDcode().isEmpty()) {
            return DESCR_ADD_XML;
        } else if (!files.fileIsExist(files.getCurrentSEMDcode())) {
            return DESCR_ADD_ZIP;
        }
        if (files.fileIsExist(chatId+"/"+chatId+".xml") && files.fileIsExist(files.getCurrentSEMDcode())) {
            if (body) {
                return DESCR_ANS+files.getAnswer(true);
            } else {
                return DESCR_ANS+files.getAnswer(false);
            }
        }
        return "Загрузите xml-документ!";
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

    public Semd getZip(DocType docType) {
        try {
            return files.unpackZip(docType);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new Semd();
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

    public String getSemdCode() {
        return files.getCurrentSEMDcode();
    }

    public String getListFiles(List<String> list) {
        String answer = "";
        for (String s : list) {
            answer += s.substring(s.indexOf(" "), s.indexOf("}")) + "\n";
        }
        if (answer.isEmpty()) {
            return "Список пакетов спецификации пустой!";
        }
        return "<b>Список файлов для СЭМД (код = "+getSemdCode()+")</b>\n\n"+answer;
    }

    public List<String> getFilesFromZip() {
        List<String> list = files.getPathList();
        //System.out.println(list.toString());
        return list;
    }

    public void clearFilesFromZip() {
        files.getPathList().clear();
    }
}
