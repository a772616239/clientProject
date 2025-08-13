package model.magicthron;

import cfg.GameConfig;
import cfg.ShuraArenaBossConfig;
import cfg.ShuraArenaBossConfigObject;
import cfg.ShuraArenaConfig;
import cfg.ShuraArenaConfigObject;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;
import common.SyncExecuteFunction;
import common.entity.RankingQuerySingleResult;
import common.entity.WorldMapData;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import db.entity.BaseEntity;
import model.battlerecord.dbCache.battlerecordCache;
import model.battlerecord.entity.battlerecordEntity;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.magicthron.dbcache.magicthronCache;
import model.magicthron.entity.MagicThronExInfo;
import model.magicthron.entity.magicthronEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.ranking.ranking.AbstractRanking;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Activity.EnumRankingType;
import protocol.Battle.BattleStatisticData;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.GameplayDB.MagicThronDB;
import protocol.MagicThron.MagicArenaInfo;
import protocol.MagicThron.MagicArenaPlayerVo;
import protocol.MagicThron.MagicThronRank;
import protocol.MagicThron.MagicThronRecord;
import protocol.MagicThron.SC_MagicThronPanel;
import protocol.MagicThron.SC_MagicThronRank;
import protocol.MagicThron.SC_MagicThronRecord;
import protocol.MagicThronDB.DB_MagicThron;
import protocol.MagicThronDB.MagicBattleRecord;
import protocol.MessageId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.LogUtil;
import util.MapUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static common.GameConst.RedisKey.MagicThron;

/**
 * @author Hammer
 */
public class MagicThronManager implements GamePlayerUpdate {

    // 元素周
    private int commonBuff = 0;

    private MagicThronDB.Builder magicThronDB = null;

    // 战斗记录数量控制
    private Map<String, Integer> battleNum = new ConcurrentHashMap<>();


