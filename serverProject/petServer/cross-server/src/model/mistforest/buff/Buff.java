package model.mistforest.buff;

import cfg.MistBuffConfig;
import cfg.MistBuffConfigObject;
import common.GlobalTick;
import java.util.HashMap;
import model.mistforest.mistobj.MistObject;
import model.mistforest.trigger.Trigger;
import protocol.MistForest.UnitBuffData;
import util.LogUtil;

public class Buff {
    private int buffId;
    private MistObject hostObj;
    private MistObject makerObj;
    private byte stackCount;
    private long expireTime;
    private long lifeTime;
    private long period;
    private long nextCycleTime;
    private MistBuffConfigObject buffCfg;
    private HashMap<Integer, Long> params;

    private long pauseTimeStamp;

    public Buff(int id, MistObject hostObj, MistObject makerObj) {
        this.buffId = id;
        this.hostObj = hostObj;
        this.makerObj = makerObj;
        init();
    }

    public void init() {
        MistBuffConfigObject buffCfg = MistBuffConfig.getById(buffId);
        if (buffCfg == null) {
            LogUtil.error("RoomId=" + hostObj.getRoom().getIdx() + ",Not found buff config, buffId = " + buffId);
            return;
        }
        this.expireTime = 0;
        this.nextCycleTime = 0;
        if (buffCfg.getMaxstackcount() > 0) {
            this.stackCount = 1;
        } else {
            this.stackCount = 0;
        }
        this.buffCfg = buffCfg;
        this.lifeTime = this.buffCfg.getLifetime();
        this.period = this.buffCfg.getCycletime();
    }

