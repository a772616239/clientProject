package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Collection;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.SC_RefreshOpponent;
import protocol.Arena.SC_RefreshOpponent.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_RefreshOpponent;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/13
 */
@MsgId(msgId = MsgIdEnum.CS_GS_RefreshOpponent_VALUE)
public class CsGsRefreshOpponentHandler extends AbstractHandler<CS_GS_RefreshOpponent> {
    @Override
    protected CS_GS_RefreshOpponent parse(byte[] bytes) throws Exception {
        return CS_GS_RefreshOpponent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_RefreshOpponent req, int i) {

        Builder resultBuilder = SC_RefreshOpponent.newBuilder();
        if (req.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(req.getRetCode());
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        arenaEntity entity = arenaCache.getByIdx(req.getPlayerIdx());
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.refreshOpponent(req.getOpponentList(), req.getSpecifyOpponentMap());
            entity.getDbBuilder().clearVictoryIdx();
            entity.getDbBuilder().clearTempOpponent();
            Collection<ArenaOpponentTotalInfo> values = entity.getDbBuilder().getOpponentMap().values();
            for (ArenaOpponentTotalInfo value : values) {
                resultBuilder.addOpponnentInfo(value.getOpponnentInfo());
            }
            resultBuilder.addAllVictoryIdx(entity.getDbBuilder().getVictoryIdxList());
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
    }
}
