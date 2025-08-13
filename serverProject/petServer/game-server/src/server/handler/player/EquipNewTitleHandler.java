package server.handler.player;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_EquipNewTitle;
import protocol.PlayerInfo.NewTitle;
import protocol.PlayerInfo.NewTitleInfo;
import protocol.PlayerInfo.NewTitleInfo.Builder;
import protocol.PlayerInfo.SC_EquipNewTitle;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/4
 */
@MsgId(msgId = MsgIdEnum.CS_EquipNewTitle_VALUE)
public class EquipNewTitleHandler extends AbstractBaseHandler<CS_EquipNewTitle> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_NewTitle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_EquipNewTitle.Builder builder = SC_EquipNewTitle.newBuilder()
                .setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_EquipNewTitle_VALUE, builder);
    }

    @Override
    protected CS_EquipNewTitle parse(byte[] bytes) throws Exception {
        return CS_EquipNewTitle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EquipNewTitle req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_EquipNewTitle.Builder resultBuilder = SC_EquipNewTitle.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_EquipNewTitle_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            NewTitle.Builder newTitleBuilder = player.getDb_data().getNewTitleBuilder();
            NewTitleInfo.Builder newTitleInfo = null;
            for (Builder titleInfo : newTitleBuilder.getInfoBuilderList()) {
                if (titleInfo.getCfgId() == req.getNewTitleId()) {
                    newTitleInfo = titleInfo;
                    break;
                }
            }
            if (newTitleInfo == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewTitle_Inactivated));
                gsChn.send(MsgIdEnum.SC_EquipNewTitle_VALUE, resultBuilder);
                return;
            }

            if (newTitleInfo.getExpireStamp() != -1
                    && GlobalTick.getInstance().getCurrentTime() >= newTitleInfo.getExpireStamp()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewTitle_Expired));
                gsChn.send(MsgIdEnum.SC_EquipNewTitle_VALUE, resultBuilder);
                return;
            }

            newTitleBuilder.setCurEquip(req.getNewTitleId());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_EquipNewTitle_VALUE, resultBuilder);
        });
    }
}
