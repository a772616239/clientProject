package model.matchArena;

import cfg.MatchArenaLT;
import cfg.MatchArenaLTObject;
import common.GameConst;
import common.load.ServerConfig;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import protocol.*;
import protocol.MatchArenaDB.RedisMatchArenaLTOneInfo;
import protocol.MatchArenaDB.RedisMatchArenaLTPlayer;
import static util.JedisUtil.jedis;
import util.LogUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 中心服擂台赛逻辑类
 */
public class MatchArenaLTManager {

    private static MatchArenaLTManager instance;

    public static MatchArenaLTManager getInstance() {
        if (instance == null) {
            synchronized (MatchArenaLTManager.class) {
                if (instance == null) {
                    instance = new MatchArenaLTManager();
                }
            }
        }
        return instance;
    }

    private MatchArenaLTManager() {
    }

    public void init() {
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.ConfgId);
        if (null != cfgData) {
            settleTime = cfgData.getSettlementtime() * 1000L;
        }
    }

    /**
     * 活动开启状态
     */
    private boolean isOpen = false;
    private long openTime = 0;
    private long settleTime = 60000L;

    private Map<Integer, Long> battleLongTime = new ConcurrentHashMap<>();
    /**
     * 缓存擂台房间ID<擂台ID,战斗房间ID>
     */
    private Map<Integer, Long> battleingRoom = new ConcurrentHashMap<Integer, Long>();

    private Map<Integer, Long> lastSendDefTime = new ConcurrentHashMap<Integer, Long>();

    public void quitLeiTai(GameServerTcpChannel gsChn, String playerIdx, int leitaiId, Battle.BattlePlayerInfo robotInfo, int showID) {
        ServerTransfer.BS_GS_MatchArenaLTQuit.Builder msg = ServerTransfer.BS_GS_MatchArenaLTQuit.newBuilder();
        msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setPlayerId(playerIdx);
        boolean isRef = false;

        try {
            // 获取公共数据判断是否本服处理该擂台
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }
            // 判断该擂台是否工作中
            String ltaddr = leitaiIp.getOrDefault(""+leitaiId, "");
            String currIp = getIpPort();
            if (!Objects.equals(currIp, ltaddr)) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }

            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
            if (null == oneLeiTaiDB) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || !oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(playerIdx)) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_MYLT);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }
            // 判断通过更新数据，替换AI
            RedisMatchArenaLTPlayer.Builder newAttData = RedisMatchArenaLTPlayer.newBuilder();
            newAttData.setName(robotInfo.getPlayerInfo().getPlayerName());
            newAttData.setPlayerId(robotInfo.getPlayerInfo().getPlayerId());
            newAttData.setSvrIndex(0);
            newAttData.setFromSvrIp("");
            newAttData.setTeamInfo(robotInfo);
            newAttData.setScore(0);
            newAttData.setShowPetId(showID);

            long defTime = System.currentTimeMillis() - oneLeiTaiProtoDB.getDefTime();
            if (defTime > 2100000000L) {
                defTime = 0;
            }
            int defTimeInt = (int) (defTime / 1000);
            RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
            // 直接替换数据
            newData.setDefPlayer(newAttData);
            newData.clearAttPlayer();
            newData.setDefWinNum(0);
            newData.setBattleId(0);
            newData.setDefTime(System.currentTimeMillis());
            newData.setLastBattleTime(System.currentTimeMillis());
            newData.setState(MatchArena.MatchArenaLTState.WAIT_VALUE);
            jedis.hset(GameConst.RedisKey.MatchArenaLTSyncDataId, ""+leitaiId, newData.getDefPlayer().getPlayerId());
            jedis.set(createRedisKeyLT(leitaiId).getBytes(), newData.build().toByteArray());
            msg.setDefTime(defTimeInt);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
            isRef = true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
        }
        if (isRef) {
            ServerTransfer.BS_GS_MatchArenaLTRef.Builder msg10 = ServerTransfer.BS_GS_MatchArenaLTRef.newBuilder();
            msg10.setLeitaiId(leitaiId);
            WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE, msg10);
        }
    }

    public void guess(GameServerTcpChannel gsChn, String playerIdx, int leitaiId, int isWin, int fromSvrIndex) {
        ServerTransfer.BS_GS_MatchArenaLTGuess.Builder msg = ServerTransfer.BS_GS_MatchArenaLTGuess.newBuilder();
        msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setPlayerId(playerIdx);
        try {
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE, msg);
                return;
            }
            // 判断该擂台是否工作中
            String ltaddr = leitaiIp.getOrDefault(""+leitaiId, "");
            String currIp = getIpPort();
            if (!Objects.equals(currIp, ltaddr)) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE, msg);
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
            if (null == oneLeiTaiDB) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE, msg);
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (oneLeiTaiProtoDB.containsGuessAttSvrData(playerIdx) || oneLeiTaiProtoDB.containsGuessDefSvrData(playerIdx)) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_RPEI_GUESS);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE, msg);
                return;
            }
            RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
            if (isWin == 1) {
                newData.putGuessDefSvrData(playerIdx, StringHelper.IntTostring(fromSvrIndex, "0"));
            } else {
                newData.putGuessAttSvrData(playerIdx, StringHelper.IntTostring(fromSvrIndex, "0"));
            }
            // 判断通过更新数据
            jedis.set(createRedisKeyLT(leitaiId).getBytes(), newData.build().toByteArray());
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE, msg);
        }
    }

    public void attLeiTai(GameServerTcpChannel gsChn, ServerTransfer.GS_BS_MatchArenaLTAtt parm) {
        ServerTransfer.BS_GS_MatchArenaLTAtt.Builder msg = ServerTransfer.BS_GS_MatchArenaLTAtt.newBuilder();
        msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setPlayerId(parm.getPlayerId());
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.ConfgId);
        if (null == cfgData) {
            msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
            return;
        }
        if (battleingRoom.containsKey(parm.getLeitaiId())) {
            msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
            return;
        }
        boolean isRef = false;
        try {
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            // 判断该擂台是否工作中
            String ltaddr = leitaiIp.getOrDefault(""+parm.getLeitaiId(), "");
            String currIp = getIpPort();
            if (!Objects.equals(currIp, ltaddr)) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(parm.getLeitaiId()).getBytes());
            if (null == oneLeiTaiDB) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(parm.getPlayerId())) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOATTMY);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            // 判断通过可以挑战
            RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
            newData.setState(MatchArena.MatchArenaLTState.FIGHT_VALUE);
            RedisMatchArenaLTPlayer.Builder msg1 = RedisMatchArenaLTPlayer.newBuilder();
            msg1.setName(parm.getName());
            msg1.setPlayerId(parm.getPlayerId());
            msg1.setSvrIndex(parm.getSvrIndex());
            msg1.setTeamInfo(parm.getTeamInfo());
            msg1.setScore(parm.getScore());
            msg1.setShowPetId(parm.getShowPetId());
            newData.setAttPlayer(msg1);
            RedisMatchArenaLTPlayer.Builder playerInfoDef = newData.getDefPlayer().toBuilder();
            Battle.BattlePlayerInfo.Builder teamInfoDef = playerInfoDef.getTeamInfo().toBuilder();
            teamInfoDef.setPlayerExtData(teamInfoDef.getPlayerExtData().toBuilder().addKeys(Battle.PlayerExtDataEnum.PEDE_Arena_LT_WinNum).addValues(newData.getDefWinNum()));
            playerInfoDef.setTeamInfo(teamInfoDef);
            int idx = newData.getDefWinNum()-1;
            if (cfgData.getWinbuff().length <= idx) {
                idx = cfgData.getWinbuff().length - 1;
            }
            int attAddBuffId = 0;
            if (idx >= 0) {
                attAddBuffId = cfgData.getWinbuff()[idx];
            }
            ServerTransfer.PvpBattlePlayerInfo firstPvpInfo = buildPvpPlayerInfo(1, newData.getAttPlayer());
            ServerTransfer.PvpBattlePlayerInfo secondPvpInfo = buildPvpPlayerInfo(2, playerInfoDef.build());
            if (firstPvpInfo == null || secondPvpInfo == null) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArena_PlayerInfoNotFount);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            ServerTransfer.ApplyPvpBattleData.Builder applyPvpBuilder = ServerTransfer.ApplyPvpBattleData.newBuilder();
            applyPvpBuilder.setFightMakeId(cfgData.getFightmakeid());
            applyPvpBuilder.setSubBattleType(Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
            applyPvpBuilder.addPlayerInfo(firstPvpInfo);
            applyPvpBuilder.addPlayerInfo(secondPvpInfo);

            // 创建战斗房间
            ServerTransfer.ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(),
                    ServerTransfer.ServerTypeEnum.STE_GameServer, newData.getAttPlayer().getSvrIndex(), attAddBuffId);
            // 房间创建失败
            if (!replyBuilder.getResult()) {
                msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            newData.setLastBattleTime(System.currentTimeMillis());
            newData.setBattleId(replyBuilder.getBattleId());
            battleingRoom.put(parm.getLeitaiId(), replyBuilder.getBattleId());
            // 更新数据
            jedis.set(createRedisKeyLT(parm.getLeitaiId()).getBytes(), newData.build().toByteArray());
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);

            // 创建PVP战斗创建成功消息
            ServerTransfer.BS_GS_ReplyPvpBattle.Builder builder = ServerTransfer.BS_GS_ReplyPvpBattle.newBuilder();
            builder.setReplyPvpBattleData(replyBuilder);

            // 通知战斗双方PVP战斗开始
            WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, newData.getAttPlayer().getSvrIndex(),
                    MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);

            int defPlayerSvrIndex = newData.getDefPlayer().getSvrIndex();
            if (defPlayerSvrIndex <= 0) {
                WarpServerManager.getInstance().getSeverIndexByIp(newData.getDefPlayer().getFromSvrIp());
            }
            if (newData.getAttPlayer().getSvrIndex() != defPlayerSvrIndex) {
                WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, defPlayerSvrIndex,
                        MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
            }
            battleLongTime.put(parm.getLeitaiId(), System.currentTimeMillis() + 450000L);
            isRef = true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            gsChn.send(MessageId.MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE, msg);
        }
        if (isRef) {
            ServerTransfer.BS_GS_MatchArenaLTRef.Builder msg10 = ServerTransfer.BS_GS_MatchArenaLTRef.newBuilder();
            msg10.setLeitaiId(parm.getLeitaiId());
            WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE, msg10);
        }
    }

    public void attLeiTaiAI(int oper, ServerTransfer.GS_BS_MatchArenaLTAtt parm) {
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.ConfgId);
        if (null == cfgData) {
            return;
        }
        if (battleingRoom.containsKey(parm.getLeitaiId())) {
            return;
        }
        boolean isRef = false;
        try {
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                return;
            }
            // 判断该擂台是否工作中
            String ltaddr = leitaiIp.getOrDefault(""+parm.getLeitaiId(), "");
            String currIp = getIpPort();
            if (!Objects.equals(currIp, ltaddr)) {
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(parm.getLeitaiId()).getBytes());
            if (null == oneLeiTaiDB) {
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || !oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(parm.getPlayerId())) {
                return;
            }
            // 判断通过可以挑战，根据类型判断时战斗还是直接替换
            RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
            RedisMatchArenaLTPlayer.Builder newAttData = RedisMatchArenaLTPlayer.newBuilder();
            newAttData.setName(parm.getName());
            newAttData.setPlayerId(parm.getTeamInfo().getPlayerInfo().getPlayerId());
            newAttData.setSvrIndex(parm.getSvrIndex());
            newAttData.setTeamInfo(parm.getTeamInfo());
            newAttData.setScore(parm.getScore());
            newAttData.setShowPetId(parm.getShowPetId());
            if (oper == 1) {
                newData.setState(MatchArena.MatchArenaLTState.FIGHT_VALUE);
                newData.setAttPlayer(newAttData);
                RedisMatchArenaLTPlayer.Builder playerInfoDef = newData.getDefPlayer().toBuilder();
                Battle.BattlePlayerInfo.Builder teamInfoDef = playerInfoDef.getTeamInfo().toBuilder();
                teamInfoDef.setPlayerExtData(teamInfoDef.getPlayerExtData().toBuilder().addKeys(Battle.PlayerExtDataEnum.PEDE_Arena_LT_WinNum).addValues(newData.getDefWinNum()));
                playerInfoDef.setTeamInfo(teamInfoDef);
                int idx = newData.getDefWinNum()-1;
                if (cfgData.getWinbuff().length <= idx) {
                    idx = cfgData.getWinbuff().length - 1;
                }
                int attAddBuffId = 0;
                if (idx >= 0) {
                    attAddBuffId = cfgData.getWinbuff()[idx];
                }
                ServerTransfer.PvpBattlePlayerInfo firstPvpInfo = buildPvpPlayerInfo(1, newData.getAttPlayer());
                ServerTransfer.PvpBattlePlayerInfo secondPvpInfo = buildPvpPlayerInfo(2, playerInfoDef.build());
                if (firstPvpInfo == null || secondPvpInfo == null) {
                    return;
                }
                ServerTransfer.ApplyPvpBattleData.Builder applyPvpBuilder = ServerTransfer.ApplyPvpBattleData.newBuilder();
                applyPvpBuilder.setFightMakeId(cfgData.getFightmakeid());
                applyPvpBuilder.setSubBattleType(Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
                applyPvpBuilder.addPlayerInfo(firstPvpInfo);
                applyPvpBuilder.addPlayerInfo(secondPvpInfo);

                // 创建战斗房间
                ServerTransfer.ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(),
                        ServerTransfer.ServerTypeEnum.STE_GameServer, newData.getAttPlayer().getSvrIndex(), attAddBuffId);
                // 房间创建失败
                if (!replyBuilder.getResult()) {
                    return;
                }
                newData.setLastBattleTime(System.currentTimeMillis());
                newData.setBattleId(replyBuilder.getBattleId());
                battleingRoom.put(parm.getLeitaiId(), replyBuilder.getBattleId());

                // 创建PVP战斗创建成功消息
                ServerTransfer.BS_GS_ReplyPvpBattle.Builder builder = ServerTransfer.BS_GS_ReplyPvpBattle.newBuilder();
                builder.setReplyPvpBattleData(replyBuilder);

                // 通知战斗双方PVP战斗开始
                int defSvrIndex = newData.getDefPlayer().getSvrIndex();
                if (defSvrIndex <= 0) { // 兼容代码
                    defSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(newData.getDefPlayer().getFromSvrIp());
                }
                WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, defSvrIndex,
                        MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
                battleLongTime.put(parm.getLeitaiId(), System.currentTimeMillis() + 450000L);
            } else {
                // 直接替换数据
                newData.setDefPlayer(newAttData);
                newData.clearAttPlayer();
                newData.setDefWinNum(0);
                newData.setDefTime(System.currentTimeMillis());
                newData.setLastBattleTime(System.currentTimeMillis());
                jedis.hset(GameConst.RedisKey.MatchArenaLTSyncDataId, ""+parm.getLeitaiId(), newData.getDefPlayer().getPlayerId());
                newData.setState(MatchArena.MatchArenaLTState.WAIT_VALUE);
            }
            // 更新数据
            jedis.set(createRedisKeyLT(parm.getLeitaiId()).getBytes(), newData.build().toByteArray());
            isRef = true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        if (isRef) {
            ServerTransfer.BS_GS_MatchArenaLTRef.Builder msg10 = ServerTransfer.BS_GS_MatchArenaLTRef.newBuilder();
            msg10.setLeitaiId(parm.getLeitaiId());
            WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE, msg10);
        }
    }

    /**
     * @param camp
     * @param playerInfo
     * @return
     * 构建跨服PVP战斗数据
     */
    public ServerTransfer.PvpBattlePlayerInfo buildPvpPlayerInfo(int camp, RedisMatchArenaLTPlayer playerInfo) {
        if (playerInfo == null || null == playerInfo.getTeamInfo()) {
            LogUtil.error("model.matchArena.MatchArenaPlayer.buildPvpPlayerInfo, player info is null");
            return null;
        }
        ServerTransfer.PvpBattlePlayerInfo.Builder resultBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();
        resultBuilder.setPlayerInfo(playerInfo.getTeamInfo().getPlayerInfo());
        int svrIndex = playerInfo.getSvrIndex();
        if (svrIndex <= 0) { // 兼容代码
            svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(playerInfo.getFromSvrIp());
        }
        resultBuilder.setFromSvrIndex(playerInfo.getSvrIndex());
        resultBuilder.setCamp(camp);
        resultBuilder.setIsAuto(playerInfo.getTeamInfo().getIsAuto());
        resultBuilder.setPlayerExtData(playerInfo.getTeamInfo().getPlayerExtData());
        resultBuilder.addAllPetList(playerInfo.getTeamInfo().getPetListList());
        resultBuilder.addAllPlayerSkillIdList(playerInfo.getTeamInfo().getPlayerSkillIdListList());
        if (GameConst.ROOBOTID.equals(playerInfo.getTeamInfo().getPlayerInfo().getPlayerId())) {
        	resultBuilder.setIsAI(1);
        }
        return resultBuilder.build();
    }

    /**
     * @param battleId
     * @param winCamp
     * 战斗结束
     */
    public void settleMatchLeitai(long battleId, int winCamp) {
        // 根据缓存查找擂台
        int leitaiId = 0;
        for (Map.Entry<Integer, Long> ent : battleingRoom.entrySet()) {
            if (ent.getValue() == battleId) {
                leitaiId = ent.getKey();
                break;
            }
        }
        if (leitaiId <= 0) {
            return;
        }
        boolean isRef = false;
        try {
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                return;
            }
            // 判断该擂台是否工作中
            String ltaddr = leitaiIp.getOrDefault(""+leitaiId, "");
            String currIp = getIpPort();
            if (!Objects.equals(currIp, ltaddr)) {
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
            if (null == oneLeiTaiDB) {
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            // 判断通过可以挑战
            RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
            newData.setState(MatchArena.MatchArenaLTState.SETTLE_VALUE);
            // 处理竞猜
            ServerTransfer.BS_GS_MatchArenaLTGuessResult.Builder msg2 = ServerTransfer.BS_GS_MatchArenaLTGuessResult.newBuilder();
            msg2.setLeitaiId(leitaiId);
            if (winCamp == 1) {
                // 攻击方胜利，守擂者数据替换
                newData.setDefPlayer(newData.getAttPlayerBuilder());
                newData.setDefWinNum(0);
                newData.setDefTime(System.currentTimeMillis());
                // 统计竞猜信息
                msg2.addAllWinIds(newData.getGuessAttSvrDataMap().keySet());
                msg2.addAllFailsIds(newData.getGuessDefSvrDataMap().keySet());
            } else {
                // 防守方胜利
                newData.setDefWinNum(newData.getDefWinNum()+1);
                // 统计竞猜信息
                msg2.addAllWinIds(newData.getGuessDefSvrDataMap().keySet());
                msg2.addAllFailsIds(newData.getGuessAttSvrDataMap().keySet());
            }
            // 战斗结束处理不论输赢数据
            newData.clearAttPlayer();
            jedis.hset(GameConst.RedisKey.MatchArenaLTSyncDataId, ""+leitaiId, newData.getDefPlayer().getPlayerId());
            newData.setLastBattleTime(System.currentTimeMillis());
            // 通知玩家战斗结果
            ServerTransfer.BS_GS_MatchArenaLTWinResult.Builder settleMsg = ServerTransfer.BS_GS_MatchArenaLTWinResult.newBuilder();
            settleMsg.setPlayerIdDef(oneLeiTaiProtoDB.getDefPlayer().getPlayerId());
            settleMsg.setPlayerIdAtt(oneLeiTaiProtoDB.getAttPlayer().getPlayerId());
            settleMsg.setScoreDef(oneLeiTaiProtoDB.getDefPlayer().getScore());
            settleMsg.setScoreAtt(oneLeiTaiProtoDB.getAttPlayer().getScore());
            settleMsg.setIsWin(winCamp == 1 ? 1 : 2);
            settleMsg.setWinNumDef(oneLeiTaiProtoDB.getDefWinNum());
            long defTime = System.currentTimeMillis() - oneLeiTaiProtoDB.getDefTime();
            if (defTime > 2100000000L) {
                defTime = 0;
            }
            int defTimeInt = (int) (defTime / 1000);
            settleMsg.setDefTime(defTimeInt);
            WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, oneLeiTaiProtoDB.getDefPlayer().getSvrIndex(), MessageId.MsgIdEnum.BS_GS_MatchArenaLTWinResult_VALUE, settleMsg);
            WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, oneLeiTaiProtoDB.getAttPlayer().getSvrIndex(), MessageId.MsgIdEnum.BS_GS_MatchArenaLTWinResult_VALUE, settleMsg);
            // 处理竞猜
            Set<Integer> fromSvrSet = new HashSet<>();
            int svrIndex;
            // 兼容代码
            for (String addr : newData.getGuessDefSvrDataMap().values()) {
                svrIndex = StringHelper.stringToInt(addr, 0);
                if (svrIndex <= 0) {
                    svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
                }
                if (svrIndex > 0) {
                    fromSvrSet.add(svrIndex);
                }
            }
            for (String addr : newData.getGuessAttSvrDataMap().values()) {
                svrIndex = StringHelper.stringToInt(addr, 0);
                if (svrIndex <= 0) {
                    svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
                }
                if (svrIndex > 0) {
                    fromSvrSet.add(svrIndex);
                }
            }
            for (Integer fromSvrIndex : fromSvrSet) {
                WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, fromSvrIndex, MessageId.MsgIdEnum.BS_GS_MatchArenaLTGuessResult_VALUE, msg2);
            }
            newData.setSettleTime(System.currentTimeMillis() + settleTime);
            newData.setLastBattleTime(System.currentTimeMillis());
            newData.clearGuessAttSvrData();
            newData.clearGuessDefSvrData();
            battleingRoom.remove(leitaiId);
            // 更新数据
            jedis.set(createRedisKeyLT(leitaiId).getBytes(), newData.build().toByteArray());
            isRef = true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        if (isRef) {
            ServerTransfer.BS_GS_MatchArenaLTRef.Builder msg10 = ServerTransfer.BS_GS_MatchArenaLTRef.newBuilder();
            msg10.setLeitaiId(leitaiId);
            WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE, msg10);
        }
    }

    public void onTick(long currTime, String leitaiIdStr) {
        int leitaiId = Integer.valueOf(leitaiIdStr);
        boolean isRef = false;
        try {
            String openTime = jedis.get(GameConst.RedisKey.MatchArenaLTTime);
            if (StringHelper.isNull(openTime)) {
                return;
            }
            // 判断存储数据是否是同一条数据
            if (Long.valueOf(openTime) <= 0) {
                // 活动关闭
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
            if (null == oneLeiTaiDB) {
                return;
            }
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (oneLeiTaiProtoDB.getState() == MatchArena.MatchArenaLTState.WAIT_VALUE) {
                // 等待玩家状态，需要判断是否长时间守擂
                if (!Objects.equals(GameConst.ROOBOTID, oneLeiTaiProtoDB.getDefPlayer().getPlayerId())) {
                    if (currTime > lastSendDefTime.getOrDefault(leitaiId, 0L)) {
                        lastSendDefTime.put(leitaiId, currTime + 20000L);
                        ServerTransfer.BS_GS_MatchArenaLTDefLong.Builder msg = ServerTransfer.BS_GS_MatchArenaLTDefLong.newBuilder();
                        msg.setLeitaiId(leitaiId);
                        msg.setLastTime(currTime - oneLeiTaiProtoDB.getLastBattleTime());
                        msg.setDefWin(oneLeiTaiProtoDB.getDefWinNum());
                        msg.setDefplayerId(oneLeiTaiProtoDB.getDefPlayer().getPlayerId());
                        int svrIndex = oneLeiTaiProtoDB.getDefPlayer().getSvrIndex();
                        if (svrIndex <= 0) {
                            svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(oneLeiTaiProtoDB.getDefPlayer().getFromSvrIp());
                        }
                        WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, svrIndex,
                                MessageId.MsgIdEnum.BS_GS_MatchArenaLTDefLong_VALUE, msg);
                    }
                }
            } else if (oneLeiTaiProtoDB.getState() == MatchArena.MatchArenaLTState.SETTLE_VALUE) {
                // 结算战斗，判断时间是否达到
                if (oneLeiTaiProtoDB.getSettleTime() > 0 && oneLeiTaiProtoDB.getSettleTime() < currTime) {
                    // 超过结算时间，更新数据
                    RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
                    newData.setState(MatchArena.MatchArenaLTState.WAIT_VALUE);
                    newData.setLastBattleTime(System.currentTimeMillis());
                    newData.setSettleTime(0);
                    // 判断通过更新数据
                    jedis.set(createRedisKeyLT(leitaiId).getBytes(), newData.build().toByteArray());
                    isRef = true;
                }
            } else if (oneLeiTaiProtoDB.getState() == MatchArena.MatchArenaLTState.FIGHT_VALUE) {
                if (battleLongTime.getOrDefault(oneLeiTaiProtoDB.getLeitaiId(), Long.MAX_VALUE) <= currTime) {
                    // 容错处理，战斗超时，450秒还有收到战斗结果消息
                    RedisMatchArenaLTOneInfo.Builder newData = oneLeiTaiProtoDB.toBuilder();
                    newData.setState(MatchArena.MatchArenaLTState.WAIT_VALUE)
                            .clearAttPlayer()
                            .clearGuessAttSvrData().clearGuessDefSvrData()
                            .setSettleTime(0)
                            .setBattleId(0)
                            .setLastBattleTime(System.currentTimeMillis());
                    jedis.set(createRedisKeyLT(leitaiId).getBytes(), newData.build().toByteArray());
                    isRef = true;
                }
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        if (isRef) {
            ServerTransfer.BS_GS_MatchArenaLTRef.Builder msg10 = ServerTransfer.BS_GS_MatchArenaLTRef.newBuilder();
            msg10.setLeitaiId(leitaiId);
            WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE, msg10);
        }
    }

    /**
     * @param leitaiId
     * @return
     * 创建单个擂台数据key
     */
    public String createRedisKeyLT(int leitaiId) {
        return GameConst.RedisKey.MatchArenaLTSyncData + leitaiId;
    }

    public String getIpPort() {
        String ipPort = ServerConfig.getInstance().getIp() + ":" + ServerConfig.getInstance().getPort();
        return ipPort;
    }

}
