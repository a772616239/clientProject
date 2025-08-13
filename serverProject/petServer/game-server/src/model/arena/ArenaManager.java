package model.arena;

import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaFreeTickets;
import cfg.ArenaFreeTicketsObject;
import cfg.ArenaRankingReward;
import cfg.ArenaRankingRewardObject;
import cfg.MailTemplateConfig;
import cfg.MailTemplateUsed;
import cfg.MailTemplateUsedObject;
import cfg.RewardConfig;
import cfg.ServerStringRes;
import common.GameConst;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ArenaRankingInfo;
import protocol.ServerTransfer.CS_GS_ArenaRankingSettle;
import protocol.ServerTransfer.GS_CS_RequestArenaRankInfo;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @data 2020.03.09
 */
public class ArenaManager implements Tickable {
    private static ArenaManager instance;

    public static ArenaManager getInstance() {
        if (instance == null) {
            synchronized (ArenaManager.class) {
                if (instance == null) {
                    instance = new ArenaManager();
                }
            }
        }
        return instance;
    }

    private ArenaManager() {
    }

    /**
     * 发放排行结算的奖励邮件模板id
     **/
    private int dailyTemplate;
    private int weeklyTemplate;

    /**
     * 上次发放竞技场门票时间
     */
    private volatile long lastSendFreeTicketsTime;

    private long updatePlayerRankTime;

    /**
     * <RoomId,>
     */
    private final Map<String, ArenaRankingInfo> arenaRankingInfo = new ConcurrentHashMap<>();

    public boolean init() {
        MailTemplateUsedObject mailTemplateCfg = MailTemplateUsed.getById(GameConst.CONFIG_ID);
        if (mailTemplateCfg == null
                || MailTemplateConfig.getByTemplateid(mailTemplateCfg.getArenadaily()) == null
                || MailTemplateConfig.getByTemplateid(mailTemplateCfg.getArenaweekly()) == null) {
            LogUtil.error("mail template cfg is error");
            return false;
        }

        this.dailyTemplate = mailTemplateCfg.getArenadaily();
        this.weeklyTemplate = mailTemplateCfg.getArenaweekly();
        this.updatePlayerRankTime = 0;
        GlobalTick.getInstance().addTick(this);
        return checkCfg();
    }

    private boolean checkCfg() {
        return checkRankingReward();
    }

    private boolean checkRankingReward() {
        for (ArenaRankingRewardObject value : ArenaRankingReward._ix_id.values()) {
            if (value.getId() <= 0) {
                continue;
            }

            if (RewardConfig.getByRewardid(value.getDailyrewards()) == null) {
                LogUtil.error("arena ranking cfg error, reward id not exist:" + value.getDailyrewards() + ", cfg id:" + value.getId());
                return false;
            }

            if (RewardConfig.getByRewardid(value.getWeeklyrewards()) == null) {
                LogUtil.error("arena ranking cfg error, reward id not exist:" + value.getWeeklyrewards() + ", cfg id:" + value.getId());
                return false;
            }
        }

        return true;
    }

    /**fo
     * 结算排行榜奖励
     * @param settle
     */
    public void doRankingReward(CS_GS_ArenaRankingSettle settle) {
        if (settle == null) {
            return;
        }
        for (Entry<String, Integer> entry : settle.getRankingInfoMap().entrySet()) {
            //非本服玩家
            if (!PlayerUtil.playerIsExist(entry.getKey())) {
                continue;
            }

            int rewardId = ArenaRankingReward.getRewardByDanIdAndRanking(settle.getDan(),
                    entry.getValue(), settle.getType());
            addMailToPlayer(entry.getKey(), rewardId, settle.getType(),
                    getDanName(entry.getKey(), settle.getDan()), String.valueOf(entry.getValue()));
        }
    }

