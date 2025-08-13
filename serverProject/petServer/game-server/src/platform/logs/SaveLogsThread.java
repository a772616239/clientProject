package platform.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateFormatUtils;
import util.LogUtil;

public class SaveLogsThread implements Runnable {

    private Class<? extends AbstractServerLog> clazz;

    public SaveLogsThread(Class<? extends AbstractServerLog> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void run() {
        try {
            Object lock = LogService.getInstance().getLock(clazz);
            List<String> logs = new ArrayList<>();
            synchronized (lock) {
                List<String> tempLogs = LogService.getInstance().getKEYS().get(clazz);
                logs.addAll(tempLogs);
                tempLogs.clear();
            }

            if (logs.size() > 0) {
                String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd-HH");
                String url = LogService.getInstance().URL + "/" + clazz.getSimpleName() + "/" + timestamp + "#1.log";
                File dir = new File(url);
                if (!dir.exists()) {
                    dir.createNewFile();
                }

                //存盘
                FileWriter writer = new FileWriter(dir, true);
                for (String log : logs) {
                    writer.write(log);
                    writer.append("\r\n");
                }
                writer.close();
            }
        } catch (IOException e) {
            LogUtil.printStackTrace(e);
        }
    }
}
