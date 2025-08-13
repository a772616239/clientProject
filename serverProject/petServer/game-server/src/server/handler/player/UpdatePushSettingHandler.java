package server.handler.player;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_UpdatePushSetting;
import protocol.PlayerInfo.SC_AlterName;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdatePushSetting_VALUE)
public class UpdatePushSettingHandler extends AbstractBaseHandler<CS_UpdatePushSetting> {

    @Override
    protected CS_UpdatePushSetting parse(byte[] bytes) throws Exception {
        return CS_UpdatePushSetting.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdatePushSetting req, int codeNum) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_AlterName.Builder resultBuilder = SC_AlterName.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AlterName_NotFoundPlayer));
            gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> player.getDb_data().setPushOpen(req.getOpenPush()));
        player.sendUpdatePushSetting();
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