    public void clear() {
        hostObj = null;
        makerObj = null;
        stackCount = 0;
        expireTime = 0;
        lifeTime = 0;
        period = 0;
        nextCycleTime = 0;
        buffCfg = null;
        pauseTimeStamp = 0;
        if (params != null) {
            params.clear();
        }
    }

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
    }

    public MistObject getHostObj() {
        return hostObj;
    }

    public void setHostObj(MistObject HostObj) {
        this.hostObj = HostObj;
    }

    public MistObject getMakerObj() {
        return makerObj;
    }

    public void setMakerObj(MistObject makerObj) {
        this.makerObj = makerObj;
    }

    public byte getStackCount() {
        return stackCount;
    }

    public void setStackCount(byte stackCount) {
        this.stackCount = stackCount;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public boolean isBuffPaused(long curTime) {
        return pauseTimeStamp > 0 && pauseTimeStamp <= curTime;
    }

    public long getPauseTimeStamp() {
        return pauseTimeStamp;
    }

    public void setPauseTimeStamp(long pauseTimeStamp) {
        this.pauseTimeStamp = pauseTimeStamp;
    }

    public boolean pause(long curTime) {
        if (isBuffExpired(curTime)) {
            return false;
        }
        pauseTimeStamp = curTime;
        return true;
    }

    public boolean resume(long curTime) {
        if (!isBuffPaused(curTime)) {
            return false;
        }
        long pauseTime = curTime - pauseTimeStamp;
        if (nextCycleTime > 0) {
            nextCycleTime += pauseTime;
        }
        if (expireTime > 0) {
            if (buffCfg != null && buffCfg.getPausedecreasetime() > 0) {
                pauseTime -= buffCfg.getPausedecreasetime();
            }
            expireTime += pauseTime;
        }
        pauseTimeStamp = 0;
        return true;
    }

    public boolean isBuffExpired(long curTime) {
        if (hostObj == null) {
            return false;
        }
        return expireTime > 0 && expireTime <= curTime;
    }

    public MistBuffConfigObject getBuffCfg() {
        return buffCfg;
    }

    public void cacheBuffTriggerParams(HashMap<Integer, Long> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.putAll(params);
    }

    public void addBuffData(long pastTime) {
        if (buffCfg == null) {
            LogUtil.error("buff cfg is null when addBuffData");
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (lifeTime > 0) {
            expireTime = curTime + lifeTime - pastTime;
            LogUtil.debug("RoomId=" + hostObj.getRoom().getIdx() + " addBuff expireTime=" + expireTime + ",HostId=" + hostObj.getId()
                    + ",ID=" + buffId + ",lifeTime = " + lifeTime + ",stackCount=" + stackCount);
        }
        if (period > 0 && nextCycleTime <= 0) {
            nextCycleTime = curTime + period;
        }
    }

    public void periodicEffect(long curTime) {
        if (buffCfg == null) {
            return;
        }
//        if (buffCfg.getMaxstackcount() > 0 && stackCount > 1) {
//            if (curTime > (expireTime - (stackCount - 1) * buffCfg.getLifetime())) {
//                --stackCount;
//                setLifeTime(getLifeTime() - buffCfg.getLifetime());
//            }
//        }
        if (period > 0 && nextCycleTime <= curTime) {
            if (buffCfg.getBuffCycleTriggers() != null) {
                for (Trigger trigger : buffCfg.getBuffCycleTriggers()) {
                    trigger.fire(makerObj, hostObj, params);
                }
            }
            nextCycleTime = curTime + period;
        }
    }

    public void addEffect(HashMap<Integer, Long> beforeAddParams) {
        if (buffCfg == null) {
            LogUtil.error("buff cfg is null when add");
            return;
        }
        if (buffCfg.getBuffAddTriggers() == null) {
            return;
        }
        LogUtil.debug("RoomId=" + hostObj.getRoom().getIdx() + ",Buff add effect,HostId=" + hostObj.getId() + ",buffId="
                + buffId);
        for (Trigger trigger : buffCfg.getBuffAddTriggers()) {
            trigger.fire(makerObj, hostObj, beforeAddParams);
        }
    }

    public void removeEffect() {
        if (buffCfg == null) {
            LogUtil.error("buff cfg is null when del");
            return;
        }
        if (buffCfg.getBuffDelTriggers() == null) {
            return;
        }
        LogUtil.debug("RoomId=" + hostObj.getRoom().getIdx() + ",Buff remove effect,HostId=" + hostObj.getId()
                + ",buffId=" + buffId);
        for (Trigger trigger : buffCfg.getBuffDelTriggers()) {
            trigger.fire(makerObj, hostObj, params);
        }
    }

    public void robEffect() {
        addEffect(this.params);
    }

    public void beenRobbedEffect() {
        if (buffCfg == null) {
            LogUtil.error("buff cfg is null when robbed");
            return;
        }
        if (buffCfg.getBuffrobbedeffectcmd() == null) {
            return;
        }
        LogUtil.debug("RoomId=" + hostObj.getRoom().getIdx() + ",Buff beenRobbed effect,HostId=" + hostObj.getId()
                + ",buffId=" + buffId);
        for (Trigger trigger : buffCfg.getBuffrobbedeffectcmd()) {
            trigger.fire(makerObj, hostObj, params);
        }
    }

    public void interruptBuff() {
        if (buffCfg == null) {
            LogUtil.error("buff cfg is null when interrupt");
            return;
        }
        if (buffCfg.getInterruptTriggers() == null) {
            return;
        }
        LogUtil.info("RoomId=" + hostObj.getRoom().getIdx() + ",Buff interrupt effect,HostId=" + hostObj.getId()
                + ",buffId=" + buffId);
        for (Trigger trigger : buffCfg.getInterruptTriggers()) {
            trigger.fire(makerObj, hostObj, params);
        }
    }

    public UnitBuffData buildBuffData(long curTime) {
        UnitBuffData.Builder buffBuilder = UnitBuffData.newBuilder();
        buffBuilder.setHostId(hostObj.getId());
        buffBuilder.setBuffId(getBuffId());
        buffBuilder.setStackCount(getStackCount());
        buffBuilder.setMaxLifeTime(getLifeTime());
        buffBuilder.setRemainLifeTime(getBuffRemainTime(curTime));
        buffBuilder.setIsPause(curTime == 0 ? false : isBuffPaused(curTime));
        return buffBuilder.build();
    }

    public long getBuffRemainTime(long curTime) {
        if (curTime == 0) {
            return getLifeTime();
        }
        return isBuffPaused(curTime) ? getExpireTime() - getPauseTimeStamp() : getExpireTime() - curTime;
    }
}
