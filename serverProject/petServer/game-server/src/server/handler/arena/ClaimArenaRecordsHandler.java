package server.handler.arena;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.util.PlayerUtil;
import protocol.Arena.CS_ClaimArenaRecords;
import protocol.Arena.SC_ClaimArenaRecords;
import protocol.Arena.SC_ClaimArenaRecords.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.12
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaRecords_VALUE)
public class ClaimArenaRecordsHandler extends AbstractBaseHandler<CS_ClaimArenaRecords> {
    @Override
    protected CS_ClaimArenaRecords parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaRecords.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaRecords req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        arenaEntity entity = arenaCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_ClaimArenaRecords.newBuilder();
        if (entity == null || PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimArenaRecords_VALUE, resultBuilder);
            return;
        }

        resultBuilder.addAllRecords(entity.getDbBuilder().getRecordsList());
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimArenaRecords_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaRecords_VALUE, SC_ClaimArenaRecords.newBuilder().setRetCode(retCode));
    }
}
