package common;

import cfg.MarqueeTemplate;
import cfg.MarqueeTemplateObject;
import cfg.ServerStringRes;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.entity.WorldMapData;
import common.load.ServerConfig;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import model.mainLine.manager.MainLineManager;
import model.pet.PetManager;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Chat;
import protocol.Chat.SC_SystemChat;
import protocol.Common;
import protocol.Common.EnumMarqueeScene;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RuneReward;
import protocol.Common.SC_DisplayRewards;
import protocol.Common.SC_Marquee;
import protocol.Common.SC_RetCode;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import server.handler.chat.entity.SystemChatCreator;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.GS_BS_ForwardPlayerMsg_VALUE;

public class GlobalData {
    private static GlobalData instance = new GlobalData();
    /**
     * <playerIdx, gsTcpChn>
     */
    protected Map<String, GameServerTcpChannel> onlinePlayerMap;

    protected int maxOnlinePlayerCount = 0;

    public GlobalData() {
        this.onlinePlayerMap = new ConcurrentHashMap<>();
    }

    public static GlobalData getInstance() {
        return instance;
    }

    public boolean init() {
        maxOnlinePlayerCount = 100;
        return true;
    }

    public GameServerTcpChannel getOnlinePlayerChannel(String id) {
        return StringHelper.isNull(id) ? null : onlinePlayerMap.get(id);
    }

    public boolean checkPlayerOnline(String playerIdx) {
        GameServerTcpChannel gsChn = getOnlinePlayerChannel(playerIdx);
        return gsChn != null && gsChn.channel.isActive();
    }

    public void addOnlinePlayer(String idx, GameServerTcpChannel channel) {
        onlinePlayerMap.put(idx, channel);
    }

    public void removeOnlinePlayer(String idx) {
        onlinePlayerMap.remove(idx);
    }

    public boolean isServerFull() {
        int maxOnlinePlayerNum = ServerConfig.getInstance().getMaxOnlinePlayerNum();
        int onlineSize = onlinePlayerMap.size();
        LogUtil.debug("maxOnlinePlayerNum = " + maxOnlinePlayerNum + ", cur online num=" + onlineSize);
        return onlineSize >= maxOnlinePlayerNum - 10;
    }

    public boolean sendMsg(String playerIdx, int msgId, Builder<?> builder) {
        return sendByteMsg(playerIdx, msgId, builder.build().toByteArray());
    }

    public boolean sendByteMsg(String playerIdx, int msgId, byte[] data) {
        if (StringUtils.isBlank(playerIdx)) {
            LogUtil.warn("common.GlobalData.sendMsg, playerIdx is null, playerIdx=" + playerIdx);
            return false;
        }
        GameServerTcpChannel channel = onlinePlayerMap.get(playerIdx);
        if (channel != null && channel.channel.isActive()) {
            channel.send(msgId, data);
            LogUtil.debug("Send " + MsgIdEnum.forNumber(msgId) + " to playerIdx[" + playerIdx + "]");
            return true;
        }
        LogUtil.debug("send msg player is offline,playerId=" + playerIdx + ",msgId=" + MsgIdEnum.forNumber(msgId));
        return false;
    }

    public Set<String> getAllOnlinePlayerIdx() {
        return Collections.unmodifiableSet(onlinePlayerMap.keySet());
    }

    /**
     * 当前在线玩家数
     *
     * @return
     */
    public int getOnlinePlayerNum() {
        return onlinePlayerMap.size();
    }

    /**
     * 发送消息到系统聊天频道，所有在线玩家
     *
     * @param str
     */
    public void sendSystemChatMsg(String str) {
        if (str == null) {
            return;
        }
        SC_SystemChat.Builder builder = SC_SystemChat.newBuilder();
        builder.setInfo(str);
        sendMsgToAllOnlinePlayer(MsgIdEnum.SC_SystemChat, builder);
    }

    /**
     * 发送系统聊天到全服
     *
     * @param chatMsgEnum
     */
    public void sendSystemChatToAllOnlinePlayer(Chat.SystemChatEnum chatMsgEnum, SystemChatCreator chatCreator) {
        if (chatMsgEnum == null) {
            return;
        }
        SC_SystemChat.Builder msg = SC_SystemChat.newBuilder()
                .setMsgEnum(chatMsgEnum).addAllParams(chatCreator.createParamsList());

        for (String playerIdx : getAllOnlinePlayerIdx()) {
            sendMsg(playerIdx, MsgIdEnum.SC_SystemChat_VALUE, msg);
        }

    }

