package ru.isu.service;

import name.dmaus.schxslt.SchematronException;
import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isu.controller.FileController;
import ru.isu.model.Answer;
import ru.isu.model.DocType;
import ru.isu.model.db.FileSemd;
import ru.isu.model.db.Semd;
import ru.isu.model.enums.Type;
import ru.isu.repository.FileSemdRepository;
import ru.isu.repository.SemdRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ru.isu.model.RabbitQueue.*;

@Service
@Component
public class RecipientServiceImpl implements RecipientService {

    final String DESCR_GET_ZIP = "Архив СЭМД успешно загружен!";
    final String DESCR_ADD_ZIP = "\nЗагрузите архив СЭМД (имя архива = <b>КОД_СЭМД.zip</b>). " +
            "\nВ архиве обязательно наличие:" +
            "\n1) шаблонов(<b>xsd</b>)" +
            "\n2) текстового документа с названием СЭМД." +
            "\nДля проверки на соответствие схематрону - наличие файла(<b>sch</b>).";
    final String DESCR_SEMD = "CЭМД не выбран. \nВыбор СЭМД осуществляется автоматически " +
            "при загрузке вашего xml-документа.\nДля просмотра списка доступных СЭМД команда /listSEMD ";

    final String DESCR_ADD_XML = "\nЗагрузите файл в формате <b>xml</b>." +
            "\nПРОВЕРЯЙТЕ РАЗРЕШЕНИЕ ФАЙЛА ПЕРЕД ЗАГРУЗКОЙ!";

    private final SenderService sender;
    private final FileController fileController;
    @Autowired
    private SemdRepository semdRepository;
    @Autowired
    private FileSemdRepository fileSemdRepository;


    public RecipientServiceImpl(SenderService sender, FileController fileController) {
        this.sender = sender;
        this.fileController = fileController;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE)
    public void getTextMessage(Answer answer) {
        //System.out.println("RecipientServiceImpl:get text message");

        var chatId = answer.getChatId();
        String textToSend = "";
        String command = answer.getMessage();

        switch (command) {
            case "/listSEMD" -> textToSend = fileController.getAllSemds(semdRepository.findAll());
            case "/listFiles" -> {
                if (fileController.getSemdCode().isEmpty()) {
                    textToSend = DESCR_SEMD;
                } else {
                    textToSend = fileController.getListFiles(
                            fileSemdRepository.findFilesByCode(fileController.getSemdCode()));
                }
                // TODO: for future save
                byte[] bin = fileSemdRepository.findFileSemdById("77/CDA.xsd").getContent();
                System.out.println(new String(bin));
            }
            case "/currentSEMD" -> {
                if (fileController.getSemdCode().isEmpty()) {
                    textToSend = DESCR_SEMD;
                } else {
                    Semd semd = semdRepository.findSemdByCode(fileController.getSemdCode());
                    if (semd == null) {
                        textToSend = "\nВ базе данных отсутствует СЭМД с кодом = " + fileController.getSemdCode();
                    } else {
                        textToSend = "\nТекущий СЭМД =" + semd.getName() + " (код " + semd.getCode() + ").";
                    }
                }
            }
            case "/checkXML" -> {
                textToSend = checkAndGetAnswer(chatId+"/"+chatId+".xml", false);
            }
            case "/checkXML_body" -> {
                textToSend = checkAndGetAnswer(chatId+"/"+chatId+".xml", true);
            }
            default -> textToSend = "Выберете другую команду!";
        }

        // choose type massage
        if (command.contains("/checkXML")) {
            sender.send(VALID_MESSAGE, new Answer(chatId, textToSend));
        } else {
            sender.send(ANSWER_MESSAGE, new Answer(chatId, textToSend));
        }
    }

    private String checkAndGetAnswer(String xmlPath, boolean body) {
        if (!new File(xmlPath).exists()) {
            return DESCR_ADD_XML;
        }
        if (fileSemdRepository.existsFileSemdByCode(fileController.getSemdCode())) {
            try {
                downloadFilesToSystem(fileController.getSemdCode());
                return fileController.readyToChecking(body);
            } catch (SchematronException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return DESCR_ADD_ZIP;
    }

    private void downloadFilesToSystem(String code) throws IOException {
        List<FileSemd> listFiles = fileSemdRepository.findFiles(code);
        File directPath;
        for (FileSemd f: listFiles) {
            String filename = f.getId();
            if (filename.contains("/")) {
                String[] arr = filename.split("/");
                String path = arr[0]+"/";
                for (int i=1;i<arr.length-1;i++) {
                    path+=arr[i]+"/";
                }
                if (!new File(path).exists()) {
                    directPath = new File(path);
                    directPath.mkdirs();
                    //System.out.println("new dir:  " + path);
                }
            }
            OutputStream os = new FileOutputStream(filename);
            os.write(f.getContent());
            os.close();
        }
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE)
    public void getDocMessage(Update update) {
        //System.out.println("RecipientServiceImpl:get document");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        var chatId = message.getChatId().toString();
        sendMessage.setChatId(chatId);
        String textToSend;
        Type type = fileController.getDocType(message.getDocument().getMimeType(),
                message.getText());
        String semdCodeFromController = fileController.getSemdCode();
        switch (type) {
            case ZIP -> {
                Document doc = message.getDocument();
                Semd semd = fileController.getZip(
                        new DocType(doc.getFileName().substring(0, doc.getFileName().indexOf(".zip")),
                                message.getText(),
                                type));
                if (semd.getName().isEmpty()) {
                    textToSend = "Отсутствует файл с названием СЭМД!" + DESCR_ADD_ZIP;
                } else if (semd.equals(new Semd())) {
                    textToSend = "Произошла ошибка! Проверьте содержимое архива " +
                            "и загрузите его еще раз!" + DESCR_ADD_ZIP;
                } else {
                    textToSend = DESCR_GET_ZIP;
                    Semd semdFromTable = semdRepository.findSemdByCode(semd.getCode());
                    //TODO: change date difference
                    if (semdFromTable == null || semdFromTable.getDate().before(semd.getDate())) {
                        semdRepository.save(semd);
                    }
                    // save all files from folder to db
                    // TODO: builder?
                    for (String filePath : fileController.getFilesFromZip()) {
                        try {
                            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                            fileSemdRepository.save(new FileSemd(filePath, semd.getCode(), bytes));
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    fileController.clearZipContent();
                }
            }
            case XML -> textToSend = fileController.getXml(chatId, message.getText());
            case SCH -> {
                if (semdRepository.existsById(semdCodeFromController)) {
                    textToSend = fileController.addFileToSemdFiles("schematron", message.getText(), Type.SCH);
                    String path = semdCodeFromController+"/schematron.sch";
                    try {
                        fileSemdRepository.save(new FileSemd(path,semdCodeFromController, Files.readAllBytes(Paths.get(path))));
                    } catch (IOException e) {
                        System.out.println("SERVER.getDocMessage:"+e.getMessage());
                    }
                } else {
                    textToSend = "Невозможно сохранить файл! Отсутствует архив СЭМД("+semdCodeFromController+")\n"+DESCR_ADD_ZIP;
                }
            }
            default -> textToSend = "Ошибка!";
        }

        sender.send(ANSWER_MESSAGE, new Answer(chatId, textToSend));
    }
}
