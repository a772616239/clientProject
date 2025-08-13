package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {

    public static void copyFile(File oldFile, String newPath) {
        if (!oldFile.isFile()) {
            return;
        }

        File file = new File(newPath);
        if (!file.exists() || !file.isFile()) {
            file.mkdirs();
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

    public static void copyFile(String className, String jsonPath, String javaPath, String objectName, boolean moveJson, boolean moveJava) {
        if (className == null || jsonPath == null || javaPath == null || objectName == null || (!moveJson && !moveJava)) {
            System.out.println("copyFile(java.lang.String, java.lang.String, java.lang.String, boolean, boolean), error params");
            return;
        }

        File jsonFile = new File(jsonPath);
        File classFile = new File(javaPath);
        File objFile = new File(objectName);

        if (jsonFile.isDirectory() || classFile.isDirectory() || objFile.isDirectory()) {
            System.out.println("error fileName," + jsonPath + ", " + javaPath + ", " + objectName);
            return;
        }

        if (PropertiesManager.commonExcel.contains(className)) {
            if (moveJson) {
                copyFile(jsonFile, PropertiesManager.crossServerJsonFolder);
                copyFile(jsonFile, PropertiesManager.gameServerJsonFolder);
                copyFile(jsonFile, PropertiesManager.battleServerJsonFolder);
            }

            if (moveJava) {
                copyFile(classFile, PropertiesManager.crossServerJavaFolder);
                copyFile(objFile, PropertiesManager.crossServerJavaFolder);
                copyFile(classFile, PropertiesManager.gameServerJavaFolder);
                copyFile(objFile, PropertiesManager.gameServerJavaFolder);
                copyFile(classFile, PropertiesManager.battleServerJavaFolder);
                copyFile(objFile, PropertiesManager.battleServerJavaFolder);
            }
        } else if (PropertiesManager.crossServerExcel.contains(className)) {
            if (moveJson) {
                copyFile(jsonFile, PropertiesManager.crossServerJsonFolder);
            }

            if (moveJava) {
                copyFile(classFile, PropertiesManager.crossServerJavaFolder);
                copyFile(objFile, PropertiesManager.crossServerJavaFolder);
            }
        } else if (PropertiesManager.battleServerExcel.contains(className)) {
            if (moveJson) {
                copyFile(jsonFile, PropertiesManager.battleServerJsonFolder);
            }

            if (moveJava) {
                copyFile(classFile, PropertiesManager.battleServerJavaFolder);
                copyFile(objFile, PropertiesManager.battleServerJavaFolder);
            }
        } else {
            if (PropertiesManager.CGExcelName.contains(className)) {
                if (moveJson) {
                    copyFile(jsonFile, PropertiesManager.crossServerJsonFolder);
                }

                if (moveJava) {
                    copyFile(classFile, PropertiesManager.crossServerJavaFolder);
                    copyFile(objFile, PropertiesManager.crossServerJavaFolder);
                }
            }
            if (moveJson) {
                copyFile(jsonFile, PropertiesManager.gameServerJsonFolder);
            }

            if (moveJava) {
                copyFile(classFile, PropertiesManager.gameServerJavaFolder);
                copyFile(objFile, PropertiesManager.gameServerJavaFolder);
            }
        }

        //删除源文件
        jsonFile.deleteOnExit();
        classFile.deleteOnExit();
        objFile.deleteOnExit();
    }

    /**
     * 文件名除去扩展名
     *
     * @param filename
     * @return
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

}
