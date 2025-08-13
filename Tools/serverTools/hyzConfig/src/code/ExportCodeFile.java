package code;

import helper.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import utils.FileUtil;

/**
 * @author
 * @date
 */
public class ExportCodeFile {

    public static final String EXPORT_CHARSET = "UTF-8";

    /**
     * 导出文件
     *
     * @param outJsonStr   导出的json字符串
     * @param outObjectStr
     * @param outJavaStr
     * @param moveJson     是否需要移动json
     * @param moveJava     是否需要移懂Java文件
     */
    public static void export(String outJsonStr, String outObjectStr, String outJavaStr, boolean moveJson, boolean moveJava) {
        if (StringUtils.isEmpty(outObjectStr)) {
            System.out.println("export error  outObjectStr>>>>>" + outObjectStr);
            System.out.println("###########创建[" + CodeTool.className + "Object]失败");
            System.out.println("###########创建[" + CodeTool.className + "]失败" + "##################");
            return;
        }

        if (StringUtils.isEmpty(outJsonStr)) {
            System.out.println("export error  outJsonStr>>>>>" + outJsonStr);
            System.out.println("###########创建[" + CodeTool.className + ".json]失败");
        }

        if (StringUtils.isEmpty(outJavaStr)) {
            System.out.println("export error  outJavaStr>>>>>" + outJavaStr);
            System.out.println("###########创建[" + CodeTool.className + ".java]失败");
        }

        makeDirs();

        String filePath = exportPackName + File.separator;

        String className = CodeTool.className;
        String objectName = CodeTool.objectName;

        String jsonPath = filePath + className + ".json";
        String javaPath = filePath + className + ".java";
        String objPath = filePath + objectName + ".java";

        try {
            if (!StringUtils.isEmpty(outJsonStr)) {
                writeFile(filePath + className + ".json", outJsonStr.getBytes(EXPORT_CHARSET));
                System.out.println("创建" + className + ".json完毕");
            }

            if (!StringUtils.isEmpty(outJavaStr)) {
                writeFile(filePath + className + ".java", outJavaStr.getBytes(EXPORT_CHARSET));
                System.out.println("创建" + className + ".java完毕");
            }

            if (!StringUtils.isEmpty(outObjectStr)) {
                writeFile(filePath + objectName + ".java", outObjectStr.getBytes(EXPORT_CHARSET));
                System.out.println("创建" + objectName + "完毕");
            }

//            System.out.println("目标文件夹《" + exportPackName + "》");
//            System.out.println("###########创建[" + CodeTool.className + "]成功##################");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("###########创建[" + CodeTool.className + "]失败" + "##################");
        }
        FileUtil.copyFile(className, jsonPath, javaPath, objPath, moveJson, moveJava);
    }

    /**
     * 输出目录
     */
    public static String exportPackName = null;
    private static String exportDefaultDir = "exportPack";

    public static void makeDirs() {
        String baseDir = System.getProperty("user.dir");
        if (StringUtils.isEmpty(exportPackName)) {
            exportPackName = baseDir + File.separator + exportDefaultDir;
        }
        File export = new File(exportPackName);
        if (!export.exists() || !export.isDirectory()) {
            export.mkdirs();
        }
    }

    public static void writeFile(String filename, byte[] contnet) {
        try {
            FileOutputStream fOut = new FileOutputStream(filename);
            fOut.write(contnet);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
