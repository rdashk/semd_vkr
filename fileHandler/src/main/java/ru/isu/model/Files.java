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

    List<String> pathList = new ArrayList<>();
    private String currentSEMDcode = "";
    private String chatID = "";
    private SEMDvalidator validator;
    private Stax stax;
    String ADD_SCHEMATRON = "\nДобавьте схематрон в выбранный СЭМД." +
            "\nДля этого отправьте схематрон в формате sch в чат.";

    /**
     * Checking xml file using shema and schematron
     * @return string result of checking
     */
    public String getAnswer(boolean body) {

        if (!chatID.isEmpty() && !currentSEMDcode.isEmpty()) {

            this.validator = new SEMDvalidator();
            this.stax = new Stax();

            File f = new File(getChatID()+"/errors/");
            f.mkdir();

            if (body) {
                if (fileIsExist(getCurrentSEMDcode()+"/schematron.sch")) {
                    return checkBodyBySchematron();
                }
                return "Проверка тела документа невозможна! В СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;
            }
            List<String> errorsShema = validator.resultOfSchemaChecking(
                    getCurrentSEMDcode() + "/" + "CDA.xsd",
                    getChatID() + "/" + getChatID()+".xml");
            if (fileIsExist(getCurrentSEMDcode()+"/schematron.sch")) {
                List<String> errorsSchematron = validator.resultOfSchematronChecking(getCurrentSEMDcode()+"/schematron.sch",
                        getChatID() + "/" + getChatID()+".xml");
                if (errorsShema.isEmpty() && errorsSchematron.isEmpty()) {
                    return "Файл соответствует схемам и схематрону";
                }
                if (errorsShema.isEmpty()) {
                    stax.errorsSchematronFilesCreate(errorsSchematron, getChatID()+"/errors/errors_schematron",
                            getChatID() + "/" + getChatID()+".xml");
                    return "Файл соответствует схемам.\n\nВ схематроне найдены ошибки!";
                }
                stax.errorsSchemaFileCreate(errorsShema, getChatID()+"/errors/errors_shema");
                return "Файл соответствует схематрону.\n\nВ схемах найдены ошибки!";

            }
            if (errorsShema.isEmpty()) {
                return "Файл соответствует схемам. \n\nФайл схематрона отсутствует.\n"+ADD_SCHEMATRON;
            }
            stax.errorsSchemaFileCreate(errorsShema, getChatID()+"/errors/errors_shema");
            return "В схемах найдены ошибки!\nВ СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;

        }
        return "Файлы отсутствуют! \nЗагрузите xml-документ для начала работы.";
    }

    private String checkBodyBySchematron() {

        String xmlFile = getChatID() + "/" + getChatID()+".xml";
        String schFile = getCurrentSEMDcode()+"/schematron.sch";
        stax.saveBody(xmlFile, getChatID()+"/"+getChatID()+"_b", Type.XML);
        stax.saveBody(schFile, getChatID()+"/schematron_b", Type.SCH);

        List<String> errorsSchematron = validator.resultOfSchematronChecking(
                getChatID()+"/schematron_b.sch",
                getChatID()+"/"+getChatID()+"_b.xml");
        if (errorsSchematron == null) {
            return "Тело xml-документа соответствует телу схематрона";
        }
        stax.errorsSchematronFilesCreate(errorsSchematron, "/errors/errors_body", getChatID()+"/"+getChatID()+".xml");
        return "В теле документа найдены ошибки!";
    }


    /**
     * Reading from link and writing file to system directory
     * Save xml and sch file names to object (with extension)
     * @param docType file link from telegram chat
     * @return file name in system directory
     */
    private String createFileFromURL(DocType docType) throws IOException {
        URL urlFile = new URL(docType.getFilePath());
        String file = docType.getFileName().isEmpty()? getChatID(): docType.getFileName();
        String fileName;
        switch (docType.getType()) {
            case XML -> fileName = getChatID() + "/" + file + ".xml";
            //case XSD -> fileName = getChatID() + "/" + file + ".xsd";
            case ZIP -> fileName = file + ".zip";
            case SCH -> fileName = getCurrentSEMDcode() + "/"+file+".sch";
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

                        pathList.add(filename);

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
}
