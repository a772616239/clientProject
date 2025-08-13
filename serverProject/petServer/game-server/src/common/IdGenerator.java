package common;

import com.bowlong.sql.AtomicInt;
import common.load.ServerConfig;
import daoMaster.playerDAO;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import model.player.cache.playerUpdateCacheL;
import model.player.dbCache.playerCache;
import util.GameUtil;
import util.LogUtil;

public class IdGenerator {
    private static IdGenerator INSTANCE = new IdGenerator();
    private long machineId;

    private AtomicLong lastId;

    private static final int BASE_SHORT_ID = ServerConfig.getInstance().getServer() <= 0
            ? 10000000 : ServerConfig.getInstance().getServer() * 10000000;

    private final AtomicInt shortIdGenerator = new AtomicInt();

    private long twepoch = 1556640000000L;   // 基准时间 2019-05-01 00:00:00

    private long machineIdBits = 12L;
    private long maxMachineId = -1L ^ (-1L << machineIdBits);

    private long sequenceBits = 12L;

    private long machineIdShift = sequenceBits;
    private long timestampLeftShift = sequenceBits + machineIdBits;
    private long maxSequence = (-1L ^ (-1L << sequenceBits)) - 1L;// 序列号的最大值
    private long sequenceMask = (-1L ^ (-1L << sequenceBits));

    private IdGenerator() {

    }

    public static IdGenerator getInstance() {
        return INSTANCE;
    }

    public boolean init(long machineId) {
        // sanity check for workerId
        if (machineId > maxMachineId || machineId < 0) {
            throw new IllegalArgumentException(
                    String.format("machine Id can't be greater than %d or less than 0", maxMachineId));
        }
        LogUtil.info("ID generator starting. timestamp left shift " + timestampLeftShift + ", machine id bits "
                + machineIdBits + ", sequence bits " + sequenceBits + ", machineId " + machineId);
        this.machineId = machineId;
        lastId = new AtomicLong((timeGen() - twepoch) << timestampLeftShift | randStartSequence());
        if (lastId == null) {
            LogUtil.error("init lastId is Null");
            return false;
        }

        initShortId();
        return true;
    }

    private void initShortId() {
        //获取当前数据库最大的shortId
        playerDAO dao = (playerDAO) playerUpdateCacheL.getInstance().getDao();
        Integer maxShortId = dao.getMaxShortId();
        if (maxShortId == null) {
            maxShortId = 0;
        }

        if (maxShortId < BASE_SHORT_ID) {
            shortIdGenerator.set(BASE_SHORT_ID);
        } else {
            shortIdGenerator.set(maxShortId);
        }
    }

    public boolean reload(long machineId) {
        if (machineId > maxMachineId || machineId < 0) {
            return false;
        }
        LogUtil.info("ID generator starting. timestamp left shift " + timestampLeftShift + ", machine id bits "
                + machineIdBits + ", sequence bits " + sequenceBits + ", machineId " + machineId);
        this.machineId = machineId;
        return true;
    }

    private long randStartSequence() {
        return ThreadLocalRandom.current().nextInt(10);
        // new Random().nextInt(10);
    }

    private IdGenerator(long machineId) {
        init(machineId);
    }

    /**
     * 最高位固定为0
     * +------------------------+---------------------------+-----------------+
     * | 39bits timestamp in ms | 12bits worker(machine) ID | 12bits sequence |
     * +------------------------+---------------------------+-----------------+
     * @return
     */
    public long generateIdNum() {
        //防止id冲突,获取id间隔不能小于1毫秒,不然会出现id相同
        GameUtil.sleep(1);

        while (true) {
            long now = timeGen();
            long oldId = lastId != null ? lastId.get() : 0;
            long lastTimeInterval = oldId >>> timestampLeftShift;
            long nowInterval = now - twepoch;
            long sequence;

            if (nowInterval < lastTimeInterval) {
                LogUtil.warn("generate id yield for time interval");
                Thread.yield();
                continue;
            } else if (nowInterval == lastTimeInterval) {
                sequence = oldId & sequenceMask;
                if (sequence >= maxSequence) {
                    now = tillNextMillis(now);
                    nowInterval = now - twepoch;
                    sequence = randStartSequence();
                } else {
                    sequence = sequence + 1;
                }
            } else {
                sequence = randStartSequence();
            }
            long newId = (nowInterval << timestampLeftShift) | (machineId << machineIdShift) | sequence;
            if (lastId == null) {
                lastId = new AtomicLong(newId);
            } else if (!lastId.compareAndSet(oldId, newId)) {
                LogUtil.warn("generate id yield for oldId set failed:oldId=" + oldId + ",newId=" + newId);
                Thread.yield();
                continue;
            }
            return newId;
        }
    }

    public String generateId() {
        return String.valueOf(generateIdNum());
    }

    private long tillNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            Thread.yield();
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen(){
        return System.currentTimeMillis();
    }

    /**
     * 生成短ID
     *
     * @return
     */
    public int generateShortId() {
        int shortId = 0;
        for (int i = 0; i < 5; i++) {
            shortId = this.shortIdGenerator.incrementAndGet();
            if (playerCache.getInstance().getPlayerByShortId(shortId) != null) {
                initShortId();
            } else {
                break;
            }
        }

        if (shortId == 0) {
            LogUtil.error("generate short id failed");
        }

        return shortId;
    }
}
