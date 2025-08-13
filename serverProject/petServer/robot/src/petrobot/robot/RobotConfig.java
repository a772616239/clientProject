package petrobot.robot;

import petrobot.FileLoaderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RobotConfig {
    private RobotConfig() {
    }

    static RobotConfig robotConfig = new RobotConfig();

    public static RobotConfig getInstance() {
        return robotConfig;
    }

    /**
     * 服务器域名
     */
    private String domain;
    /**
     * 端口
     */
    private int port;
    /**
     * 机器人发送心跳包间隔
     */
    private int hearBeatInterval;
    /**
     * 机器人组的个数
     */
    private int groupCount;
    /**
     * 每个机器人组中机器人的个数
     */
    private int robotCountPerGroup;
    /**
     * 机器人登录名称前缀（登录名=前缀+索引，索引见robotBeginIndex配置）
     */
    private String loginNamePre;
    /**
     * 机器人开始索引，机器人索引从当前配置开始，到最大个数结束
     */
    private int robotBeginIndex;
    /**
     * 配置文件路径
     */
    private String dataConfigPath;
    /**
     * 机器人是否循环执行
     */
    private boolean robotCycle;

    private boolean debug;

    public void load(String file) {
        InputStream in = null;
        try {
            File configFile = new File(file);
            if (configFile.exists()) {
                in = new FileInputStream(configFile);
            } else {
                in = FileLoaderUtil.findInputStreamByFileName(file);
            }
            if (in == null) {
                throw new RuntimeException("配置文件找不到：" + file);
            }
            Properties pro = new Properties();
            pro.load(in);

            this.domain = pro.getProperty("host");
            this.port = Integer.parseInt(pro.getProperty("port"));
            this.groupCount = Integer.parseInt(pro.getProperty("groupCount"));
            this.robotCountPerGroup = Integer.parseInt(pro.getProperty("robotCountPerGroup"));
            this.loginNamePre = pro.getProperty("loginNamePre");
            this.robotBeginIndex = Integer.parseInt(pro.getProperty("robotBeginIndex"));
            this.robotCycle = Boolean.parseBoolean(pro.getProperty("robot.cycle"));
            this.debug = Boolean.parseBoolean(pro.getProperty("debug"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHearBeatInterval() {
        return hearBeatInterval;
    }

    public void setHearBeatInterval(int hearBeatInterval) {
        this.hearBeatInterval = hearBeatInterval;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    public int getRobotCountPerGroup() {
        return robotCountPerGroup;
    }

    public void setRobotCountPerGroup(int robotCountPerGroup) {
        this.robotCountPerGroup = robotCountPerGroup;
    }

    public String getLoginNamePre() {
        return loginNamePre;
    }

    public void setLoginNamePre(String loginNamePre) {
        this.loginNamePre = loginNamePre;
    }

    public int getRobotBeginIndex() {
        return robotBeginIndex;
    }

    public void setRobotBeginIndex(int robotBeginIndex) {
        this.robotBeginIndex = robotBeginIndex;
    }

    public String getDataConfigPath() {
        return dataConfigPath;
    }

    public void setDataConfigPath(String dataConfigPath) {
        this.dataConfigPath = dataConfigPath;
    }

    public boolean isRobotCycle() {
        return robotCycle;
    }

    public void setRobotCycle(boolean robotCycle) {
        this.robotCycle = robotCycle;
    }

    public boolean isDebug() {
        return debug;
    }
}
