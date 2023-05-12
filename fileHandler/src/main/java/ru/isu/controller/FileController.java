package ru.isu.controller;

import name.dmaus.schxslt.SchematronException;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.model.DocType;
import ru.isu.model.Files;
import ru.isu.model.Node;
import ru.isu.model.enums.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.isu.model.enums.Type.*;
import static ru.isu.model.enums.Type.ZIP;

public class FileController {
    Files files = new Files();
    final String DESCR_BOT = "\n\nЭто бот для проверки XML-документа на соответствие СЭМД\n\n<b>Команды:</b>\n\n" +
            "/start - начало работы с ботом\n" +
            "/help - список команд\n" +
            "/listSEMD - список доступных СЭМД\n" +
            "/listFiles - список доступных файлов текущего СЭМД\n" +
            "/changeSEMD - смена СЭМД\n" +
            "/currentSEMD - текущий СЭМД\n" +
            "/checkXML - проверка xml-документа\n" +
            "/checkXML_body - проверки <b>тела</b> xml-документа\n";
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
    final String DESCR_SEMD = "Выберете СЭМД или загрузите новый архив\n\n<b>Команды:</b>\n\n" +
            "/listSEMD - список доступных СЭМД\n" +
            "/addNewSEMD - добавление нового СЭМД";


    public String getText(Update update) {
        String messageText = update.getMessage().getText().toString();
        
        switch (messageText) {
            case "/listFiles" -> {
                if (files.getCurrentSEMDcode().isEmpty()) {
                    return "CЭМД не выбран. \nВыбор СЭМД осуществляется автоматически " +
                            "при загрузке вашего xml-документа.\nДля просмотра списка доступных СЭМД команда /listSEMD " +
                            "\nили\n" + DESCR_ADD_ZIP;
                } else if (files.listFilesIsEmpty()) {
                    return "Папка с файлами пустая. " + DESCR_ADD_ZIP;
                } else {
                    return "<b>Список доступных файлов</b>\n\n" + toMessageString(files.getFilesFromFolder());
                }
            }

            case "/changeSEMD" -> {
                return DESCR_SEMD;
            }

            case "/addNewSEMD" -> {
                return DESCR_ADD_ZIP;
            }

            case "/listSEMD" -> {
                /**
                 * TODO: From bd get all SEMDs with codes and titles and data
                 * if semds aren't exist in DB send
                 * telegramBot.sendMessage(message.getChatId(), "СЭМД отсутствуют в системе. " + DESCR_ADD_ZIP);
                 */
                List<String> test_list = new ArrayList<>();
                test_list.add("SEMD_1");test_list.add("SEMD_2");test_list.add("SEMD_3");
                return "<b>Список доступных СЭМД</b>\n\n" + toMessageString(test_list);
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
                    return readyToChecking(update, false);
                } catch (SchematronException e) {
                    throw new RuntimeException(e);
                }
            }

            case "/checkXML_body" -> {
                try {
                    return readyToChecking(update, true);
                } catch (SchematronException e) {
                    throw new RuntimeException(e);
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
    public String toMessageString(List<String> list) {
        String answer = "";
        int i = 1;
        for (String f : list) {
            answer += (i++) + ". " + f + "\n";
        }
        return answer;
    }

    public String getDocument(Update update) {
        Document doc = update.getMessage().getDocument();
        System.out.println("doc name="+doc.getFileName());
        String fileName = "";
        String path = update.getMessage().getText();
        Type type = XML;
        switch (doc.getMimeType()) {
            case "text/xml" -> type = XML;
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
                // TODO: get SEMD name from DB using current SEMD code
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
     * @param update This object represents an incoming update.
     *               update1 = update2, if update1_id = update2_id.
     * @throws SchematronException
     */
    private String readyToChecking(Update update, boolean body) throws SchematronException {
        if (files.readyToChecking()) {
            if (body) {
                return DESCR_ANS+files.getAnswer(true);
            } else {
                return DESCR_ANS+files.getAnswer(false);
            }
            //TODO: delete customer files
            /*try {
                files.deleteFolder(files.getChatID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }
        
        if (files.getCurrentSEMDcode().isEmpty()) {
            return DESCR_ADD_XML;
        } else if (!files.FileIsExist(files.getCurrentSEMDcode()+"/"+files.getCurrentSEMDcode())) {
            return DESCR_ADD_ZIP;
        }
        return "Загрузите xml-документ и выберете СЭМД";
    }

}
