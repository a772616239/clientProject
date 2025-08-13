package server.handler.battle;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battlerecord.dbCache.battlerecordCache;
import model.battlerecord.entity.battlerecordEntity;
import protocol.Battle.CS_ClaimBattleStatistic;
import protocol.Battle.SC_ClaimBattleStatistic;
import protocol.BattleRecordDB.DB_ServerBattleRecord;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/4/21
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimBattleStatistic_VALUE)
public class ClaimBattleStatisticHandler extends AbstractBaseHandler<CS_ClaimBattleStatistic> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimBattleStatistic.Builder resultBuilder = SC_ClaimBattleStatistic.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimBattleStatistic_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimBattleStatistic parse(byte[] bytes) throws Exception {
        return CS_ClaimBattleStatistic.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimBattleStatistic req, int i) {
        String battleId = String.valueOf(req.getBattleId());
        battlerecordEntity entity = battlerecordCache.getInstance().getEntity(battleId);
        SC_ClaimBattleStatistic.Builder resultBuilder = SC_ClaimBattleStatistic.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimBattleStatistic_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setObserverCamp(1);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ServerBattleRecord.Builder dataBuilder = entity.getServerBattleRecordBuilder();
            if (dataBuilder != null) {
                resultBuilder.setStatisticData(dataBuilder.getStatisticData());
            }
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimBattleStatistic_VALUE, resultBuilder);
    }
}
