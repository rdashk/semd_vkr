package ru.isu.model;

import com.google.common.primitives.Bytes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import ru.isu.model.db.OnePackageFile;
import ru.isu.model.db.SemdPackage;
import ru.isu.model.enums.Type;
import ru.isu.model.validation.SEMDvalidator;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class for working with files
 */
@NoArgsConstructor
@Getter
@Setter
public class Files {

    private SEMDvalidator validator;
    private Stax stax;
    String ADD_SCHEMATRON = "\nДобавьте схематрон в выбранный СЭМД." +
            "\nДля этого отправьте схематрон в формате sch в чат.";

    /**
     * Checking xml file using shema and schematron
     * @return string result of checking
     */
    public String checkDocByPackage(String semdCode, String chatId) {
            this.validator = new SEMDvalidator();
            this.stax = new Stax();

            File f = new File(chatId+"/errors/");
            f.mkdir();

            List<String> errorsShema = validator.resultOfSchemaChecking(
                    semdCode + "/" + "CDA.xsd",
                    chatId + "/" + chatId+".xml");
            if (fileIsExist(semdCode+"/schematron.sch")) {
                List<String> errorsSchematron = validator.resultOfSchematronChecking(semdCode+"/schematron.sch",
                        chatId + "/" + chatId+".xml");
                if (errorsShema.isEmpty() && errorsSchematron.isEmpty()) {
                    return "Файл соответствует схемам и схематрону";
                }
                if (errorsShema.isEmpty()) {
                    stax.errorsSchematronFilesCreate(errorsSchematron, chatId+"/errors/errors_schematron",
                            chatId + "/" + chatId+".xml");
                    return "Файл соответствует схемам.\n\nВ схематроне найдены ошибки!";
                }
                stax.errorsSchemaFileCreate(errorsShema, chatId+"/errors/errors_shema");
                return "Файл соответствует схематрону.\n\nВ схемах найдены ошибки!";

            }
            if (errorsShema.isEmpty()) {
                return "Файл соответствует схемам. \n\nФайл схематрона отсутствует.\n"+ADD_SCHEMATRON;
            }
            stax.errorsSchemaFileCreate(errorsShema, chatId+"/errors/errors_shema");
            return "В схемах найдены ошибки!\nВ СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;
    }

    /**
     * Checking body xml file using body schematron
     * @return string result of checking
     */
    public String checkBodyBySchematron(String semdCode, String chatId) {
        this.validator = new SEMDvalidator();
        this.stax = new Stax();

        File f = new File(chatId+"/errors/");
        f.mkdir();

        if (fileIsExist(semdCode+"/schematron.sch")) {
            String xmlFile = chatId + "/" + chatId+".xml";
            String schFile = semdCode+"/schematron.sch";
            stax.saveBody(xmlFile, chatId+"/"+chatId+"_b", Type.XML);
            stax.saveBody(schFile, chatId+"/schematron_b", Type.SCH);

            List<String> errorsSchematron = validator.resultOfSchematronChecking(
                    chatId+"/schematron_b.sch",
                    chatId+"/"+chatId+"_b.xml");
            if (errorsSchematron == null) {
                return "Тело xml-документа соответствует телу схематрона";
            }
            stax.errorsSchematronFilesCreate(errorsSchematron, "/errors/errors_body", chatId+"/"+chatId+".xml");
            return "В теле документа найдены ошибки!";

        }
        return "Проверка тела документа невозможна! В СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;
    }


    /**
     * Reading from link and writing file to system directory
     * Save xml and sch file names to object (with extension)
     * @param docType file link from telegram chat
     * @return file name in system directory
     */
    private String createFileFromURL(DocType docType) throws IOException {
        URL urlFile = new URL(docType.getFilePath());
        String newFolder = docType.getFileName();
        String fileName;
        switch (docType.getType()) {
            case XML -> fileName = newFolder + "/" + newFolder + ".xml";
            //case XSD -> fileName = newFolder + "/" + newFolder + ".xsd";
            case ZIP -> fileName = newFolder + ".zip";
            case SCH -> fileName = newFolder + "/schematron.sch";
            default -> throw new IllegalStateException("Unexpected value: " + docType.getType().toString());
        }
        File f = new File(fileName);
        FileUtils.copyURLToFile(urlFile, f);
        System.out.println("Create file: " + fileName);
        return fileName;
    }

    /**
     * Unpacking archive and saving all files from it
     *
     * @return manage to do unpacking
     * @throws IOException file exception
     */
    public SemdPackage unpackZip(DocType docType) throws IOException {
        String zipFolder = createFileFromURL(docType);
        String semdCode = docType.getFileName();
        String currentSemdTitle = "";

        File directPath = new File(semdCode);
        directPath.mkdirs();

        InputStream is;
        ZipInputStream zis;

        List<OnePackageFile> packageFileList = new ArrayList<>();

        try {
            is = new FileInputStream(zipFolder);
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
                if (!filename.contains("__MACOSX") && !filename.contains(".DS_Store") &&
                        !getFileType(filename).equals(Type.OTHER)) {

                    //System.out.println("file:  " + filename);

                    Type fileType = getFileType(filename);

                    // set SEMD title, not save txt file
                    if (fileType.equals(Type.TXT)) {

                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            //byte[] bytes = byteArray.toByteArray();
                            currentSemdTitle = byteArray.toString(StandardCharsets.UTF_8);
                            byteArray.reset();
                        }
                        zis.closeEntry();

                    }
                    else {// it's shema or schematron

                        if (fileType.equals(Type.SCH)) {
                            filename = semdCode +"/schematron.sch";
                            //System.out.println("Schematron="+filename);
                        }

                        // reading and writing files
                        //FileOutputStream fout = new FileOutputStream(filename);
                        byte[] result = new byte[0];
                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            result = Bytes.concat(result, bytes);
                            //fout.write(bytes);
                            byteArray.reset();
                        }
                        packageFileList.add(new OnePackageFile(filename, result));
                        //fout.close();
                        zis.closeEntry();

                    }
                }
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new SemdPackage();
        }
        deleteFolder(zipFolder);
        return new SemdPackage(semdCode, currentSemdTitle, new Date(), packageFileList);
    }

    /**
     * Getting file extension
     *
     * @param filename file name
     * @return file extension (class Type)
     * @throws MalformedURLException
     */
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

    /**
     * Save file to currentSEMDcode
     * @param docType class DocType
     */
    public void saveNewFile(DocType docType) {
        try {
            createFileFromURL(docType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean fileIsExist(String fileName) {
        return new File(fileName).exists();
    }

    /**
     * Deleting currentSEMDcode folder
     *
     * @throws IOException
     */
    public void deleteFolder(String folderName) throws IOException {
        if (new File(folderName).exists()) {
            FileUtils.forceDelete(new File(folderName));
        }
    }

    /**
     *
     * @param fileName file name
     * @throws IOException fileexception
     */
    public void deleteFile(String fileName) throws IOException {
        System.out.println("delete file = " +fileName);
        if (new File(fileName).exists()) {
            FileUtils.forceDelete(new File(fileName));
        }
    }

    public byte[] getByteContent(String filePath) {
        byte[] result = new byte[0];

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                byte[] bytes = line.getBytes();
                result = Bytes.concat(result, bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
