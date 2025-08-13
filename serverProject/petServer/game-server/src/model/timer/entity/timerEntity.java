/**
 * created by tool DAOGenerate
 */
package model.timer.entity;

import common.GameConst.EventType;
import common.IdGenerator;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import model.obj.BaseObj;
import model.timer.TimerConst.TimerExpireType;
import model.timer.dbCache.timerCache;
import protocol.Server.DB_TimerParam;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * created by tool
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class timerEntity extends BaseObj {

    public String getClassType() {
        return "timerEntity";
    }

    @Override
    public void putToCache() {
        timerCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.params = getParamBuilder().build().toByteArray();
    }

    /**
     *
     */
    private String idx;

    private long starttime;

    /**
     * 过期类型，1：以指定时间过期，-1不过期。
     * 2:以指定触发次数后过期，-1无限次
     */
    private int expiretype;

    /**
     * 触发间隔
     */
    private long cycleinterval;

    /**
     * 下次触发时间
     */
    private long nexttriggertime;

    /**
     * 过期参数，当expireType =1， 过期时间， expireType =2, 最大触发次数
     */
    private long expireparam;

    /**
     * 已经触发的次数
     */
    private int alreadytriggertimes;

    /**
     * 目标类型
     */
    private int targettype;

    /**
     * 参数
     */
    private byte[] params;

    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }


    /**
     * ===================================================
     */

    private timerEntity() {
    }

    /**
     * @param idx           需要存储到数据库Timer,idx 请在model.timer.TimerConst.TimerIdx中定义枚举
     * @param startTime     循环开始时间
     * @param cycleInterval 循环间隔
     * @param expireType    过期类型(TimerExpireType)
     * @param expireParam   过期参数,当为次数类型是为最大触发次数，当为时间类型时为过期时间
     * @param targetType    目标类型，用户自定
     * @param paramBuilder  参数（DB_TimerParam） 自定（Server.proto)
     */
    public timerEntity(String idx, long startTime, long cycleInterval, int targetType, int expireType,
                       long expireParam, DB_TimerParam.Builder paramBuilder) {
        this.idx = idx;
        this.starttime = startTime;
        this.cycleinterval = cycleInterval;
        this.targettype = targetType;
        this.expiretype = expireType;
        this.expireparam = expireParam;
        this.paramBuilder = paramBuilder;

        calculateNextTriggerTime();
    }

    /**
     * 适用于只触发一次，且不需要持久化到数据库的情况
     *
     * @param nextTriggerTime
     * @param targetType
     */
    public timerEntity(long nextTriggerTime, int targetType) {
        this.idx = IdGenerator.getInstance().generateId();
        this.nexttriggertime = nextTriggerTime;
        this.targettype = targetType;
    }

    private DB_TimerParam.Builder paramBuilder;

    public DB_TimerParam.Builder getParamBuilder() {
        if (paramBuilder == null) {
            this.paramBuilder = getTimerParams();
        }
        return paramBuilder;
    }

    private DB_TimerParam.Builder getTimerParams() {
        try {
            if (this.params != null) {
                return DB_TimerParam.parseFrom(this.params).toBuilder();
            } else {
                return DB_TimerParam.newBuilder();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    /**
     * 如果传入的参数与现有的定时器id一致，则不更新
     *
     * @param timer
     */
    public void merge(timerEntity timer) {
        if (timer == null) {
            return;
        }

        this.starttime = timer.getStarttime();
        this.expiretype = timer.getExpiretype();
        this.cycleinterval = timer.getCycleinterval();
        this.targettype = timer.getTargettype();
        this.paramBuilder = timer.getParamBuilder();
        this.expireparam = timer.getExpireparam();
    }

    /**
     * 除了下次触发时间（nexttriggertime）和已经触发次数外（alreadytriggertimes）的字段相同为完全相同
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof timerEntity)) {
            return false;
        }
        timerEntity entity = (timerEntity) obj;
        this.transformDBData();
        entity.transformDBData();
        return Objects.equals(this.getIdx(), entity.getIdx())
                && this.expiretype == entity.getExpiretype()
                && this.cycleinterval == entity.getCycleinterval()
                && this.expireparam == entity.getExpireparam()
                && this.targettype == entity.getTargettype()
                && Arrays.equals(this.params, entity.getParams())
                && this.starttime == entity.getStarttime();
    }

    public boolean canInvoke() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.starttime > currentTime) {
            return false;
        }
        if (this.nexttriggertime > currentTime) {
            return false;
        }
        return true;
    }

    /**
     * 调用该计时器
     *
     * @param needUpdate 是否需要更新到数据库
     */
    public void invoke(boolean needUpdate) {
        LogUtil.info("timerEntity.invoke , invoke idx = " + this.idx + ",targetType = " + this.getTargettype()
                + ", curTime = " + GlobalTick.getInstance().getCurrentTime());

        Event event = Event.valueOf(EventType.ET_TimerInvoke, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(getTargettype(), getParamBuilder());
        EventManager.getInstance().dispatchEvent(event);

        this.alreadytriggertimes++;
        calculateNextTriggerTime();

        if (needUpdate) {
            putToCache();
        }
    }

    /**
     * 当前定时器是否已经失效
     */
    public boolean isInvalid() {
        if (getExpireparam() == -1) {
            return false;
        }
        if (getExpiretype() == TimerExpireType.ET_EXPIRE_BY_TIME) {
            if (getNexttriggertime() < getExpireparam()) {
                return false;
            }
        } else if (getExpiretype() == TimerExpireType.ET_EXPIRE_BY_TRIGGER_TIMES) {
            if (getAlreadytriggertimes() < getExpiretype()) {
                return false;
            }
        }

        return true;
    }

    private synchronized void calculateNextTriggerTime() {
        if (this.cycleinterval <= 0) {
            return;
        }
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        long tmpTime = this.cycleinterval - (currentTime - this.starttime) % this.cycleinterval;
        this.nexttriggertime = currentTime + tmpTime;
    }
}