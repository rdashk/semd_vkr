package ru.isu.controller;

import name.dmaus.schxslt.SchematronException;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    final String DESCR_ADD_ZIP = "\nЗагрузите архив СЭМД (имя архива = <b>КОД_СЭМД.zip</b>).В архиве обязательно наличие:" +
            "1) шаблов(<b>xsd</b>)" +
            "2) текстового документа с названием СЭМД." +
            "\nДля проверки на соответствие схематрону - наличие файла(<b>sch</b>).";
    final String DESCR_GET_XML = "Файл <b>xml</b> успешно загружен! ";
    final String DESCR_GET_SCH = "Файл <b>sch</b> успешно загружен!";
    final String DESCR_CHECK = "\n\n<b>Команды:</b>\n\n" +
            "/checkXML - выполнение проверки xml-документа на соответствие шаблонам и схематрону\n" +
            "/checkXML_body - выполнение проверки <b>тела</b> xml-документа на соответствие схематрону";
    final String DESCR_ANS = "<b>Результат проверки</b>\n\n";
    final String DESCR_SEMD = "CЭМД не выбран. \nВыбор СЭМД осуществляется автоматически " +
            "при загрузке вашего xml-документа.\nДля просмотра списка доступных СЭМД команда /listSEMD ";


    public String getText(Update update) {
        String messageText = update.getMessage().getText().toString();
        
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
     * For sending user list of files in user currentSEMDcode
     *
     * @return numbered list of file path
     */
    public String getListSemd(List<Semd> list) {
        String answer = "";
        for (Semd s : list) {
            answer += s.getCode() + ". " + s.getName() + "\n";
        }
        if (answer.isEmpty()) {
            return "Список СЭМД пустой!";
        }
        return "<b>Список доступных СЭМД</b>\n\n"+answer;
    }

    /**
     * User have all files and can get conformity check
     */
    private String readyToChecking(boolean body) throws SchematronException {
        if (files.readyToChecking()) {
            if (body) {
                return DESCR_ANS+files.getAnswer(true);
            } else {
                return DESCR_ANS+files.getAnswer(false);
            }
        }
        
        if (files.getCurrentSEMDcode().isEmpty()) {
            return DESCR_ADD_XML;
        } else if (!files.fileIsExist(files.getCurrentSEMDcode())) {
            return DESCR_ADD_ZIP;
        }
        return "Загрузите xml-документ и выберете СЭМД";
    }

    public Type getDocType(Message message) {
        Document doc = message.getDocument();
        System.out.println("doc name="+doc.getFileName()+"\npath="+message.getText());
        switch (doc.getMimeType()) {
            case "text/xml" -> {return XML;}
            case "application/octet-stream" -> {
                if (message.getText().contains(".xsd")) {
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

    public String getXml(Message message) {
        String chatId = message.getChatId().toString();
        files.setChatID(chatId);
        DocType docType = new DocType("", message.getText(), XML);
        files.saveNewFile(docType);

        files.setCurrentSEMDcode(Node.getAttributeValue(chatId+"/"+files.getName_XML(), "ClinicalDocument/code/@code"));
        return DESCR_GET_XML + "\nТекущий код СЭМД = " + files.getCurrentSEMDcode() + ". \n" + DESCR_CHECK;
    }

    //TODO: must save to db (not users folder)
    public String getSch(Message message) {
        String chatId = message.getChatId().toString();
        files.setChatID(chatId);
        DocType docType = new DocType("", message.getText(), SCH);
        files.saveNewFile(docType);

        return DESCR_GET_SCH;
    }

    public String getSemdCode() {
        return files.getCurrentSEMDcode();
    }

    public String getListFiles(List<String> list) {
        String answer = "";
        for (String s : list) {
            answer += getSemdCode() + ". " + s + "\n";
        }
        if (answer.isEmpty()) {
            return "Список СЭМД пустой!";
        }
        return "<b>Список файлов в СЭМД (код = "+getSemdCode()+")</b>\n\n"+answer;
    }

    public List<String> getFilesFromZip() {
        List<String> list = files.getPathList();
        //files.getPathList().clear();
        System.out.println(list.toString());
        return list;
    }
}
