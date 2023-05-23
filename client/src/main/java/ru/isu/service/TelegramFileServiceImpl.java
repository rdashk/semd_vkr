package ru.isu.service;

import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@Component
public class TelegramFileServiceImpl implements TelegramFileService {

    //@Value("${bot.username}")
    private String botToken = "6064600469:AAHqiUWVhQyc9VToZi2TZ35S0iEc5skGHZs";
    //@Value("${service.file_info.uri}")
    private String fileInfoUri = "https://api.telegram.org/bot{token}/getFile?file_id={fileId}";
    //@Value("${service.file_storage.uri}")
    private String fileStorageUri = "https://api.telegram.org/file/bot{token}/{filePath}";

    /**
     * @param update received message
     * @return object DocType for defining file type
     */
    public String checkTypeDoc(Update update) {
        Document doc = update.getMessage().getDocument();
        //System.out.println(doc.getMimeType());
        String s = processDoc(update);
        String fileName = doc.getFileName().substring(0, doc.getFileName().indexOf("."));
        switch (doc.getMimeType()) {
            case "application/octet-stream" -> {
                if (s.contains(".xsd")) {
                    return s;
                }
                if (s.contains(".sch")) {
                    return s;
                }
                return null;
            }
            case "text/xml" -> {
                return s;
            }
            case "application/zip" -> {
                // if zip folder has semd code
                if (doc.getFileName().matches("\\d*.zip")) {
                    return s;
                }
                return "wrong_name_zip";
            }
            default -> {
                return "";
            }
        }
    }


    /**
     * Call reading file method
     *
     * @param telegramMessage received message
     * @return file link for downloading
     */
    public String processDoc(Update telegramMessage) {
        Document telegramDoc = telegramMessage.getMessage().getDocument();
        String fileId = telegramDoc.getFileId();
        System.out.printf("For file(%s)  type = %s  ", telegramDoc.getFileName(), telegramDoc.getMimeType());
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            String filePath = getFilePath(response);
            byte[] fileInByte;
            try {
                //fileInByte = downloadFile(filePath);
                return downloadFile(filePath);
                //readFile(filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //return fileInByte.toString();
        } else {
            return "Something wrong!";
        }
    }

    // отбор определенных параметров для генерации ссылки на файл
    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    // получение ссылки на файл
    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);

        return restTemplate.exchange(fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                botToken,
                fileId
        );
    }

    // метод, где выводится ссылка для скачивания
    private String downloadFile(String filePath) throws Exception {
        String fullUri = fileStorageUri.replace("{token}", botToken)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            System.out.println("full uri: " + fullUri);
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }
        return fullUri;

        /* TODO: how to readFile big file?
        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new Exception(urlObj.toExternalForm(), e);
        }*/
    }

    // метод считывания файла
    private void readFile(String filePath) throws IOException {
        String fullUri = fileStorageUri.replace("{token}", botToken)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(urlObj.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            //System.out.println(inputLine);
            in.close();
    }

}
