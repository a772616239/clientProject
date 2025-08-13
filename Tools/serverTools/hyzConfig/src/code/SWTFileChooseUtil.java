package code;

import java.awt.Canvas;
import java.awt.Container;
import java.io.File;
import javax.swing.JPanel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Administrator
 */
public class SWTFileChooseUtil {

    public final static int file = 1;

    public final static int dir = 2;

    private File fileFile = null;

    public SWTFileChooseUtil(Container container, int type, File chooseFile) {
        Canvas awtCanvas = new Canvas();
        JPanel panel = new JPanel();
        panel.setLayout(null);
        awtCanvas.setLocation(1, 1);
        awtCanvas.setSize(2, 2);
        panel.add(awtCanvas);
        container.add(panel);

        Display swtDisplay = Display.getDefault();
        Shell swtShell = SWT_AWT.new_Shell(swtDisplay, awtCanvas);
        swtShell.setSize(1, 1);
        swtShell.setVisible(false);
        try {
            switch (type) {
                case 1:
                    fileDig(swtShell, chooseFile);
                    break;
                case 2:
                    folderDig(swtShell, chooseFile);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // TODO: handle exception  
            e.printStackTrace();
        } finally {
            swtShell.dispose();
        }
    }


    /**
     * 选择文件对话框
     * @param parent
     */
    private void fileDig(Shell parent, File chooseFile) {
        FileDialog fileDig = new FileDialog(parent, SWT.OPEN);
        fileDig.setText("文件选择");
        if (chooseFile == null) {
            fileDig.setFilterPath("SystemRoot");
        } else {
            fileDig.setFilterPath(chooseFile.getAbsolutePath());
        }

        String[] extensions = new String[]{"*.xlsx", "*.xlsm", "*.xls"};
        fileDig.setFilterExtensions(extensions);

        String selected = fileDig.open();
        fileFile = new File(selected);
    }

    /**
     * 选择目录对话框
     * @param parent
     */
    private void folderDig(Shell parent, File chooseFile) {
        DirectoryDialog folderdlg = new DirectoryDialog(parent);
        folderdlg.setText("文件选择");
        if (chooseFile == null) {
            folderdlg.setFilterPath("SystemDrive");
        } else {
            folderdlg.setFilterPath(chooseFile.getAbsolutePath());
        }

        folderdlg.setMessage("请选择相应的文件夹");
        //打开文件对话框，返回选中文件夹目录
        String selectedDir = folderdlg.open();
        if (selectedDir == null) {
            return;
        } else {
            System.out.println("您选中的文件夹目录为：" + selectedDir);
            fileFile = new File(selectedDir);
        }
    }

    /**
     *
     * @return
     */
    public File getFileFile() {
        return fileFile;
    }
}  
