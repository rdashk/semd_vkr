package ru.isu.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import name.dmaus.schxslt.SchematronException;
import org.apache.commons.io.FileUtils;
import ru.isu.model.enums.Type;
import ru.isu.model.validation.SEMDvalidator;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    private String name_XML = "";
    private String currentSEMDcode = "";
    private List<DocType> listFiles = new ArrayList<>();
    private String chatID = "";
    private String schematron = "";
    private SEMDvalidator validator;
    private Stax stax;
    String ADD_SCHEMATRON = "\nДобавьте схематрон в выбранный СЭМД.\nДля этого отправьте схематрон в формате sch в чат." +
            "\nДля просмотра всех файлов в текущем СЭМД нажмите команду /listFiles";

    /**
     * Checking xml file using shema and schematron
     * @return string result of checking
     * @throws SchematronException
     */
    public String getAnswer(boolean body) throws SchematronException {

        if (!chatID.isEmpty() && !currentSEMDcode.isEmpty()) {

            this.validator = new SEMDvalidator();
            this.stax = new Stax();

            if (body) {
                if (!getSchematron().isEmpty()) {
                    return checkBodyBySchematron();
                }
                return "Проверка тела документа невозможна! В СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;
            }
            List<String> errorsShema = validator.resultOfSchemaChecking(
                    getCurrentSEMDcode() + "/"+getCurrentSEMDcode() + "/" + "CDA.xsd",
                    getChatID() + "/" + getName_XML());
            if (!getSchematron().isEmpty()) {
                List<String> errorsSchematron = validator.resultOfSchematronChecking(getCurrentSEMDcode() + "/" + getSchematron(), getChatID() + "/" + getName_XML());
                if (errorsShema.isEmpty() && errorsSchematron.isEmpty()) {
                    return "Файл соответствует схемам\n\n и схематрону";
                }
                if (errorsShema.isEmpty()) {
                    stax.errorsFilesCreate(errorsSchematron, getChatID()+"/"+"errors_schematron", getChatID() + "/" + getName_XML());
                    return "Файл соответствует схемам.\n\nВ схематроне найдены ошибки!";
                }
                stax.errorsFilesCreate(errorsShema, getChatID()+"/"+"errors_shema", getChatID() + "/" + getName_XML());
                return "Файл соответствует схематрону.\n\nВ схемах найдены ошибки!";

            }
            if (errorsShema.isEmpty()) {
                return "Файл соответствует схемам. \n\nФайл схематрона отсутствует."+ADD_SCHEMATRON;
            }
            stax.errorsFilesCreate(errorsShema, getChatID()+"/"+"errors_shema", getChatID() + "/" + getName_XML());
            return "В схемах найдены ошибки!\nВ СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;

        }
        return "Файлы отсутствуют! \nПроверьте наличие шаблонов и схематронов в загруженном архиве. \n\nДля проверки нажмите команду /listFiles";
    }

    private String checkBodyBySchematron() {
        try {
            deleteFile(getChatID()+"/"+getChatID()+".xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String xmlFile = getChatID() + "/" + getName_XML();
        String schFile = getCurrentSEMDcode() + "/" + getSchematron();
        stax.saveBody(xmlFile, getChatID()+"/"+getChatID(), Type.XML);
        stax.saveBody(schFile, getChatID()+"/"+getChatID(), Type.SCH);

        List<String> errorsSchematron = validator.resultOfSchematronChecking(getChatID()+"/"+getChatID()+".sch", getChatID()+"/"+getChatID()+".xml");
        if (errorsSchematron.isEmpty()) {
            return "Тело xml-документа соответствует телу схематрона";
        }
        stax.errorsFilesCreate(errorsSchematron, "errors_body", getChatID()+"/"+getChatID()+".xml");
        return "В теле документа найдены ошибки!";
    }


    /**
     * Reading from link and writing file to system directory
     *
     * @param docType file link from telegram chat
     * @return file name in system directory
     * @throws IOException
     */
    private String createFileFromURL(DocType docType) throws IOException {
        URL urlFile = new URL(docType.getFilePath());
        String file = docType.getFileName().isEmpty()?getChatID(): docType.getFileName();
        String fileName;
        switch (docType.getType()) {
            case XML -> fileName = getChatID() + "/" + file + ".xml";
            case XSD -> fileName = getChatID() + "/" + file + ".xsd";
            case ZIP -> fileName = file + ".zip";
            case SCH -> fileName = getCurrentSEMDcode() + "/" + file + ".sch";
            default -> throw new IllegalStateException("Unexpected value: " + docType.getType().toString());
        }
        File f = new File(fileName);
        FileUtils.copyURLToFile(urlFile, f);
        System.out.println("Create file: " + fileName);
        return fileName;
    }

    /**
     * Checking for .xml .xsd and .sch files
     *
     * @return is exists .xml .xsd and .sch files
     */
    public boolean haveAllFiles() {
        // if xml is exists
        if (new File(getChatID() + "/" + getName_XML()).exists()) {
            if (!listFiles.isEmpty()) return true;
            if (new File(getCurrentSEMDcode()).exists()) {
                // reading files
                //TODO: считывание всех файлов! Сейчас проверка по наличию схематрона
                return new File(getCurrentSEMDcode() + "/" + getSchematron()).exists();
            }
        }
        return !listFiles.isEmpty();
    }

    /**
     * Checking for exists file witch contains name in file name
     *
     * @param name file name
     * @return full file name
     */
    private String getFileFromName(String name) {
        for (DocType file : getListFiles()) {
            if (file.getFilePath().contains(name)) {
                return file.getFilePath();
            }
        }
        return "";
    }

    /**
     * Getting all file names in currentSEMDcode
     *
     * @return list of file names
     */
    public List<String> getFilesFromFolder() {
        List<String> list = new ArrayList<>();
        for (DocType f : this.listFiles) {
            System.out.println(f.getFilePath());
            list.add(f.getFilePath());
        }
        return list;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    /**
     * Unpacking archive and saving all files from it
     *
     * @return manage to do unpacking
     * @throws IOException
     */
    public boolean unpackZip(DocType docType) throws IOException {
        //deleteFolder(getChatID());
        System.out.println(docType.getFileName());
        String zipFolder = createFileFromURL(docType);
        setCurrentSEMDcode(docType.getFileName());

        InputStream is;
        ZipInputStream zis;

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
                if (ze.isDirectory()) {
                    File directPath = new File(getCurrentSEMDcode() + "/" + filename);
                    directPath.mkdirs();
                    //System.out.println("dir:  " + filename);
                } else if (!filename.contains("__MACOSX") && !filename.contains(".DS_Store") &&
                        !getFileType(filename).equals(Type.OTHER)) { // it's shema or schematron
                    //System.out.println("file:  " + filename);
                    Type fileType = getFileType(filename);


                    // add filename to list files
                    this.listFiles.add(new DocType(filename, fileType));

                    FileOutputStream fout = new FileOutputStream(getCurrentSEMDcode() + "/" + filename);
                    if (fileType.equals(Type.SCH)) {
                        //System.out.println("Schematron="+filename);
                        setSchematron(filename);
                    }

                    // reading and writing files
                    while ((count = zis.read(buffer)) != -1) {
                        byteArray.write(buffer, 0, count);
                        byte[] bytes = byteArray.toByteArray();
                        fout.write(bytes);
                        byteArray.reset();
                    }
                    fout.close();
                    zis.closeEntry();
                }
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        deleteFile(zipFolder);
        return true;
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
        return Type.OTHER;
    }

    public boolean listFilesIsEmpty() {
        return this.listFiles.isEmpty();
    }

    /**
     * Save file to currentSEMDcode
     *
     * @param docType class DocType
     */
    public void saveNewFile(DocType docType) {
        try {
            createFileFromURL(docType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveNewFolder(String folder) {
        File f = new File(folder);
        System.out.println("Create currentSEMDcode: " + folder);
    }

    public boolean FileIsExist(String fileName) {
        return new File(getCurrentSEMDcode() + "/" + fileName).exists();
    }

    /**
     * Deleting currentSEMDcode
     *
     * @throws IOException
     */
    public void deleteFolder(String folderName) throws IOException {
        if (new File(folderName).exists()) {
            this.listFiles.clear();
            FileUtils.forceDelete(new File(folderName));
        }
    }

    /**
     *
     * @param fileName
     * @throws IOException
     */
    public void deleteFile(String fileName) throws IOException {
        System.out.println("delete file = " +fileName);
        if (new File(fileName).exists()) {
            FileUtils.forceDelete(new File(fileName));
        }
    }
}