    public void sendSystemChatToPlayer(String playerIdx, String str) {
        SC_SystemChat.Builder builder = SC_SystemChat.newBuilder();
        builder.setInfo(str);
        sendMsg(playerIdx, MsgIdEnum.SC_SystemChat_VALUE, builder);
    }

    /**
     * 发送消息到所有的在线玩家
     *
     * @param msgIdEnum
     * @param builder
     */
    public void sendMsgToAllOnlinePlayer(MsgIdEnum msgIdEnum, Builder<?> builder) {
        sendMsgByPlayerList(getAllOnlinePlayerIdx(), msgIdEnum, builder);
    }

    /**
     * 发送消息到所有满足条件的
     *
     * @param msgIdEnum
     * @param builder
     */
    public void sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum msgIdEnum, Builder<?> builder, Predicate<String> condition) {
        if (msgIdEnum == null || msgIdEnum == MsgIdEnum.CS_Null || builder == null) {
            LogUtil.error("sendMsgToAllSatisfyOnlinePlayer error params");
            return;
        }

        Set<String> allOnlinePlayerIdx = getAllOnlinePlayerIdx().stream()
                .filter(condition)
                .collect(Collectors.toSet());
        sendMsgByPlayerList(allOnlinePlayerIdx, msgIdEnum, builder);
    }

    public void sendMsgByPlayerList(Collection<String> playerColl, MsgIdEnum msgIdEnum, Builder<?> builder) {
        if (CollectionUtils.isEmpty(playerColl) || msgIdEnum == null || msgIdEnum == MsgIdEnum.CS_Null || builder == null) {
            return;
        }
        sendMsgByPlayerList(playerColl, msgIdEnum.getNumber(), builder);
    }

    /**
     * 发送到指定玩家列表 使用异步线程执行
     *
     * @param playerColl
     * @param msgIdNum
     * @param builder
     */
    public void sendMsgByPlayerList(Collection<String> playerColl, int msgIdNum, Builder<?> builder) {
        if (CollectionUtils.isEmpty(playerColl) || msgIdNum == 0 || builder == null) {
            return;
        }

        GlobalThread.getInstance().execute(() -> {
            for (String playerIdx : playerColl) {
                sendMsg(playerIdx, msgIdNum, builder);
            }
        });
    }

    /**
     * 发送奖励展示消息到玩家
     *
     * @param playerIdx
     * @param rewards
     */
    public void sendDisRewardMsg(String playerIdx, List<Reward> rewards, RewardSourceEnum rewardSource) {
        if (playerIdx == null || rewards == null || rewards.isEmpty()) {
            return;
        }
        SC_DisplayRewards.Builder builder = SC_DisplayRewards.newBuilder();
        builder.addAllRewardList(rewards);
        builder.setRewardSource(rewardSource);
        this.sendMsg(playerIdx, MsgIdEnum.SC_DisplayRewards_VALUE, builder);
    }

    /**
     * 发送奖励展示消息到玩家
     *
     * @param playerIdx
     * @param reward
     */
    public void sendDisRewardMsg(String playerIdx, Reward reward, RewardSourceEnum rewardSource) {
        if (playerIdx == null || reward == null || rewardSource == null) {
            return;
        }
        List<Reward> rewards = new ArrayList<>();
        rewards.add(reward);
        sendDisRewardMsg(playerIdx, rewards, rewardSource);
    }

    /**
     * 发送奖励展示消息到玩家
     *
     * @param playerIdx
     * @param rewards
     * @param runeRewards
     * @param rse
     */
    public void sendDisMultiRewardsMsg(String playerIdx,  List<Reward> rewards,  List<RuneReward> runeRewards, RewardSourceEnum rse) {
        if (playerIdx == null || rse == null) {
            return;
        }
        Common.SC_DisplayMultiRewards.Builder showRewardsMsg = Common.SC_DisplayMultiRewards.newBuilder();
        if (rewards != null) {
            showRewardsMsg.addAllRewardList(rewards);
        }
        if (runeRewards != null) {
            showRewardsMsg.addAllRuneReward(runeRewards);
        }
        showRewardsMsg.setRewardSource(rse);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_DisplayMultiRewards_VALUE, showRewardsMsg);
    }

    /**
     * 关闭玩家的Channel
     *
     * @param playerIdx
     */
    public void closeChannel(String playerIdx) {
        GameServerTcpChannel onlinePlayerChannel = getOnlinePlayerChannel(playerIdx);
        if (onlinePlayerChannel != null) {
            onlinePlayerChannel.close();
        }
    }

    /**
     * 发送跑马灯到玩家
     *
     * @param playerIdx
     * @param marqueeTemplateId
     */
    public void sendMarqueeToPlayer(String playerIdx, int marqueeTemplateId, Object... params) {
        if (!checkPlayerOnline(playerIdx)) {
            return;
        }

        MarqueeTemplateObject templateCfg = MarqueeTemplate.getById(marqueeTemplateId);
        if (templateCfg == null) {
            LogUtil.error("GlobalData.sendMarqueeToPlayer, marquee template is not exist, id = " + marqueeTemplateId);
            return;
        }

        String content = ServerStringRes.getContentByLanguage(templateCfg.getContent(), PlayerUtil.queryPlayerLanguage(playerIdx), params);
        SC_Marquee.Builder builder = SC_Marquee.newBuilder()
                .setCycleCount(templateCfg.getRolltimes())
                .setInfo(content)
                .addAllScenesValue(ArrayUtil.intArrayToList(templateCfg.getScene()))
                .setPriorityValue(templateCfg.getPriority())
                .setDuration(templateCfg.getDuration());
        sendMsg(playerIdx, MsgIdEnum.SC_Marquee_VALUE, builder);
    }

    /**
     * 发送跑马灯到所有在线玩家
     *
     * @param marqueeTemplateId
     */
    public void sendMarqueeToAllOnlinePlayer(int marqueeTemplateId, Object... params) {
        sendMarqueeToAllSatisfyOnlinePlayer(marqueeTemplateId, null, params);
    }

    /**
     * 发送跑马灯到所有满足条件的在线玩家
     *
     * @param marqueeTemplateId
     * @param
     */
    public void sendMarqueeToAllSatisfyOnlinePlayer(int marqueeTemplateId, Predicate<String> condition, Object... params) {
        Set<String> allOnlinePlayerIdx = getAllOnlinePlayerIdx();
        if (GameUtil.collectionIsEmpty(allOnlinePlayerIdx)) {
            return;
        }

        MarqueeTemplateObject templateCfg = MarqueeTemplate.getById(marqueeTemplateId);
        if (templateCfg == null) {
            LogUtil.error("GlobalData.sendMarqueeToAllSatisfyOnlinePlayer, marquee template is not exist, id = " + marqueeTemplateId);
            return;
        }

        Map<Integer, String> content = ServerStringRes.getLanguageNumContentMap(templateCfg.getContent(), params);

        SC_Marquee.Builder builder = SC_Marquee.newBuilder()
                .setCycleCount(templateCfg.getRolltimes())
                .addAllScenesValue(ArrayUtil.intArrayToList(templateCfg.getScene()))
                .setPriorityValue(templateCfg.getPriority())
                .setDuration(templateCfg.getDuration());

        for (String playerIdx : allOnlinePlayerIdx) {
            if (condition != null && !condition.test(playerIdx)) {
                continue;
            }
            String str = content.get(PlayerUtil.queryPlayerLanguage(playerIdx).getNumber());
            if (StringUtils.isBlank(str)) {
                continue;
            }
            builder.clearInfo();
            builder.setInfo(str);
            sendMsg(playerIdx, MsgIdEnum.SC_Marquee_VALUE, builder);
        }
    }

    /**
     * 发送跑马灯到指定玩家
     *
     * @param playerIdx
     * @param strId
     * @param cycleCount 循环次数
     */
    public void sendMarqueeToPlayer(String playerIdx, int strId, int cycleCount) {
        if (playerIdx == null || strId <= 0 || cycleCount <= 0) {
            LogUtil.error("GlobalData.sendMarqueeToPlayer, params error");
            return;
        }
        String contentByLanguage = ServerStringRes.getContentByLanguage(strId, PlayerUtil.queryPlayerLanguage(playerIdx));
        sendMarqueeToPlayer(playerIdx, contentByLanguage, cycleCount);
    }

    /**
     * 发送跑马灯到指定玩家,默认最低优先级,默认主场景,默认过期时间二十秒
     *
     * @param playerIdx
     * @param str
     * @param cycleCount 循环次数
     */
    public void sendMarqueeToPlayer(String playerIdx, String str, int cycleCount) {
        if (playerIdx == null || str == null || "".equals(StringUtils.deleteWhitespace(str)) || cycleCount <= 0) {
            LogUtil.error("GlobalData.sendMarqueeToPlayer, params error");
            return;
        }

        SC_Marquee.Builder marquee = SC_Marquee.newBuilder();
        marquee.setInfo(str);
        marquee.setCycleCount(cycleCount);
        marquee.addScenes(EnumMarqueeScene.EMS_MainScene);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Marquee_VALUE, marquee);

        //发送到系统聊天
        sendSystemChatToPlayer(playerIdx, str);
    }

    public void sendDisPetRewardMsg(String playerId, List<PetMessage.PetReward> petRewards) {
        if (StringUtils.isEmpty(playerId) || CollectionUtils.isEmpty(petRewards)) {
            return;
        }
        PetMessage.SC_DisplayPetRewards.Builder msg = PetMessage.SC_DisplayPetRewards.newBuilder();
        msg.addAllRewardList(petRewards);
        GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_DisplayPetRewards_VALUE, msg);

    }

    public void sendSpecialMarqueeToAllOnlinePlayer(int marqueeTemplateId, List<Reward> rewardList, String... params) {
        Set<String> allOnlinePlayerIdx = getAllOnlinePlayerIdx();
        if (GameUtil.collectionIsEmpty(allOnlinePlayerIdx)) {
            return;
        }

        MarqueeTemplateObject templateCfg = MarqueeTemplate.getById(marqueeTemplateId);
        if (templateCfg == null) {
            LogUtil.error("GlobalData.sendSpecialMarqueeToAllOnlinePlayer, marquee template is not exist, id = " + marqueeTemplateId);
            return;
        }

        Map<Integer, String> content = ServerStringRes.getLanguageNumContentMap(templateCfg.getContent());

        Common.SC_SpecialMarquee.Builder builder = Common.SC_SpecialMarquee.newBuilder()
                .addAllRewards(rewardList)
                .addAllParams(Arrays.asList(params))
                .setCycleCount(templateCfg.getRolltimes())
                .addAllScenesValue(ArrayUtil.intArrayToList(templateCfg.getScene()))
                .setPriorityValue(templateCfg.getPriority())
                .setDuration(templateCfg.getDuration());

        for (String playerIdx : allOnlinePlayerIdx) {
            String str = content.get(PlayerUtil.queryPlayerLanguage(playerIdx).getNumber());
            if (StringUtils.isBlank(str)) {
                continue;
            }
            builder.clearInfo();
            builder.setInfo(str);
            sendMsg(playerIdx, MsgIdEnum.SC_SpecialMarquee_VALUE, builder);
        }
    }

    public void sendRetCodeMsg(String playerIdx, RetCodeEnum retCode) {
        if (StringUtils.isEmpty(playerIdx) || retCode == null) {
            return;
        }
        SC_RetCode.Builder retBuilder = SC_RetCode.newBuilder().setRetCode(GameUtil.buildRetCode(retCode));
        sendMsg(playerIdx, MsgIdEnum.SC_RetCode_VALUE, retBuilder);
    }

    /**
     * 战斗服转发消息到其他游戏服
     *
     * @param playerIpMap 玩家id 玩家服务器索引
     * @param msgId       消息号
     * @param msg         消息对象
     */
    public void forwardMsg(Map<String, String> playerIpMap, int msgId, GeneratedMessageV3 msg) {
        if (CollectionUtils.isEmpty(playerIpMap)) {
            return;
        }

        BaseNettyClient battleServer = BattleServerManager.getInstance().getAvailableBattleServer();
        if (battleServer == null) {
            LogUtil.error("forwardMsg fail,no available battle server ");
            return;
        }
        ServerTransfer.GS_BS_ForwardPlayerMsg.Builder forwardMsg = ServerTransfer.GS_BS_ForwardPlayerMsg.newBuilder()
                .setMsgId(msgId).setMsgData(msg.toByteString());

        int svrIndex;
        for (Entry<String, String> entry : playerIpMap.entrySet()) {
            svrIndex = StringHelper.stringToInt(entry.getValue(), 0);
            if (svrIndex > 0) {
                forwardMsg.putSendPlayer(entry.getKey(), svrIndex);
            } else {
                if (StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())) {
                    forwardMsg.putOldSendPlayer(entry.getKey(), entry.getValue());
                }
            }
        }
        battleServer.send(GS_BS_ForwardPlayerMsg_VALUE, forwardMsg);

    }

    public WorldMapData getWorldMapInfo() {
        WorldMapData worldMapData = new WorldMapData();
        worldMapData.setPetLv( PetManager.getInstance().findWorldMapPetLv());
        worldMapData.setMainlineNode(MainLineManager.getInstance().findWorldMapMainLineNode());
        return worldMapData;
    }
}
