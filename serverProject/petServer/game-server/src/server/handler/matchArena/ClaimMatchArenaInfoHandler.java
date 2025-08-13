package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_ClaimMatchArenaInfo;
import protocol.MatchArena.SC_ClaimMatchArenaInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimMatchArenaInfo_VALUE)
public class ClaimMatchArenaInfoHandler extends AbstractBaseHandler<CS_ClaimMatchArenaInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimMatchArenaInfo.Builder resultBuilder = SC_ClaimMatchArenaInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimMatchArenaInfo_VALUE, resultBuilder);
    }


    @Override
    protected CS_ClaimMatchArenaInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimMatchArenaInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMatchArenaInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);

        SC_ClaimMatchArenaInfo.Builder resultBuilder = SC_ClaimMatchArenaInfo.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_MatchArena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimMatchArenaInfo_VALUE, resultBuilder);
            return;
        }

        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMatchArenaInfo_VALUE, resultBuilder);
            return;
        }

        entity.sendInfo();
    }
}
