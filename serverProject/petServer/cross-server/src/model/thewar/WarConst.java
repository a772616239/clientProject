package model.thewar;


import cfg.TheWarFightMakeConfig;
import cfg.TheWarFightMakeConfigObject;
import java.util.Random;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;

public class WarConst {
    public static class ActivityState {
        public static final int EndState = 0;          //活动结束状态
        public static final int OpenState = 1;         //活动开启状态
        public static final int PreEndState = 2;       //活动预结算状态
    }

    public static class RoomState {
        public static final int ClosedState = 0;
        public static final int FightingState = 1;
        public static final int SettleState = 2;
        public static final int EndState = 3;
    }

    public static class WarCamp {
        public static final int camp0 = 0;
        public static final int camp1 = 1;
    }

    public static boolean isServerOnlyProp(int propertyEnum) {
        return propertyEnum == TheWarCellPropertyEnum.TWCP_CampPosGroup_VALUE
                || propertyEnum == TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE
                || propertyEnum == TheWarCellPropertyEnum.TWCP_MonsterRefreshCfgId_VALUE
                || propertyEnum == TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE
                || propertyEnum == TheWarCellPropertyEnum.TWCP_ValidPortalSourcePos_VALUE
                || propertyEnum == TheWarCellPropertyEnum.TWCP_PortalSourcePos_VALUE;
    }

    public static int getFightMakeIdByCfgId(int cfgId) {
        TheWarFightMakeConfigObject fightMakeCfg = TheWarFightMakeConfig.getById(cfgId);
        if (fightMakeCfg == null || fightMakeCfg.getFightmakelist() == null || fightMakeCfg.getFightmakelist().length <= 0) {
            return 0;
        }
        int index = new Random().nextInt(fightMakeCfg.getFightmakelist().length);
        int fightMakeId = fightMakeCfg.getFightmakelist()[index];
        return fightMakeId;
    }

    public static long protoPosToLongPos(Position pos) {
        if (pos == null) {
            return 0;
        }
        return ((((long) pos.getX()) << 32) & 0xFFFFFFFF00000000l) | (((long) pos.getY()) & 0xFFFFFFFFl);
    }
}
