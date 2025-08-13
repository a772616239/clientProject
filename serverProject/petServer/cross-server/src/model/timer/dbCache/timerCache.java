/*CREATED BY TOOL*/

package model.timer.dbCache;

import annotation.annationInit;
import common.GlobalTick;
import common.IdGenerator;
import common.Tickable;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.timer.TimerConst.TimerExpireType;
import model.timer.TimerConst.TimerIdx;
import model.timer.TimerConst.TimerTargetType;
import model.timer.cache.timerUpdateCache;
import model.timer.entity.timerEntity;
import org.apache.commons.lang.StringUtils;
import protocol.Server.DB_TimerParam;
import util.LogUtil;
import util.TimeUtil;

@annationInit(value = "timerCache", methodname = "load")
public class timerCache extends baseCache<timerCache> implements IbaseCache, Tickable {

    /******************* MUST HAVE ********************************/

    private static timerCache instance = null;

    public static timerCache getInstance() {

        if (instance == null) {
            instance = new timerCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "timerDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("timerDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (timerCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(timerEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static timerEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (timerEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return timerUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        timerEntity t = (timerEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    /**
     * 临时timer存储
     **/
    private final Map<String, timerEntity> temporaryTimer = new ConcurrentHashMap<>();

    public boolean init() {
        addTimer(TimerIdx.TI_RESET_DAILY_DATE, TimeUtil.getNextDayResetTime(0), TimeUtil.MS_IN_A_DAY,
                TimerTargetType.TT_RESET_DAILY_DATA, TimerExpireType.ET_EXPIRE_BY_TIME, -1, true);

        addTimer(TimerIdx.TI_RESET_WEEK_DATE, TimeUtil.getNextWeekResetStamp(0), TimeUtil.MS_IN_A_WEEK,
                TimerTargetType.TT_RESET_WEEK_DATA, TimerExpireType.ET_EXPIRE_BY_TIME, -1, true);

        return GlobalTick.getInstance().addTick(this);
    }

    @Override
    public void onTick() {
        Map<String, BaseEntity> all = getAll();
        for (BaseEntity value : all.values()) {
            if (!(value instanceof timerEntity)) {
                continue;
            }
            timerEntity timer = (timerEntity) value;
            if (timer.canInvoke()) {
                timer.invoke(true);
                if (timer.isInvalid()) {
                    remove(timer.getIdx());
                }
            }
        }

        for (timerEntity entity : temporaryTimer.values()) {
            if (entity.canInvoke()) {
                entity.invoke(false);
                if (entity.isInvalid()) {
                    temporaryTimer.remove(entity.getIdx());
                }
            }
        }
    }


    /**
     * 添加一个新的定时器任务，当已有定时器任务存在时，会直接以当前参数覆盖之前的参数，
     *
     * @param idx           定时器idx,固定id
     * @param startTime     定时器开始时间,
     * @param cycleInterval 循环间隔
     * @param targetType    事件类型 {@see model.timer.TimerConst.TimerTargetType}
     * @param expireType    过期类型，1:以指定时间过期，
     *                      2:以指定触发次数后过期
     * @param expireParam   过期参数：当expireType =1， 过期时间， -1不过期。
     *                      expireType =2, 最大触发次数 ，-1无限次
     * @param needSave      是否需要保存到数据库
     * @param paramBuilder  参数列表
     * @return
     * @see TimerIdx
     * @see TimerTargetType
     * @see TimerExpireType
     * @see TimerExpireType
     */
    public void addTimer(String idx, long startTime, long cycleInterval, int targetType, int expireType,
                         long expireParam, DB_TimerParam.Builder paramBuilder, boolean needSave) {
        timerEntity entity = getByIdx(idx);
        if (entity != null) {
            LogUtil.warn("specify idx is already exist, idx =" + idx);
            entity.setStarttime(startTime);
            entity.setCycleinterval(cycleInterval);
            entity.setTargettype(targetType);
            entity.setExpiretype(expireType);
            entity.setExpireparam(expireParam);
            entity.setParamBuilder(paramBuilder);

            //不需要保存
            if (!needSave) {
                remove(idx);
                temporaryTimer.put(idx, entity);
            }
        } else {
            entity = new timerEntity(idx, startTime, cycleInterval, targetType, expireType, expireParam, paramBuilder);

            if (needSave) {
                put(entity);
            } else {
                temporaryTimer.put(idx, entity);
            }
        }
    }

    public void addTimer(long startTime, long cycleInterval, int targetType, int expireType,
                         long expireParam, DB_TimerParam.Builder paramBuilder, boolean needSave) {
        addTimer(IdGenerator.getInstance().generateId(), startTime, cycleInterval, targetType,
                expireType, expireParam, paramBuilder, needSave);
    }

    public void addTimer(String idx, long startTime, long cycleInterval, int targetType, int expireType,
                         long expireParam, boolean needSave) {
        addTimer(idx, startTime, cycleInterval, targetType, expireType, expireParam, null, needSave);
    }

    public void addTimer(long startTime, long cycleInterval, int targetType, int expireType, long expireParam,
                         boolean needSave) {
        addTimer(startTime, cycleInterval, targetType, expireType, expireParam, null, needSave);
    }

    public long getNextTriggerTime(String idx) {
        if (StringUtils.isBlank(idx)) {
            return 0;
        }
        timerEntity entity = getByIdx(idx);
        if(entity == null) {
            return 0;
        }
        return entity.getNexttriggertime();
    }
}
