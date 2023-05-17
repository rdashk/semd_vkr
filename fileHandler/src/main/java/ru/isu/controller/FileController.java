package ru.isu.controller;

import name.dmaus.schxslt.SchematronException;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Document;
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
    final String DESCR_GET_ZIP = "Архив СЭМД успешно загружен!";
    final String DESCR_CHECK = "\n\n<b>Команды:</b>\n\n" +
            "/checkXML - выполнение проверки xml-документа на соответствие шаблонам и схематрону\n" +
            "/checkXML_body - выполнение проверки <b>тела</b> xml-документа на соответствие схематрону";
    final String DESCR_ANS = "<b>Результат проверки</b>\n\n";
    final String DESCR_SEMD = "CЭМД не выбран. \nВыбор СЭМД осуществляется автоматически " +
            "при загрузке вашего xml-документа.\nДля просмотра списка доступных СЭМД команда /listSEMD ";


    public String getText(Update update) {
        String messageText = update.getMessage().getText().toString();
        
        switch (messageText) {
            case "/listFiles" -> {
                if (files.getCurrentSEMDcode().isEmpty()) {
                    return DESCR_SEMD;
                } else {
                    // TODO: get files from DB
                    //return "<b>Список доступных файлов</b>\n\n" + toMessageString(list from DB);
                    return "<b>Список доступных файлов</b>\n\n";
                }
            }

            case "/currentSEMD" -> {
                if (!files.getCurrentSEMDcode().isEmpty() && !files.getCurrentSEMDtitle().isEmpty()) {
                    return "\nТекущий СЭМД =" + files.getCurrentSEMDtitle() +" (код "+files.getCurrentSEMDcode() + ").";
                }
                if (!files.getCurrentSEMDcode().isEmpty()) {
                    return "Текущий код СЭМД = " + files.getCurrentSEMDcode() + ".";
                }
                return DESCR_SEMD;
            }

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
                    // delete all users files from his folder
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

    public String getDocument(Update update) {
        Document doc = update.getMessage().getDocument();
        System.out.println("doc name="+doc.getFileName());
        String fileName = "";
        String path = update.getMessage().getText();
        Type type = XML;
        switch (doc.getMimeType()) {
            case "text/xml" -> {type = XML;}
            case "application/octet-stream" -> {
                if (path.contains(".xsd")) {
                    type = XSD;
                } else {
                    type = SCH;
                }
            }
            case "application/zip" -> {
                type = ZIP;
                fileName = doc.getFileName().substring(0, doc.getFileName().indexOf(".zip"));
            }
        }
        
        DocType docType = new DocType(fileName, path, type);
        String chatId = update.getMessage().getChatId().toString();

        files.setChatID(chatId);
        if (type == XML || type == SCH) {
            files.saveNewFile(docType);

            if (type.equals(SCH)) {
                return DESCR_GET_SCH;
            } else {
                files.setCurrentSEMDcode(Node.getAttributeValue(chatId+"/"+files.getName_XML(), "ClinicalDocument/code/@code"));
                // TODO: get SEMD folder from DB using current SEMD code
                // if (SEMD folder from DB isn't exist) return return DESCR_GET_XML + "\nТекущий код СЭМД = " + files.getCurrentSEMDcode() + ". \n" + DESCR_ADD_ZIP
                // else
                    return DESCR_GET_XML + "\nТекущий код СЭМД = " + files.getCurrentSEMDcode() + ". \n" + DESCR_CHECK;
            }
        }

        try {
            files.unpackZip(docType);
            //TODO: save to db
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return DESCR_GET_ZIP;
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
}
