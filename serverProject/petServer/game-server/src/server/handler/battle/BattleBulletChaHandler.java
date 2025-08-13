package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle;
import protocol.Battle.CS_BattleBulletCha;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.ServerTransfer;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BattleBulletCha_VALUE)
public class BattleBulletChaHandler extends AbstractBaseHandler<CS_BattleBulletCha> {
    @Override
    protected CS_BattleBulletCha parse(byte[] bytes) throws Exception {
        return CS_BattleBulletCha.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattleBulletCha req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("recv battle frame data but player is null");
            return;
        }
        Battle.SC_BattleBulletCha.Builder msg1 = Battle.SC_BattleBulletCha.newBuilder();
        msg1.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));

        int svrIndex = BattleServerManager.getInstance().getPlayerWatchInfo().get(playerIdx);
        if (svrIndex <= 0) {
            msg1.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Battle_NotInBattle));
            gsChn.send(MsgIdEnum.SC_BattleBulletCha_VALUE, msg1);
            return;
        }
        BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
        if (nettyClient == null) {
            msg1.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Battle_NotInBattle));
            gsChn.send(MsgIdEnum.SC_BattleBulletCha_VALUE, msg1);
            return;
        }
        ServerTransfer.GS_BS_BattleBulletCha.Builder msgSend = ServerTransfer.GS_BS_BattleBulletCha.newBuilder();
        msgSend.setBattleId(req.getBattleId());
        msgSend.setTalkFixedId(req.getTalkFixedId());
        msgSend.setTalkFree(req.getTalkFree());
        msgSend.setTalkType(req.getTalkType());
        msgSend.setPlayerIdx(playerIdx);
        msgSend.setName(player.getName());
        nettyClient.send(MsgIdEnum.GS_BS_BattleBulletCha_VALUE, msgSend);
        gsChn.send(MsgIdEnum.SC_BattleBulletCha_VALUE, msg1);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
