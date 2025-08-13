package main;


import code.CodeTool;
import helper.StringUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utils.FileUtil;
import utils.PropertiesManager;

/**
 * @author Administrator
 * @date
 */
public class CreateAllAndMoveJson {

    /**
     * excel临时文件的起始文件名
     */
    public static final String TEMPORARY_EXCEL_FILE_START = "~$";

    /**
     * 所有支持的excel扩展文件名
     */
    public static final Set<String> EXCEL_FILE_EXTENSION = new HashSet<>();

    static {
        EXCEL_FILE_EXTENSION.add("xlsx");
        EXCEL_FILE_EXTENSION.add("xlsm");
        EXCEL_FILE_EXTENSION.add("xls");
    }

    public static void main(String[] args) {
        //拿到所有的文件
        File[] allFiles = listAllFile(new File(PropertiesManager.excelSourceFolder), EXCEL_FILE_EXTENSION);
        //分类
        if (allFiles == null || allFiles.length <= 0) {
            System.out.println("source excel files is null");
            return;
        }

        List<File> gameServerFiles = new ArrayList<>();
        List<File> crossServerFiles = new ArrayList<>();
        List<File> battleServerFiles = new ArrayList<>();
        for (File file : allFiles) {
            String fileName = file.getName();
            //跳过临时文件
            if (fileName.startsWith(TEMPORARY_EXCEL_FILE_START)) {
                System.out.println("=======================================================");
                System.out.println("file is temporary,skip generate file:" + fileName);
                System.out.println("=======================================================");
                continue;
            }

            String onlyName = fileName.substring(0, fileName.lastIndexOf("."));

            if (PropertiesManager.commonExcel.contains(onlyName)) {
                crossServerFiles.add(file);
                gameServerFiles.add(file);
                battleServerFiles.add(file);
            } else if (PropertiesManager.crossServerExcel.contains(onlyName)) {
                crossServerFiles.add(file);
            } else if (PropertiesManager.battleServerExcel.contains(onlyName)) {
                battleServerFiles.add(file);
            } else {
                if (PropertiesManager.CGExcelName.contains(onlyName)) {
                    crossServerFiles.add(file);
                }
                gameServerFiles.add(file);
            }

        }

        //生成GameServer
        createAndCopy(gameServerFiles);
        createAndCopy(crossServerFiles);
        createAndCopy(battleServerFiles);
    }

    private static void createAndCopy(List<File> files) {
        if (files == null) {
            System.out.println("main.MainFunction.createAndCopy， error param");
            return;
        }

        createAllConfig(files, true, false);
    }


    private static void moveFiles(File[] oldFiles, String targetFolder) {
        if (oldFiles == null || targetFolder == null) {
            System.out.println("main.MainFunction.moveFiles, error param");
            return;
        }
        File file = new File(targetFolder);
        if (file.isFile()) {
            System.out.println("main.MainFunction.moveFiles, error target path, path is a file");
            return;
        }

        if (!file.exists()) {
            file.mkdirs();
        }

        for (File oldFile : oldFiles) {
            copyFile(oldFile, targetFolder);
            //复制完成后删除
            oldFile.delete();
        }
    }


    private static void copyFile(File oldFile, String newPath) {
        if (!oldFile.isFile()) {
            return;
        }
        File newFile = new File(newPath + File.separator + oldFile.getName());
        if (newFile.exists()) {
            newFile.delete();
        }
        try {
            Files.copy(oldFile.toPath(), newFile.toPath());
        } catch (IOException e) {
            System.out.println("move file " + oldFile.getName() + " filed");
        }
    }

    private static File[] listAllFile(File file, Set<String> endWith) {
        if (file == null || file.isFile() || endWith == null) {
            System.out.println("main.MainFunction.listAllFile, error param");
            return null;
        }

        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isFile()) {
                    return false;
                }

                for (String end : endWith) {
                    if (pathname.getName().endsWith(end)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static void createAllConfig(List<File> files, boolean moveJson, boolean moveJava) {
        if (files == null) {
            return;
        }
        if (files.size() <= 0) {
            System.out.println("excel folder file is null, please check path");
            return;
        }

        for (File file : files) {
            String filName = file.getName();
            String filePath = file.getAbsolutePath();
            String fileNameNoEx = FileUtil.getFileNameNoEx(filName);
            CodeTool.createAllCode(filePath, fileNameNoEx, moveJson, moveJava);
        }
        System.out.println("create all config files finished, skip file list：" + SKIP_FILE.toString());

    }


    public static final Set<String> SKIP_FILE = new HashSet<>();

    public static void addSkipFile(String file) {
        if (StringUtils.isEmpty(file)) {
            return;
        }
        SKIP_FILE.add(file);
    }
}
