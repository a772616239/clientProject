package server.handler.player;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_UpdateFunctionUnlockAnimation;
import protocol.PlayerInfo.SC_UpdateFunctionUnlockAnimation;
import protocol.PlayerInfo.SC_UpdateFunctionUnlockAnimation.Builder;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.08.31
 */
@MsgId(msgId = MsgIdEnum.CS_UpdateFunctionUnlockAnimation_VALUE)
public class UpdateFunctionUnlockAnimationHandler extends AbstractBaseHandler<CS_UpdateFunctionUnlockAnimation> {
    @Override
    protected CS_UpdateFunctionUnlockAnimation parse(byte[] bytes) throws Exception {
        return CS_UpdateFunctionUnlockAnimation.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateFunctionUnlockAnimation req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);

        Builder resultBuilder = SC_UpdateFunctionUnlockAnimation.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_UpdateFunctionUnlockAnimation_VALUE, resultBuilder);
            return;
        }


        SyncExecuteFunction.executeConsumer(player, p -> {
            if (!player.getDb_data().getFunctionUnlockAnimationList().contains(req.getNewFunction())) {
                player.getDb_data().addFunctionUnlockAnimation(req.getNewFunction());
            }
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_UpdateFunctionUnlockAnimation_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
