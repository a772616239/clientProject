package server.handler.player;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_UnEquipNewTitle;
import protocol.PlayerInfo.SC_UnEquipNewTitle;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/5
 */
@MsgId(msgId = MsgIdEnum.CS_UnEquipNewTitle_VALUE)
public class UnEquipNewTitleHandler extends AbstractBaseHandler<CS_UnEquipNewTitle> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_NewTitle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_UnEquipNewTitle.Builder builder = SC_UnEquipNewTitle.newBuilder()
                .setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_UnEquipNewTitle_VALUE, builder);
    }

    @Override
    protected CS_UnEquipNewTitle parse(byte[] bytes) throws Exception {
        return CS_UnEquipNewTitle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UnEquipNewTitle req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);

        SC_UnEquipNewTitle.Builder resultBuilder = SC_UnEquipNewTitle.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_UnEquipNewTitle_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            if (player.getCurEquipNewTitleId() != req.getNewTitleId()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewTitle_NotEquip));
            } else {
                player.getDb_data().getNewTitleBuilder().clearCurEquip();
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            }
        });

        gsChn.send(MsgIdEnum.SC_UnEquipNewTitle_VALUE, resultBuilder);
    }
}
