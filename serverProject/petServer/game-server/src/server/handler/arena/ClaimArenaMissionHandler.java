package server.handler.arena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Arena.CS_ClaimArenaMission;
import protocol.Arena.SC_ClaimArenaMission;
import protocol.Arena.SC_ClaimArenaMission.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.09.02
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaMission_VALUE)
public class ClaimArenaMissionHandler extends AbstractBaseHandler<CS_ClaimArenaMission> {
    @Override
    protected CS_ClaimArenaMission parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Builder resultBuilder = SC_ClaimArenaMission.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimArenaMission_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllMission(entity.getDb_Builder().getArenaMission().getMissionsMap().values());
        gsChn.send(MsgIdEnum.SC_ClaimArenaMission_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaMission_VALUE, SC_ClaimArenaMission.newBuilder().setRetCode(retCode));
    }
}
