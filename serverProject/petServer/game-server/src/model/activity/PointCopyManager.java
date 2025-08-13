package model.activity;

import cfg.*;
import common.tick.GlobalTick;
import common.tick.Tickable;
import io.netty.util.internal.ConcurrentSet;
import java.util.*;
import lombok.Getter;
import model.consume.ConsumeUtil;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.reward.RewardUtil;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.ClientActivity;
import protocol.Common.Consume;
import protocol.GameplayDB.DB_PointCopy;
import protocol.GameplayDB.DB_PointCopy.Builder;
import protocol.GameplayDB.GameplayTypeEnum;
import util.ArrayUtil;
import util.LogUtil;
import util.TimeUtil;

public class PointCopyManager implements Tickable, GamePlayerUpdate {
    private static PointCopyManager instance;

    public static PointCopyManager getInstance() {
        if (instance == null) {
            synchronized (PointCopyManager.class) {
                if (instance == null) {
                    instance = new PointCopyManager();
                }
            }
        }
        return instance;
    }

    private PointCopyManager() {
    }

    private long startTime;
    private long endTime;
    @Getter
    private PointCopyOpenTimeObject openTimeCfg;

    private static final String POINT_COPY_IDX = String.valueOf(GameplayTypeEnum.GTE_PointCopy_VALUE);

    /**该字段只读**/
    public final Map<Integer, PointCopyCfgObject> rewardList = new HashMap<>();

    /**
     * 活动需要消耗的活动货币id
     */
    private final Set<Integer> currencyIdList = new ConcurrentSet<>();

    private void clear() {
        startTime = 0;
        endTime = 0;
        openTimeCfg = null;
        currencyIdList.clear();
    }

    public boolean init() {
        clear();

        Map<Integer, PointCopyOpenTimeObject> ix_cfg = PointCopyOpenTime._ix_id;
        if (ix_cfg == null || ix_cfg.isEmpty()) {
            LogUtil.warn("point copy open cfg is null");
            return true;
        }

        for (PointCopyOpenTimeObject value : ix_cfg.values()) {
            if (getTime(value.getStarttime()) >= getTime(value.getEndtime())) {
                LogUtil.error("pointCopyOpenTime cfg is error, cfgId = " + value.getId());
                return false;
            }
            if (!checkMissionList(value.getFightlist(), 1) || !checkMissionList(value.getPointlist(), 2)) {
                LogUtil.error("pointCopyOpenTime cfg is error, cfgId = " + value.getId());
                return false;
            }
        }

        gameplayEntity entity = gameplayCache.getByIdx(POINT_COPY_IDX);
        DB_PointCopy.Builder pointCopy;
        if (entity != null) {
            try {
                pointCopy = DB_PointCopy.parseFrom(entity.getGameplayinfo()).toBuilder();
            } catch (Exception e) {
                pointCopy = DB_PointCopy.newBuilder();
            }
        } else {
            pointCopy = DB_PointCopy.newBuilder();
        }

        this.openTimeCfg = PointCopyOpenTime.getById(pointCopy.getCurCfgId());
        if (openTimeCfg != null) {
            this.startTime = getTime(openTimeCfg.getStarttime());
            this.endTime = getTime(openTimeCfg.getEndtime());
        }

        updateSpecialActivity();
        return GlobalTick.getInstance().addTick(this) && gameplayCache.getInstance().addToUpdateSet(this);
    }

    /**
     * 检查任务配置
     * @param list
     * @param type  1：战斗任务， 2：积分任务
     * @return
     */
    private boolean checkMissionList(int[] list, int type) {
        if (list == null || list.length <= 0) {
            return false;
        }

        for (int i : list) {
            PointCopyCfgObject value = PointCopyCfg.getById(i);
            if (value == null) {
                LogUtil.error("pointCopyCfg id = " + i + " is null");
                return false;
            }

            if (value.getMissiontype() != type) {
                LogUtil.error("pointCopyCfg miss type match");
                return false;
            }

            if (value.getMissiontype() == 1) {
                if (FightMake.getById(value.getFightmakeid()) == null) {
                    LogUtil.error("point copy cfg error, fightMake is not exist, cfg id = " + value.getFightmakeid());
                    return false;
                }
//                if (value.getPointreward() <= 0) {
//                    LogUtil.warn("point copy cfg error， point reward <= 0, cfg id = " + value.getFightmakeid());
//                    return false;
//                }
            } else if (value.getMissiontype() == 2) {
                if (value.getPointtarget() <= 0) {
                    LogUtil.error("point copy cfg error, fightMake is not exist, cfg id = " + value.getFightmakeid());
                    return false;
                }
                if (RewardUtil.parseRewardIntArrayToRewardList(value.getReward()) == null) {
                    LogUtil.warn("point copy cfg error， point reward <= 0, cfg id = " + value.getFightmakeid());
                    return false;
                }
            } else {
                LogUtil.error("unsupported type");
                return false;
            }
        }
        return true;
    }

