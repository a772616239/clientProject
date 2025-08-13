package server.handler.arena;

import cfg.ArenaConfig;
import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.util.PlayerUtil;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Arena.CS_RefreshOpponent;
import protocol.Arena.SC_RefreshOpponent;
import protocol.Arena.SC_RefreshOpponent.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_RefreshOpponent;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.09
 */
@MsgId(msgId = MsgIdEnum.CS_RefreshOpponent_VALUE)
public class RefreshOpponentHandler extends AbstractBaseHandler<CS_RefreshOpponent> {
    @Override
    protected CS_RefreshOpponent parse(byte[] bytes) throws Exception {
        return CS_RefreshOpponent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreshOpponent req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_RefreshOpponent.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        arenaEntity entity = arenaCache.getByIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime - entity.getDbBuilder().getLastRefreshTime() < ArenaConfig.getById(GameConst.CONFIG_ID).getRefreshinterval()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_RefreshFrequently));
            gsChn.send(MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        GS_CS_RefreshOpponent.Builder refreshBuilder = GS_CS_RefreshOpponent.newBuilder();
        refreshBuilder.setPlayerIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            List<String> unBeatDirectUpPlayer = entity.getUnBeatDirectUpPlayer();
            if (CollectionUtils.isNotEmpty(unBeatDirectUpPlayer)) {
                refreshBuilder.addAllSpecifyIdx(unBeatDirectUpPlayer);
            }
        });

        if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_RefreshOpponent_VALUE, refreshBuilder, false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_CanNotFindServer));
            gsChn.send(MsgIdEnum.SC_RefreshOpponent_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RefreshOpponent_VALUE, SC_RefreshOpponent.newBuilder().setRetCode(retCode));
    }
}
