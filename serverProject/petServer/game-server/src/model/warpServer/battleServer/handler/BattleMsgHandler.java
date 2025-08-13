package model.warpServer.battleServer.handler;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Battle;
import protocol.Battle.SC_BattleStart;
import protocol.BattleMono.SC_FrameData;
import protocol.Chat.SC_BattleChatData;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_TransferBattleMsg;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_GS_TransferBattleMsg_VALUE)
public class BattleMsgHandler extends AbstractHandler<BS_GS_TransferBattleMsg> {
    @Override
    protected BS_GS_TransferBattleMsg parse(byte[] bytes) throws Exception {
        return BS_GS_TransferBattleMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_TransferBattleMsg req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        try {
            switch (req.getMsgId()) {
                case MsgIdEnum.SC_BattleStart_VALUE:
                    SC_BattleStart battleStart = SC_BattleStart.parseFrom(req.getMsgData());
                    GlobalData.getInstance().sendMsg(player.getIdx(),
                            MsgIdEnum.SC_BattleStart_VALUE, battleStart.toBuilder());
                    break;
                case MsgIdEnum.SC_FrameData_VALUE:
                    SC_FrameData frameData = SC_FrameData.parseFrom(req.getMsgData());
                    GlobalData.getInstance().sendMsg(
                            player.getIdx(), MsgIdEnum.SC_FrameData_VALUE, frameData.toBuilder());
                    break;
                case MsgIdEnum.SC_BattleChatData_VALUE:
                    SC_BattleChatData battleChat = SC_BattleChatData.parseFrom(req.getMsgData());
                    GlobalData.getInstance().sendMsg(player.getIdx(),
                            MsgIdEnum.SC_BattleChatData_VALUE, battleChat.toBuilder());
                    break;
                case MsgIdEnum.SC_RetCode_VALUE:
                    Common.SC_RetCode retCode = Common.SC_RetCode.parseFrom(req.getMsgData());
                    GlobalData.getInstance().sendMsg(player.getIdx(),
                            MsgIdEnum.SC_RetCode_VALUE, retCode.toBuilder());
                    break;
                case MsgIdEnum.SC_BattleBulletChaPush_VALUE:
                    Battle.SC_BattleBulletChaPush battleWatch = Battle.SC_BattleBulletChaPush.parseFrom(req.getMsgData());
                    GlobalData.getInstance().sendMsg(player.getIdx(),
                            MsgIdEnum.SC_BattleBulletChaPush_VALUE, battleWatch.toBuilder());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
