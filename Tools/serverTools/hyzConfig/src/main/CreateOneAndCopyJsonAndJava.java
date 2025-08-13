package main;

import code.CodeTool;
import code.SWTFileChooseUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import utils.FileUtil;

/**
 * @author Administrator
 * @date
 */
public class CreateOneAndCopyJsonAndJava {
    public static void main(String[] args) {
        final JFrame jFrame = new JFrame("选择文件");
        jFrame.setSize(new Dimension(200, 200));
        jFrame.setBackground(Color.BLACK);
        jFrame.setLocation(new Point(300, 300));
        jFrame.setVisible(true);
        jFrame.setLayout(new FlowLayout());
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("正在关闭...");
                System.exit(0);
            }
        });
        JButton button = new JButton("请选择配置文件...");
        button.addActionListener((ActionListener) arg0 -> {
            SWTFileChooseUtil chooseUtil = new SWTFileChooseUtil(jFrame, 1, (File) null);
            File file = chooseUtil.getFileFile();
            String filName = file.getName();
            String filePath = file.getAbsolutePath();
            String fileNameNoEx = FileUtil.getFileNameNoEx(filName);
            CodeTool.createAllCode(filePath, fileNameNoEx, true, true);
        });
        jFrame.add(button);
    }
}
