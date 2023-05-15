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
    private String currentSEMDtitle = "";
    private List<DocType> listFiles = new ArrayList<>();
    private String chatID = "";
    private String schematron = "";
    private SEMDvalidator validator;
    private Stax stax;
    String ADD_SCHEMATRON = "\nДобавьте схематрон в выбранный СЭМД." +
            "\nДля этого отправьте схематрон в формате sch в чат.";

    /**
     * Checking xml file using shema and schematron
     * @return string result of checking
     * @throws SchematronException
     */
    public String getAnswer(boolean body) throws SchematronException {

        if (!chatID.isEmpty() && !currentSEMDcode.isEmpty()) {

            this.validator = new SEMDvalidator();
            this.stax = new Stax();

            File f = new File(getChatID()+"/errors/");
            f.mkdir();

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
                List<String> errorsSchematron = validator.resultOfSchematronChecking(getCurrentSEMDcode() +
                        "/" + getSchematron(), getChatID() + "/" + getName_XML());
                if (errorsShema.isEmpty() && errorsSchematron.isEmpty()) {
                    return "Файл соответствует схемам и схематрону";
                }
                if (errorsShema.isEmpty()) {
                    stax.errorsFilesCreate(errorsSchematron, getChatID()+"/errors/errors_schematron",
                            getChatID() + "/" + getName_XML());
                    return "Файл соответствует схемам.\n\nВ схематроне найдены ошибки!";
                }
                stax.errorsFilesCreate(errorsShema, getChatID()+"/errors/errors_shema", getChatID() +
                        "/" + getName_XML());
                return "Файл соответствует схематрону.\n\nВ схемах найдены ошибки!";

            }
            if (errorsShema.isEmpty()) {
                return "Файл соответствует схемам. \n\nФайл схематрона отсутствует."+ADD_SCHEMATRON;
            }
            stax.errorsFilesCreate(errorsShema, getChatID()+"/errors/errors_shema", getChatID() +
                    "/" + getName_XML());
            return "В схемах найдены ошибки!\nВ СЭМД отсутствует файл схематрона."+ADD_SCHEMATRON;

        }
        return "Файлы отсутствуют! \nЗагрузите xml-документ для начала работы.";
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
        stax.errorsFilesCreate(errorsSchematron, "/errors/errors_body", getChatID()+"/"+getChatID()+".xml");
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
        String file = docType.getFileName().isEmpty()?getChatID(): docType.getFileName();
        String fileName;
        switch (docType.getType()) {
            case XML -> fileName = getChatID() + "/" + file + ".xml";
            //case XSD -> fileName = getChatID() + "/" + file + ".xsd";
            case ZIP -> fileName = file + ".zip";
            case SCH -> fileName = getCurrentSEMDcode() + "/" + file + ".sch";
            default -> throw new IllegalStateException("Unexpected value: " + docType.getType().toString());
        }
        File f = new File(fileName);
        if (docType.getType().equals(Type.XML)) {
            setName_XML(file + ".xml");
        } else if (docType.getType().equals(Type.SCH)) {
            setSchematron(file + ".sch");
        }
        FileUtils.copyURLToFile(urlFile, f);
        System.out.println("Create file: " + fileName);
        return fileName;
    }

    /**
     * Checking for .xml .xsd and .sch files
     *
     * @return is exists .xml .xsd and .sch files
     */
    public boolean readyToChecking() {
        // if xml and semd is exists
        if (new File(getChatID() + "/" + getName_XML()).exists()) {
            if (new File(getCurrentSEMDcode() + "/" + getCurrentSEMDcode()).exists()) {
                return true;
            }
        }
        return false;
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
     * Unpacking archive and saving all files from it
     *
     * @return manage to do unpacking
     * @throws IOException
     */
    public boolean unpackZip(DocType docType) throws IOException {
        String zipFolder = createFileFromURL(docType);
        String semdFolder = docType.getFileName();

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
                    File directPath = new File(semdFolder + "/" + filename);
                    directPath.mkdirs();
                    //System.out.println("dir:  " + filename);
                } else if (!filename.contains("__MACOSX") && !filename.contains(".DS_Store") &&
                        !getFileType(filename).equals(Type.OTHER)) {

                    //System.out.println("file:  " + filename);

                    Type fileType = getFileType(filename);

                    // set SEMD title, not save txt file
                    if (fileType.equals(Type.TXT)) {

                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            setCurrentSEMDtitle(new String(bytes, "UTF-8"));
                            byteArray.reset();
                        }
                        zis.closeEntry();

                    } else {// it's shema or schematron

                        // reading and writing files
                        FileOutputStream fout = new FileOutputStream(semdFolder + "/" + filename);
                        while ((count = zis.read(buffer)) != -1) {
                            byteArray.write(buffer, 0, count);
                            byte[] bytes = byteArray.toByteArray();
                            fout.write(bytes);
                            byteArray.reset();
                        }
                        fout.close();
                        zis.closeEntry();

                        // add filename to list files
                        //this.listFiles.add(new DocType(filename, fileType));

                        if (fileType.equals(Type.SCH)) {
                            //System.out.println("Schematron="+filename);
                            setSchematron(filename);
                        }
                    }
                }
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        deleteFolder(zipFolder);
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
        if (filename.contains(".txt")) {
            return Type.TXT;
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

    public boolean FileIsExist(String fileName) {
        return new File(getCurrentSEMDcode() +
                "/" + fileName).exists();
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
