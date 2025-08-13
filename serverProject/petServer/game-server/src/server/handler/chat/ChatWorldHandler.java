package server.handler.chat;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import model.wordFilter.WordFilterManager;
import model.wordFilter.WordFilterUtil;
import protocol.Chat.CS_Chat_World;
import protocol.Chat.PlayerChatBaseInfo;
import protocol.Chat.SC_Chat_World;
import protocol.Chat.SC_WorldChat;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_Chat_World_VALUE)
public class ChatWorldHandler extends AbstractBaseHandler<CS_Chat_World> {
    @Override
    protected CS_Chat_World parse(byte[] bytes) throws Exception {
        return CS_Chat_World.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_Chat_World req, int i) {
        SC_Chat_World.Builder resultBuilder = SC_Chat_World.newBuilder();
        if (PlayerUtil.queryPlayerLv(String.valueOf(gsChn.getPlayerId1())) < FunctionOpenLvConfig.getOpenLv(EnumFunction.WordChat)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_Chat_World_VALUE, resultBuilder);
            return;
        }

        String info = req.getInfo();
        if (info == null) {
            LogUtil.info("ChatWorldHandler, send Msg is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_Chat_World_VALUE, resultBuilder);
            return;
        }
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        //发送信息到所有的在线玩家
        SC_WorldChat.Builder sendAllOnlinePlayer = SC_WorldChat.newBuilder();
        if (WordFilterUtil.isSystemInfo(info)) {
            sendAllOnlinePlayer.setInfo(info);
        } else {
            sendAllOnlinePlayer.setInfo(WordFilterManager.getInstance().filterSensitiveWords(info));
        }
        PlayerChatBaseInfo playerChatbaseInfo = getPlayerChatBaseInfo(playerIdx);
        if (playerChatbaseInfo != null) {
            sendAllOnlinePlayer.setSender(playerChatbaseInfo);
        }
        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_WorldChat, sendAllOnlinePlayer);

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_Chat_World_VALUE, resultBuilder);
    }

    private PlayerChatBaseInfo getPlayerChatBaseInfo(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }

        PlayerChatBaseInfo.Builder baseBuilder = PlayerChatBaseInfo.newBuilder();
        baseBuilder.setPlayerIdx(player.getIdx());
        baseBuilder.setName(player.getName());
        baseBuilder.setVIPLv(player.getVip());
        baseBuilder.setLv(player.getLevel());
        baseBuilder.setAvatarId(player.getAvatar());
        baseBuilder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (baseBuilder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            baseBuilder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(playerIdx));
        }
        return baseBuilder.build();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WordChat;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_Chat_World_VALUE, SC_Chat_World.newBuilder().setRetCode(retCode));
    }
}
