package main;

import code.CodeTool;
import java.io.File;
import utils.FileUtil;

/**
 * @author huhan
 * @date 2021/4/1
 */
public class CreateOneCfgJson {
    public static void main(String[] args) {
        if (args.length > 0) {
            File file = new File(args[0]);
            if (!file.isFile() || !file.exists()) {
                System.out.println("文件路径错误");
                return;
            }

            String filName = file.getName();
            String filePath = file.getAbsolutePath();
            String fileNameNoEx = FileUtil.getFileNameNoEx(filName);
            CodeTool.createAllCode(filePath, fileNameNoEx, true, false);
        } else {
            System.out.println("参数为空");
        }
    }
}
