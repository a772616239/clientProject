package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 属性管理类
 */
public class PropertiesManager {

    public static final String configPath = getBaseDir() + "config.properties";

    public static String excelSourceFolder;
    public static String javaPackage;

    public static String crossServerJsonFolder;
    public static String crossServerJavaFolder;

    public static String gameServerJsonFolder;
    public static String gameServerJavaFolder;

    public static Set<String> crossServerExcel = new HashSet<>();
    public static Set<String> commonExcel = new HashSet<>();
    public static Set<String> CGExcelName = new HashSet<>();

    public static Set<String> battleServerExcel = new HashSet<>();
    public static String battleServerJsonFolder;
    public static String battleServerJavaFolder;


    static {
        Properties properties = initProperties(configPath);
        if (properties == null) {
            System.out.println("init properties error");
            System.exit(0);
        }
        excelSourceFolder = getBaseDir() + properties.getProperty("excelSourceFolder");
        javaPackage = properties.getProperty("javaPackage");
        crossServerJsonFolder = getBaseDir() + properties.getProperty("crossServerJsonFolder");
        crossServerJavaFolder = getBaseDir() + properties.getProperty("crossServerJavaFolder");
        gameServerJsonFolder = getBaseDir() + properties.getProperty("gameServerJsonFolder");
        gameServerJavaFolder = getBaseDir() + properties.getProperty("gameServerJavaFolder");

        battleServerJsonFolder = getBaseDir() + properties.getProperty("battleServerJsonFolder");
        battleServerJavaFolder = getBaseDir() + properties.getProperty("battleServerJavaFolder");

        //先初始化文件名
        parseExcelName(crossServerExcel, properties.getProperty("crossServerExcelName"));
        parseExcelName(commonExcel, properties.getProperty("commonExcel"));
        parseExcelName(battleServerExcel, properties.getProperty("battleServerExcelName"));
        parseExcelName(CGExcelName, properties.getProperty("CGExcelName"));

    }

    public static String getBaseDir(){
        return System.getProperty("user.dir") + File.separator;
    }


    private static Properties initProperties(String filePath) {
        if (filePath == null) {
            System.out.println("error filePath");
            return null;
        }
        File file = new File(filePath);
        if (!file.isFile()) {
            System.out.println("filePath is not a direct file");
            return null;
        }

        try {
            InputStream in = new FileInputStream(filePath);
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            System.out.println("init properties error");
        }
        return null;
    }

    public static void parseExcelName(Set<String> set, String clientExcelName) {
        if (set == null || clientExcelName == null) {
            System.out.println("main.MainFunction.parseExcelName, error params");
            return;
        }

        String[] split = clientExcelName.split(",");
        set.addAll(Arrays.asList(split));
    }
}
