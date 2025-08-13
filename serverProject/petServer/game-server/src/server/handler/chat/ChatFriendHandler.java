package server.handler.chat;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.wordFilter.WordFilterManager;
import model.wordFilter.WordFilterUtil;
import protocol.Activity.SC_ActivityBossInit;
import protocol.Chat.CS_Chat_Friend;
import protocol.Chat.SC_Chat_Friend;
import protocol.Chat.SC_FriendChat;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_Chat_Friend_VALUE)
public class ChatFriendHandler extends AbstractBaseHandler<CS_Chat_Friend> {
    @Override
    protected CS_Chat_Friend parse(byte[] bytes) throws Exception {
        return CS_Chat_Friend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_Chat_Friend req, int i) {
        String info = req.getInfo();
        String targetIdx = req.getTargetIdx();

        SC_Chat_Friend.Builder resultBuilder = SC_Chat_Friend.newBuilder();
        if (StringHelper.isNull(info) || playerCache.getByIdx(targetIdx) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_Chat_Friend_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("ChatFriendHandler, playerIdx[" + playerIdx + "] entity is null");
            return;
        }


        SyncExecuteFunction.executeConsumer(player, entity -> {
            if (!player.isFriend(targetIdx)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Friend_TargetIsNotFriend));
                gsChn.send(MsgIdEnum.SC_Chat_Friend_VALUE, resultBuilder);
                return;
            }

            if (GlobalData.getInstance().checkPlayerOnline(targetIdx)) {
                SC_FriendChat.Builder chatBuilder = SC_FriendChat.newBuilder();
                if (WordFilterUtil.isSystemInfo(info)) {
                    chatBuilder.setInfo(info);
                } else {
                    chatBuilder.setInfo(WordFilterManager.getInstance().filterSensitiveWords(info));
                }
                chatBuilder.setSenderIdx(playerIdx);
                GlobalData.getInstance().sendMsg(targetIdx, MsgIdEnum.SC_FriendChat_VALUE, chatBuilder);
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_Chat_Friend_VALUE, resultBuilder);
        });

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WordChat;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_Chat_Friend_VALUE, SC_Chat_Friend.newBuilder().setRetCode(retCode));
    }
}
