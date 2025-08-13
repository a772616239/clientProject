/**
 * created by tool DAOGenerate
 */
package model.arena.entity;

import cfg.ArenaConfig;
import cfg.ArenaDan;
import cfg.ArenaDanObject;
import common.GameConst;
import common.GlobalData;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.obj.BaseObj;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.ArenaRecord;
import protocol.Arena.SC_AddNewArenaRecord;
import protocol.Arena.SC_HitBackSuccessfully;
import protocol.ArenaDB.DB_Arena;
import protocol.ArenaDB.DB_Arena.Builder;
import protocol.Battle.PlayerExtDataDict;
import protocol.Battle.PlayerExtDataEnum;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class arenaEntity extends BaseObj {

    public String getClassType() {
        return "arenaEntity";
    }

    @Override
    public void putToCache() {
        arenaCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.arenainfo = getDbBuilder().build().toByteArray();
    }

    /**
     *
     */
    private String playeridx;

    /**
     *
     */
    private byte[] arenainfo;


    /**
     * 获得
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得
     */
    public byte[] getArenainfo() {
        return arenainfo;
    }

    /**
     * 设置
     */
    public void setArenainfo(byte[] arenainfo) {
        this.arenainfo = arenainfo;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return playeridx;
    }

    private arenaEntity() {
    }

    public arenaEntity(String playerIdx) {
        this.playeridx = playerIdx;
    }

    private DB_Arena.Builder dbBuilder;

    public DB_Arena.Builder getDbBuilder() {
        if (dbBuilder == null) {
            dbBuilder = getDbInfo();
        }
        return dbBuilder;
    }

    private DB_Arena.Builder getDbInfo() {
        try {
            if (this.arenainfo != null) {
                return DB_Arena.parseFrom(arenainfo).toBuilder();
            } else {
                return DB_Arena.newBuilder();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            LogUtil.error("parse DB_Arena failed, return new builder, playerIdx:" + getPlayeridx());
            return DB_Arena.newBuilder();
        }
    }

    /**
     * 带直升标记的玩家，且未击败不刷新
     * <p>
     * 检查当前直升玩家是否未打败  且段位不匹配
     *
     * @param opponentList
     */
    public void refreshOpponent(List<ArenaOpponentTotalInfo> opponentList, Map<String, ArenaOpponentTotalInfo> specifyOpponentMap) {
        Builder dbBuilder = getDbBuilder();
        if (GameUtil.collectionIsEmpty(opponentList) || dbBuilder == null) {
            return;
        }

        //处理名字多语言,按照玩家当前语言处理,简体中文不处理
        LanguageEnum language = PlayerUtil.queryPlayerLanguage(getPlayeridx());
        List<ArenaOpponentTotalInfo> dealList = null;
        Map<String, ArenaOpponentTotalInfo> dealMap = null;
        if (language != LanguageEnum.LE_SimpleChinese) {
            dealList = opponentList.stream()
                    .map(e -> ArenaUtil.dealRobotName(e, language))
                    .collect(Collectors.toList());

            if (MapUtils.isNotEmpty(specifyOpponentMap)) {
                dealMap = specifyOpponentMap.values().stream()
                        .map(e -> ArenaUtil.dealRobotName(e, language))
                        .collect(Collectors.toMap(e -> e.getOpponnentInfo().getSimpleInfo().getPlayerIdx(), e -> e));
            }
        } else {
            dealList = opponentList;
            dealMap = specifyOpponentMap;
        }

        refreshToDB(dealList, dealMap);
    }

    private void refreshToDB(List<ArenaOpponentTotalInfo> opponentList, Map<String, ArenaOpponentTotalInfo> specifyOpponentMap) {
//        Builder dbBuilder = getDbBuilder();
//        if (GameUtil.collectionIsEmpty(opponentList) || dbBuilder == null) {
//            return;
//        }

        //需要添加的
        List<ArenaOpponentTotalInfo> needAdd = new ArrayList<>();
        //新的直升玩家
        List<ArenaOpponentTotalInfo> newDirectUp = new ArrayList<>();

        for (String unBeat : getUnBeatDirectUpPlayer()) {
            //检查当前直升玩家段位是否不匹配
            ArenaOpponentTotalInfo specifyOpponent =
                    MapUtils.isEmpty(specifyOpponentMap) ? null : specifyOpponentMap.get(unBeat);
            if (specifyOpponent != null
                    && specifyOpponent.getOpponnentInfo().getSimpleInfo().getDan() == dbBuilder.getDan()) {
                ArenaOpponentTotalInfo info = dbBuilder.getOpponentMap().get(unBeat);
                if (info != null) {
                    needAdd.add(info);
                }
            }
        }

        for (ArenaOpponentTotalInfo info : opponentList) {
            //跳过段位不匹配的对手
            if (info.getOpponnentInfo().getSimpleInfo().getDan() != dbBuilder.getDan()) {
                continue;
            }

            if (info.getOpponnentInfo().getDerectUp()) {
                newDirectUp.add(info);
            } else {
                needAdd.add(info);
            }
        }

        //清除数据
        dbBuilder.clearOpponent();
        dbBuilder.clearVictoryIdx();

        //添加数据
        for (ArenaOpponentTotalInfo info : needAdd) {
            dbBuilder.putOpponent(info.getOpponnentInfo().getSimpleInfo().getPlayerIdx(), info);
        }

        //数据不足还需要添加直升玩家
        if (getDbBuilder().getOpponentCount() < opponentList.size()) {
            int needAddSize = Math.min(newDirectUp.size(), opponentList.size() - getDbBuilder().getOpponentCount());
            for (int i = 0; i < needAddSize; i++) {
                ArenaOpponentTotalInfo info = newDirectUp.get(i);
                if (info == null) {
                    continue;
                }
                dbBuilder.putOpponent(info.getOpponnentInfo().getSimpleInfo().getPlayerIdx(), info);
            }
        }
        dbBuilder.setLastRefreshTime(GlobalTick.getInstance().getCurrentTime());
    }

    /**
     * 获取未打败的直升玩家id
     *
     * @return
     */
    public List<String> getUnBeatDirectUpPlayer() {
        List<String> result = new ArrayList<>();
        if (getDbBuilder().getOpponentCount() <= 0) {
            return result;
        }

        List<ArenaOpponentTotalInfo> opponents = new ArrayList<>(getDbBuilder().getOpponentMap().values());
        for (ArenaOpponentTotalInfo opponent : opponents) {
            String playerIdx = opponent.getOpponnentInfo().getSimpleInfo().getPlayerIdx();
            if (opponent.getOpponnentInfo().getDerectUp()
                    && !getDbBuilder().getVictoryIdxList().contains(playerIdx)) {
                result.add(playerIdx);
            }
        }
        return result;
    }

    public boolean canChallenge(String challengeIdx) {
        ArenaOpponentTotalInfo opponentInfo = getOpponentInfo(challengeIdx);
        return opponentInfo != null
                //不同段位不能挑战
                && opponentInfo.getOpponnentInfo().getSimpleInfo().getDan() == getDbBuilder().getDan()
                && !getDbBuilder().getVictoryIdxList().contains(challengeIdx);
    }


    public ArenaOpponentTotalInfo getOpponentInfo(String opponentIdx) {
        if (StringUtils.isBlank(opponentIdx)) {
            return null;
        }
        Builder dbBuilder = getDbBuilder();
        if (dbBuilder == null) {
            return null;
        }
        ArenaOpponentTotalInfo result = dbBuilder.getOpponentMap().get(opponentIdx);
        if (result == null) {
            result = dbBuilder.getTempOpponentMap().get(opponentIdx);
        }

        return result;
    }

    /**
     * 添加和发送刷新战绩消息
     *
     * @param record
     */
    public void addBattleRecord(ArenaRecord record) {
        if (record == null) {
            return;
        }

        Builder dbBuilder = getDbBuilder();
        if (dbBuilder == null) {
            return;
        }

        if (dbBuilder.getRecordsCount() > ArenaConfig.getById(GameConst.CONFIG_ID).getSaverecordcount()) {
            //移除第一位记录
            dbBuilder.removeRecords(0);
        }
        dbBuilder.addRecords(record);

        if (record.getBattleResult() == 1) {
            //如果不是挑战列表内的玩家不用加入记录
            getDbBuilder().addVictoryIdx(record.getOpponentIdx());
            //修改回击状态
            hitBackSuccessfully(record.getOpponentIdx());
        }

        sendNewRefreshRecord(record);
    }

//    /**
//     * 判断是否是临时挑战玩家
//     * @param
//     * @return
//     */
//    private boolean isTempOpponent(String opponentIdx) {
//        if (StringUtils.isEmpty(opponentIdx)) {
//            return false;
//        }
//        return getDbBuilder().getTempOpponentMap().containsKey(opponentIdx);
//    }

    private void sendNewRefreshRecord(ArenaRecord record) {
        SC_AddNewArenaRecord.Builder builder = SC_AddNewArenaRecord.newBuilder();
        builder.setRecord(record);
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_AddNewArenaRecord_VALUE, builder);
    }

    public void addTempOpponent(ArenaOpponentTotalInfo totalInfo) {
        if (totalInfo == null) {
            return;
        }
        String playerIdx = totalInfo.getOpponnentInfo().getSimpleInfo().getPlayerIdx();
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        getDbBuilder().putTempOpponent(playerIdx, totalInfo);
    }

    public void updateDailyData() {
        getDbBuilder().clearTodayBuyTicketCount();
        getDbBuilder().clearTodayFreeChallengeTimes();
    }

    public PlayerExtDataDict buildArenaSpecialInfo() {
        PlayerExtDataDict.Builder extData = PlayerExtDataDict.newBuilder();
        extData.addKeys(PlayerExtDataEnum.PEDE_ServerIndex);
        extData.addValues(ServerConfig.getInstance().getServer());

        extData.addKeys(PlayerExtDataEnum.PEDE_Arena_Score);
        extData.addValues(getDbBuilder().getScore());

        extData.addKeys(PlayerExtDataEnum.PEDE_Arena_Rank);
        extData.addValues(getDbBuilder().getRanking());
        return extData.build();
    }

    public void hitBackSuccessfully(String opponentIdx) {
        if (StringUtils.isBlank(opponentIdx)) {
            return;
        }

        boolean needRefresh = false;
        for (ArenaRecord.Builder recordBuilder : getDbBuilder().getRecordsBuilderList()) {
            if (Objects.equals(recordBuilder.getOpponentIdx(), opponentIdx)
                    && recordBuilder.getBattleResult() == 2
                    && !recordBuilder.getHitBackSuccessfully()) {
                recordBuilder.setHitBackSuccessfully(true);
                needRefresh = true;
            }
        }

        if (needRefresh) {
            SC_HitBackSuccessfully.Builder builder = SC_HitBackSuccessfully.newBuilder();
            builder.setOpponentIdx(opponentIdx);
            GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_HitBackSuccessfully_VALUE, builder);
        }
    }

    /**
     * 段位达成奖励
     */
    public void doDanReachReward() {
        List<Integer> alreadyClaimDanRewardList = getDbBuilder().getAlreadyClaimDanRewardList();
        for (ArenaDanObject value : ArenaDan._ix_id.values()) {
            if (value.getId() <= 0) {
                continue;
            }

            //奖励未领取且当前段位小于等于玩家段位
            if (alreadyClaimDanRewardList.contains(value.getId()) || value.getId() > getDbBuilder().getDan()) {
                continue;
            }

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(value.getDanreachreward());
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.warn("arenaEntity.doDanReachReward, dan reach reward is empty, dan:" + value.getId());
                continue;
            }

            //发放奖励
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena);
            RewardManager.getInstance().doRewardByList(getPlayeridx(), rewards, reason, false);

            //添加领取记录
            getDbBuilder().addAlreadyClaimDanReward(value.getId());
        }
    }

    /**
     * 判断需要挑战的玩家是否在记录中且存在防守失败
     *
     * @param challengeIdx
     * @return
     */
    public boolean existDefendAndFailRecord(String challengeIdx) {
        if (StringUtils.isBlank(challengeIdx)) {
            return false;
        }

        for (ArenaRecord arenaRecord : getDbBuilder().getRecordsList()) {
            if (Objects.equals(challengeIdx, arenaRecord.getOpponentIdx())
                    && arenaRecord.getBattleType() == 2
                    && arenaRecord.getBattleResult() == 2) {
                return getOpponentInfo(challengeIdx) != null;
            }
        }

        return false;
    }

    /**
     * 判断玩家在挑战列表内且为挑战
     *
     * @param challengeIdx
     * @return
     */
    public boolean existInChallengeListAndUnBeat(String challengeIdx) {
        if (StringUtils.isBlank(challengeIdx)) {
            return false;
        }

        return getDbBuilder().getOpponentMap().containsKey(challengeIdx)
                && !getDbBuilder().getVictoryIdxList().contains(challengeIdx);
    }
}