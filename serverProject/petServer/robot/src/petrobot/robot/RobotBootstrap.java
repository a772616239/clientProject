package petrobot.robot;

import cfg.MistMapConfig;
import cfg.MistWorldMapConfig;
import cfg.PetBagConfig;
import cfg.PetBaseProperties;
import cfg.PetFragmentConfig;
import cfg.PetRuneProperties;
import cfg.TheWarConstConfig;
import cfg.TheWarMapConfig;
import cfg.TheWarSeasonConfig;
import java.io.IOException;
import petrobot.robot.index.IndexManager;
import petrobot.system.thewar.config.TotalWarMapCfgData;
import petrobot.tick.GlobalTick;

/**
 * 机器人启动程序
 */
public class RobotBootstrap {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 启动配置
        PetBagConfig.getInstance().initConfig();
        PetBaseProperties.getInstance().initConfig();
        PetFragmentConfig.getInstance().initConfig();
        PetRuneProperties.getInstance().initConfig();
        MistWorldMapConfig.getInstance().initConfig();
        MistMapConfig.getInstance().initConfig();
        TheWarConstConfig.getInstance().initConfig();
        TheWarMapConfig.getInstance().initConfig();
        TheWarSeasonConfig.getInstance().initConfig();
        TotalWarMapCfgData.init();

        IndexManager.getIns().load();
        String excelFile = "index.xlsx";
        IndexManager.getIns().loadExcel(excelFile);
        String file = "robot.properties";
        if (args != null && args.length > 0) {
            file = args[0];
        }
        String loginNamePre = null;
        if (args != null && args.length > 1) {
            loginNamePre = args[1];
        }

        int robotCountPerGroup = 0;

        if (args != null && args.length > 2) {
            robotCountPerGroup = Integer.parseInt(args[2]);
        }

        int robotBeginIndex = -1;

        if (args != null && args.length > 3) {
            robotBeginIndex = Integer.parseInt(args[3]);
        }
        RobotConfig config = RobotConfig.getInstance();
        config.load(file);
        if (loginNamePre != null) {
            config.setLoginNamePre(loginNamePre);
        }
        if (robotCountPerGroup > 0) {
            config.setRobotCountPerGroup(robotCountPerGroup);
        }
        if (robotBeginIndex > -1) {
            config.setRobotBeginIndex(robotBeginIndex);
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        for (int i = 0; i < config.getGroupCount(); i++) {
            RobotGroup group = new RobotGroup(config, i);
            group.start();
        }

        GlobalTick.getInstance().run();
    }
}
