package server.handler.newbee;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.Newbee.CS_SetNewbeeStep;
import protocol.Newbee.SC_SetNewbeeStep;
import protocol.PlayerDB.DB_NewBee;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/9/17
 */
@MsgId(msgId = MsgIdEnum.CS_SetNewbeeStep_VALUE)
public class SetNewbeeHandler extends AbstractBaseHandler<CS_SetNewbeeStep> {
    @Override
    protected CS_SetNewbeeStep parse(byte[] bytes) throws Exception {
        return CS_SetNewbeeStep.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_SetNewbeeStep csSetNewbeeStep, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        SC_SetNewbeeStep.Builder newbeeStepResult = SC_SetNewbeeStep.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            newbeeStepResult.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_UnknownError).build());
            gameServerTcpChannel.send(MsgIdEnum.SC_SetNewbeeStep_VALUE, newbeeStepResult);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                newbeeStepResult.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_UnknownError).build());
                gameServerTcpChannel.send(MsgIdEnum.SC_SetNewbeeStep_VALUE, newbeeStepResult);
                return;
            }

            DB_NewBee.Builder newBeeInfoBuilder = db_data.getNewBeeInfoBuilder();

            //判断是否是-1,-1:玩家已经完成新手引导,就不处理直接返回
            if (newBeeInfoBuilder.getPlayerNewbeeStepCount() > 0
                    && newBeeInfoBuilder.getPlayerNewbeeStep(0) == -1) {
                return;
            }

            newBeeInfoBuilder.clearPlayerNewbeeStep();
            newBeeInfoBuilder.addAllPlayerNewbeeStep(csSetNewbeeStep.getNewbeeStepList());
        });

        newbeeStepResult.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success).build());
        gameServerTcpChannel.send(MsgIdEnum.SC_SetNewbeeStep_VALUE, newbeeStepResult);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