    public Integer findPlayerArea(String playerIdx) {
        AbstractRanking ranking = RankingManager.getInstance().getRanking(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_Team1Ability));
        if (ranking == null) {
            return null;
        }
        int playerRanking = ranking.queryPlayerRanking(playerIdx);
        if (playerRanking==-1){
            return MaxArea;
        }
        for (ShuraArenaConfigObject areaCfg : ShuraArenaConfig._ix_id.values()) {
            if (areaCfg.getRankrange().length < 2) {
                continue;
            }
            if (areaCfg.getRankrange()[0] <= playerRanking && (areaCfg.getRankrange()[1] == -1 || areaCfg.getRankrange()[1] > playerRanking)) {
                return areaCfg.getId();
            }
        }
        return null;
    }

    public int getCommonBuff() {
        return this.commonBuff;
    }

    public int getFightMakeId(Integer areaId, int difficult) {
        ShuraArenaBossConfigObject bossCfg = MagicThronManager.getInstance().getBossCfgByAreaAndDifficult(areaId, difficult);
        if (bossCfg == null) {
            return 0;
        }
        int week = getMagicThronDB().getWeek();
        int length = bossCfg.getFightmakeid().length;
        if (length <= 0) {
            return 0;
        }
        return bossCfg.getFightmakeid()[week % length];
    }

    private MagicThronDB.Builder getMagicThronDB() {
        return magicThronDB;
    }

    public int findPlayerRank(String playerIdx) {
        AbstractRanking ranking = RankingManager.getInstance().getRanking(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_Team1Ability));

        if (ranking == null) {
            return -1;
        }
        return ranking.queryPlayerRanking(playerIdx);

    }


    private static class LazyHolder {
        private static final MagicThronManager INSTANCE = new MagicThronManager();
    }

    private MagicThronManager() {
    }

    public static MagicThronManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean init() {
        clearOldData();
        initBattleNum();
        loadMagicThronDB();
        initCommonBuff();
        gameplayCache.getInstance().addToUpdateSet(this);
        MaxArea = ShuraArenaConfig._ix_id.keySet().stream().max(Integer::compareTo).get();
        return true;
    }

    private int MaxArea;

    private void loadMagicThronDB() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_MagicThron);
        if (entity == null) {
            return;
        }
        if (entity.getGameplayinfo() == null) {
            magicThronDB = MagicThronDB.newBuilder();
            return;
        }
        try {
            magicThronDB = MagicThronDB.parseFrom(entity.getGameplayinfo()).toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void clearOldData() {
        String clearOldDataKey = MagicThron + "clearOldData:" + ServerConfig.getInstance().getServer();
        if (JedisUtil.jedis.exists(clearOldDataKey)) {
            return;
        }
        for (String idx : magicthronCache.getInstance()._ix_id.keySet()) {
            magicthronCache.remove(idx);
        }
        JedisUtil.jedis.set(clearOldDataKey, GlobalTick.getInstance().getCurrentTime() + "");
    }


    /**
     * 主面板
     *
     * @param playerId
     */
    public void getPanel(String playerId) {
        SC_MagicThronPanel.Builder msg = SC_MagicThronPanel.newBuilder();
        magicthronEntity magicthron = magicthronCache.getByIdx(playerId);
        if (magicthron == null) {
            if (StringUtils.isEmpty(playerId)) {
                return;
            }
            magicthron = initEntity(playerId);
        }
        AbstractRanking ranking = RankingManager.getInstance().getRanking(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_Team1Ability));

        if (ranking != null) {
            msg.addAllArenaInfo(buildShowAreaInfo(ranking));
            msg.setPlayerRank(ranking.queryPlayerRanking(playerId));
            msg.setExpireTime(ranking.getNextUpdateRankingTime() + RandomUtils.nextInt(1000) + 500);
        }

        DB_MagicThron.Builder magicThron = magicthron.getInfoDB();
        msg.setBossTimes(MapUtil.map2IntMap(magicThron.getBossTimesMap()));
        msg.setPlayerLastDamage(magicThron.getLastDamage());
        msg.setPlayerCumuDamage(magicThron.getCumuDamage());
        Integer playerArea = findPlayerArea(playerId);
        if (playerArea == null) {
            playerArea = MaxArea;
        }

        msg.setBuff(commonBuff);
        msg.setFightMakeId(getFightMakeId(playerArea, 1));
        msg.setPlayerArea(playerArea);

        msg.setResult(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronPanel_VALUE, msg);
    }


    private magicthronEntity initEntity(String playerId) {
        magicthronEntity magicthronEntity = new magicthronEntity();
        magicthronEntity.setIdx(playerId);
        magicthronEntity.setInfoDB(DB_MagicThron.newBuilder());
        SyncExecuteFunction.executeConsumer(magicthronEntity, cache -> {

        });
        return magicthronEntity;
    }

    private List<MagicArenaInfo> buildShowAreaInfo(AbstractRanking ranking) {

        List<RankingQuerySingleResult> totalRanking = ranking.getRankingTotalInfoList();
        if (CollectionUtils.isEmpty(totalRanking)) {
            return Collections.emptyList();
        }

        int showEnd;
        int showStart;
        List<MagicArenaInfo> result = new ArrayList<>();
        for (ShuraArenaConfigObject cfg : ShuraArenaConfig._ix_id.values()) {
            MagicArenaInfo.Builder areaInfo = MagicArenaInfo.newBuilder().setId(cfg.getId());
            if (cfg.getRankrange().length < 2) {
                continue;
            }
            showStart = cfg.getRankrange()[0] - 1;
            if (showStart >= totalRanking.size()) {
                return result;
            }
            showEnd = Math.min(totalRanking.size(), showStart + cfg.getShowsize() - 1);
            List<RankingQuerySingleResult> areaRankInfo = totalRanking.subList(showStart, showEnd);
            for (RankingQuerySingleResult rankItem : areaRankInfo) {
                MagicArenaPlayerVo.Builder playerVo = MagicArenaPlayerVo.newBuilder();
                playerEntity playerEntity = playerCache.getByIdx(rankItem.getPrimaryKey());
                if (playerEntity == null) {
                    continue;
                }
                playerVo.setPlayerIdx(playerEntity.getIdx());
                playerVo.setPlayerName(playerEntity.getName());
                playerVo.setAvatar(playerEntity.getAvatar());
                playerVo.setTitleId(playerEntity.getTitleId());
                playerVo.setAbility(rankItem.getPrimaryScore());
                playerVo.setAvatarBorder(playerEntity.getDb_data().getCurAvatarBorder());

                areaInfo.addPlayers(playerVo.build());
            }
            result.add(areaInfo.build());
        }
        return result;
    }


    /**
     * 排行榜
     *
     * @param playerId
     */
    public void getRankPanel(String playerId, int area) {
        SC_MagicThronRank.Builder builder = SC_MagicThronRank.newBuilder();

        AbstractRanking ranking = RankingManager.getInstance().getRanking(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_Team1Ability));

        ShuraArenaConfigObject cfg = ShuraArenaConfig.getById(area);
        if (cfg == null || cfg.getRankrange().length < 2) {
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronRank_VALUE, builder);
            return;
        }

        if (ranking != null) {
            builder.setExpireTime(ranking.getNextUpdateRankingTime()+RandomUtils.nextInt(1000)+1000);
            List<RankingQuerySingleResult> rankList = ranking.getSortTotalRanking();
            int oneShowNum = cfg.getShowsize();
            int rankStart = cfg.getRankrange()[0] - 1;
            if (rankStart>= rankList.size()){
                GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronRank_VALUE, builder);
                return ;
            }
            int rankEnd = Math.min(rankList.size(), rankStart + oneShowNum);
            rankList = rankList.subList(rankStart, rankEnd);
            for (RankingQuerySingleResult item : rankList) {
                builder.addRank(buildMagicRank(playerId, item));
            }
        }

        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronRank_VALUE, builder);
    }

    private protocol.MagicThron.MagicThronRank.Builder buildMagicRank(String playerId, RankingQuerySingleResult item) {
        MagicThronRank.Builder builder = protocol.MagicThron.MagicThronRank.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player != null) {
            builder.setPlayerId(playerId);
            builder.setName(player.getName());
            builder.setAvatar(player.getAvatar());
        }
        builder.setAbility(item.getPrimaryScore());
        builder.setRank(item.getRanking());
        if (item.getExtInfo() != null) {
            MagicThronExInfo exInfo = JSON.parseObject(item.getExtInfo(), MagicThronExInfo.class);
            if (exInfo != null) {
                builder.setMaxDamageOnce(exInfo.getMaxDamageOnce());
                builder.setSumuDamage(exInfo.getSumuDamage());
            }
        }
        return builder;
    }

    private int getMagicTronWeek(int week) {
        int[] buffArr = GameConfig.getById(GameConst.CONFIG_ID).getMagicbuff();
        if (week >= buffArr.length && buffArr.length > 0) {
            return buffArr[week % buffArr.length];
        }
        return buffArr[week];
    }

    private int getWeekBuff(int week) {
        int[] buffArr = GameConfig.getById(GameConst.CONFIG_ID).getMagicbuff();
        if (week >= buffArr.length && buffArr.length > 0) {
            return buffArr[week % buffArr.length];
        }
        return buffArr[week];
    }

    public void magicThronRecord(String playerId) {
        SC_MagicThronRecord.Builder b = SC_MagicThronRecord.newBuilder();

        magicthronEntity player = magicthronCache.getByIdx(playerId);
        if (player == null) {
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronRecord_VALUE, b);
            return;
        }
        List<MagicBattleRecord> recordList = player.getInfoDB().getRecordList();
        for (MagicBattleRecord e : recordList) {
            battlerecordEntity entity = battlerecordCache.getInstance().getEntity(e.getBattleId());
            if (entity != null) {
                SyncExecuteFunction.executeFunction(player, p -> {
                    BattleStatisticData statisticData = entity.getServerBattleRecordBuilder().getStatisticData();
                    MagicThronRecord.Builder record = MagicThronRecord.newBuilder();

                    record.setRecord(statisticData);
                    long time = 0;
                    long power = 0;
                    for (MagicBattleRecord ee : player.getInfoDB().getRecordList()) {
                        if (ee.getBattleId().equals(e.getBattleId())) {
                            time = ee.getTime();
                            power = ee.getPower();
                            break;
                        }
                    }
                    record.setTime(time);
                    record.setPower(power);
                    b.addRecord(record);
                    return null;
                });

            }
        }
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_MagicThronRecord_VALUE, b);
    }

    public void deleteBattleNum(String battleId) {
        int num = battleNum.getOrDefault(battleId, 0);
        if (num <= 1) {
            battleNum.remove(battleId);
            battlerecordCache.remove(battleId);
        } else {
            battleNum.put(battleId, num - 1);
        }
    }

    public void addBattleNum(String battleId, int num) {

        if (battleNum.containsKey(battleId)) {
            battleNum.put(battleId, battleNum.getOrDefault(battleId, 0) + num);
        } else {
            battleNum.put(battleId, num);
        }
    }


    public long updateWeek() {
        initCommonBuff();
        updateWeeklyBoss();
        long now = GlobalTick.getInstance().getCurrentTime();
        long nextFreshTime = TimeUtil.getNextWeekResetStamp(now);
        magicthronCache.getInstance().updateWeeklyData();
        return nextFreshTime;
    }

    private void initCommonBuff() {
        int week = getMagicThronDB().getWeek();
        this.commonBuff = getWeekBuff(week);
    }

    private void updateWeeklyBoss() {
        WorldMapData worldMapInfo = GlobalData.getInstance().getWorldMapInfo();
        getMagicThronDB().setPetBaseLv(worldMapInfo.getPetLv());
    }

    public int getPetBaseLv() {
        int petBaseLv = getMagicThronDB().getPetBaseLv();
        if (petBaseLv <= 0) {
            petBaseLv = GlobalData.getInstance().getWorldMapInfo().getPetLv();
            getMagicThronDB().setPetBaseLv(petBaseLv);
        }
        return petBaseLv;
    }

    public ShuraArenaBossConfigObject getBossCfgByAreaAndDifficult(int area, int difficult) {
        return ShuraArenaBossConfig._ix_id.values().stream().filter(e -> e.getArea() == area && e.getDifficult() == difficult).findFirst().orElse(null);
    }

    private void initBattleNum() {
        Map<String, Integer> battleNum = new ConcurrentHashMap<>();
        for (Entry<String, BaseEntity> ent : magicthronCache.getInstance().getAll().entrySet()) {
            magicthronEntity player = (magicthronEntity) ent.getValue();
            for (MagicBattleRecord record : player.getInfoDB().getRecordList()) {
                int num = 0;
                if (battleNum.containsKey(record.getBattleId())) {
                    num = battleNum.get(record.getBattleId());
                }
                battleNum.put(record.getBattleId(), num + 1);

            }
        }
        this.battleNum = battleNum;
    }

    @Override
    public void update() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_MagicThron);
        entity.setGameplayinfo(this.magicThronDB.build().toByteArray());
        gameplayCache.put(entity);
    }
}
