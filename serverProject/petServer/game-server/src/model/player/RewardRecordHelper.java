package model.player;

import common.RewardRecordConst;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import util.LogUtil;

public class RewardRecordHelper {

    public static void updatePlayerDailyRecord(String playerId, int recordIndex) {
        updatePlayerRecord(playerId, recordIndex, RewardRecordConst.RewardRecordEnum.DailyRewardRecord);
    }


    public static void updatePlayerOnceRecord(String playerId, int recordIndex) {
        updatePlayerRecord(playerId, recordIndex, RewardRecordConst.RewardRecordEnum.OnceRewardRecord);
    }


    public static void updatePlayerRecord(String playerId, int recordIndex, RewardRecordConst.RewardRecordEnum recordEnum) {
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        long record = getRecordInDb(player, recordEnum);
        long newRecord = modifyIndexOfLongValueTO1(record, recordIndex);
        modifyToPlayerEntity(player, newRecord, recordEnum);
    }

    private static void modifyToPlayerEntity(playerEntity player, long newRecord, RewardRecordConst.RewardRecordEnum recordEnum) {
        switch (recordEnum) {
            case OnceRewardRecord:
                LogUtil.info("Before modifyToPlayerEntity playerIdx:{} ,beforeRecord:{},recordEnum:{}", player.getIdx(), player.getOncerewardreward(), recordEnum);
                player.setOncerewardreward(newRecord);
                LogUtil.info("After modifyToPlayerEntity playerIdx:{} ,newRecord:{},recordEnum:{}", player.getIdx(), player.getOncerewardreward(), recordEnum);
                break;
            case DailyRewardRecord:
                player.setDailyrewardrecord(newRecord);
        }
    }

    private static long modifyIndexOfLongValueTO1(long value, int index) {
        if (index > Long.SIZE - 1) {
            throw new RuntimeException("indexOfLongValueZero error by index: " + index + "value: " + value);
        }
        if (!indexOfLongValueZero(index, value)) {
            LogUtil.warn("modifyIndexOfLongValueTO1 not need modify,value:" + value + " index:" + index);
            return value;
        }
        return value ^ ((value & (1 << index)) ^ (1 << index));
    }

    public static String LongToBinary(long num) {
        StringBuilder binStr = new StringBuilder();
        for (int i = Long.SIZE - 1; i >= 0; i--) {
            binStr.append(num >>> i & 1);
        }
        return binStr.toString();
    }


    public static boolean dailyRewardClaimed(String playerId, int recordIndex) {
        return alreadyClaimed(playerId, recordIndex, RewardRecordConst.RewardRecordEnum.DailyRewardRecord);

    }


    public static boolean onceRewardClaimed(String playerId, int recordIndex) {
        return alreadyClaimed(playerId, recordIndex, RewardRecordConst.RewardRecordEnum.OnceRewardRecord);

    }

    private static boolean alreadyClaimed(String playerId, int recordIndex, RewardRecordConst.RewardRecordEnum recordEnum) {
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return true;
        }
        long record = getRecordInDb(player, recordEnum);

        return !indexOfLongValueZero(recordIndex, record);
    }

    private static long getRecordInDb(playerEntity player, RewardRecordConst.RewardRecordEnum recordEnum) {
        if (player == null || recordEnum == null) {
            throw new RuntimeException("getRecordInDb error by params is null");
        }
        switch (recordEnum) {
            case OnceRewardRecord:
                return player.getOncerewardreward();
            case DailyRewardRecord:
                return player.getDailyrewardrecord();
        }
        throw new RuntimeException("getRecordInDb error by recordEnum wrong,recordEnum: " + recordEnum);
    }

    private static boolean indexOfLongValueZero(int index, long value) {
        if (index > Long.SIZE - 1) {
            throw new RuntimeException("indexOfLongValueZero error by index: " + index + "value: " + value);
        }
        return ((value >> index)) % 2 == 0;
    }

}