    private long getTime(int[] timeCfg) {
        if (timeCfg == null || timeCfg.length != 4) {
            return 0;
        }
        try {
            Calendar instance = Calendar.getInstance();
            //处理为0时区
            instance.setTimeZone(TimeZone.getTimeZone(TimeUtil.defaultTimeZone));
            instance.set(Calendar.MONTH, timeCfg[0] - 1);
            //获取当月日期配置不能大于当月的最大日期
            instance.set(Calendar.DAY_OF_MONTH, Math.min(timeCfg[1], instance.getActualMaximum(Calendar.DAY_OF_MONTH)));
            instance.set(Calendar.HOUR_OF_DAY, timeCfg[2]);
            instance.set(Calendar.MINUTE, timeCfg[3]);
            instance.set(Calendar.SECOND, 0);
            instance.set(Calendar.MILLISECOND, 0);
            return instance.getTimeInMillis();
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return -1;
        }
    }

    public boolean refreshPointCopy() {
        clear();
        Map<Integer, PointCopyOpenTimeObject> ix_id = PointCopyOpenTime._ix_id;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        for (PointCopyOpenTimeObject value : ix_id.values()) {
            long startTime = getTime(value.getStarttime());
            long endTime = getTime(value.getEndtime());
            if (startTime < curTime && curTime < endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.openTimeCfg = value;
                break;
            }
        }
        return true;
    }


    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime > endTime || currentTime < startTime) {
            refreshPointCopy();
        }
    }

    public boolean isOpen() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return curTime >= startTime && curTime <= endTime;
    }

    public void updateSpecialActivity() {
        ClientActivity.Builder newActivity = ClientActivity.newBuilder();
        newActivity.setActivityType(ActivityTypeEnum.ATE_PointCopy);
        ActivityManager.getInstance().addSpecialActivity(newActivity.build());
    }

    public int getCfgId() {
        if (this.openTimeCfg == null) {
            return 0;
        }
        return openTimeCfg.getId();
    }

    public List<PointCopyCfgObject> getAllRewardMission() {
        List<PointCopyCfgObject> result = new ArrayList<>();
        if (openTimeCfg != null) {
            int[] pointList = openTimeCfg.getPointlist();
            for (int i : pointList) {
                PointCopyCfgObject byId = PointCopyCfg.getById(i);
                if (byId != null) {
                    result.add(byId);
                }
            }
        }

        return result;
    }

    public PointCopyCfgObject getRewardMissionById(int index) {
        if (openTimeCfg != null) {
            int[] pointList = openTimeCfg.getPointlist();
            if (!ArrayUtil.intArrayContain(pointList, index)) {
                return null;
            }
            return PointCopyCfg.getById(index);
        }
        return null;
    }

    @Override
    public void update() {
        gameplayEntity entity = gameplayCache.getByIdx(POINT_COPY_IDX);
        if (entity == null) {
            entity = new gameplayEntity();
            entity.setIdx(POINT_COPY_IDX);
        }
        Builder builder = DB_PointCopy.newBuilder();
        builder.setCurCfgId(getCfgId());

        entity.setGameplayinfo(builder.build().toByteArray());
        entity.putToCache();
    }

    public static int getDefaultUnlockFightId(int cfgId) {
        PointCopyOpenTimeObject cfg = PointCopyOpenTime.getById(cfgId);
        if (cfg == null) {
            return -1;
        }
        return cfg.getDefaultunlockfightid();
    }

    public Consume getConsumeByCfgId(int cfgId) {
        PointCopyCfgObject byId = PointCopyCfg.getById(cfgId);
        if (byId == null || byId.getMissiontype() == 2) {
            return null;
        }

        PointCopyOpenTimeObject openTimeCfg = PointCopyManager.getInstance().getOpenTimeCfg();
        if (openTimeCfg == null || openTimeCfg.getDropticket() == null || openTimeCfg.getDropticket().length < 2) {
            return null;
        }

        return ConsumeUtil.parseConsume(openTimeCfg.getDropticket()[0], openTimeCfg.getDropticket()[1], byId.getConsume());
    }

    public int getFightMakeIdByCfgId(int cfgId) {
        PointCopyCfgObject byId = PointCopyCfg.getById(cfgId);
        if (byId == null || byId.getMissiontype() == 2) {
            return -1;
        }
        return byId.getFightmakeid();
    }
}