    private void addMailToPlayer(String playerIdx, int rewardId, int type, String danName, String ranking) {
        List<Reward> rewards = RewardUtil.getRewardsByRewardId(rewardId);
        if (GameUtil.collectionIsEmpty(rewards)) {
            LogUtil.warn("model.arena.ArenaManager.addMailToPlayer, reward is reward is empty, reward id:" + rewardId);
        }

        int mailTemplate = type == 1 ? dailyTemplate : weeklyTemplate;

        EventUtil.triggerAddMailEvent(playerIdx, mailTemplate, rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena), danName, ranking);
    }

    private String getDanName(String playerIdx, int dan) {
        ArenaDanObject danCfg = ArenaDan.getById(dan);
        if (danCfg == null) {
            return "";
        }
        return ServerStringRes.getContentByLanguage(danCfg.getServername(), PlayerUtil.queryPlayerLanguage(playerIdx));
    }

    @Override
    public void onTick() {
        sendFreeTicket();
        updateArenaRanking();
    }

    private void updateArenaRanking() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updatePlayerRankTime < curTime) {
            updatePlayerRankTime = curTime + TimeUtil.MS_IN_A_MIN;
            //TODO 更新排名信息

            GS_CS_RequestArenaRankInfo.Builder builder = GS_CS_RequestArenaRankInfo.newBuilder();
            builder.setDan(ArenaDan.getInstance().getMaxDan());
            builder.setLimit(200);
            CrossServerManager.getInstance().sendMsgToArena(MsgIdEnum.GS_CS_RequestArenaRankInfo_VALUE, builder);
        }
    }


    private void sendFreeTicket() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        ArenaFreeTicketsObject freeTicketCfg = ArenaFreeTickets.getById(TimeUtil.getDayOfWeek(currentTime));
        if (freeTicketCfg == null) {
            return;
        }

        if (lastSendFreeTicketsTime != 0) {
            long todayStamp = TimeUtil.getTodayStamp(currentTime);
            int[] sendTime = freeTicketCfg.getSendtime();
            for (int time : sendTime) {
                long cfgTime = todayStamp + time * TimeUtil.MS_IN_A_MIN;
                if (GameUtil.inScope(lastSendFreeTicketsTime, currentTime, cfgTime)) {
                    addMailToAllSatisfyPlayer(freeTicketCfg);
                }
            }
        }
        lastSendFreeTicketsTime = currentTime;
    }

    /**
     * 发放邮件到所有满足条件的玩家
     * @param freeTicketCfg
     */
    private void addMailToAllSatisfyPlayer(ArenaFreeTicketsObject freeTicketCfg) {
        if (freeTicketCfg == null) {
            return;
        }

        List<String> playerIdxList = playerCache.getInstance().getAllPlayerIdx();
        if (CollectionUtils.isEmpty(playerIdxList)) {
            return;
        }

        for (String idx : playerIdxList) {
            if (PlayerUtil.queryPlayerLv(idx) < freeTicketCfg.getLvlimit()) {
                continue;
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena);
            EventUtil.triggerAddMailEvent(idx, freeTicketCfg.getMailtemplate(), null, reason);
        }
        LogUtil.info("ArenaManager.addMailToAllSatisfyPlayer, send finished, cfg info：" + freeTicketCfg);
    }

    public void updateRankingInfo(List<ArenaRankingInfo> newRankInfo) {
        if (CollectionUtils.isEmpty(newRankInfo)) {
            return;
        }
        for (ArenaRankingInfo rankingInfo : newRankInfo) {
            this.arenaRankingInfo.put(rankingInfo.getRoomId(), rankingInfo);
        }
    }

    public int getPlayerRank(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return -1;
        }

        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        if(entity == null) {
            return -1;
        }

        String roomId = entity.getDbBuilder().getRoomId();
        if (StringUtils.isBlank(roomId)) {
            return -1;
        }

        ArenaRankingInfo rankingInfo = this.arenaRankingInfo.get(roomId);
        if (rankingInfo == null) {
            return -1;
        }
        Integer result = rankingInfo.getArenaRankInfoMap().get(playerIdx);
        return result == null ? -1 : result;
    }
}
