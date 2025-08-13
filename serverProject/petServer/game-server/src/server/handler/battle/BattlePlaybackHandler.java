package server.handler.battle;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Objects;
import model.battlerecord.dbCache.battlerecordCache;
import model.battlerecord.entity.battlerecordEntity;
import protocol.Battle.CS_BattlePlayback;
import protocol.Battle.SC_BattlePlayback;
import protocol.BattleRecordDB.DB_ServerBattleRecord;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/4/21
 */
@MsgId(msgId = MsgIdEnum.CS_BattlePlayback_VALUE)
public class BattlePlaybackHandler extends AbstractBaseHandler<CS_BattlePlayback> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_BattlePlayback.Builder resultBuilder = SC_BattlePlayback.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_BattlePlayback_VALUE, resultBuilder);
    }

    @Override
    protected CS_BattlePlayback parse(byte[] bytes) throws Exception {
        return CS_BattlePlayback.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattlePlayback req, int i) {
        String battleId = String.valueOf(req.getBattleId());
        battlerecordEntity entity = battlerecordCache.getInstance().getEntity(battleId);

        SC_BattlePlayback.Builder resultBuilder = SC_BattlePlayback.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BattlePlayback_VALUE, resultBuilder);
            return;
        }

        if (!Objects.equals(req.getPlaybackVersion(), entity.getVersion())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_BattlePlayBack_VersionMissMatching));
            gsChn.send(MsgIdEnum.SC_BattlePlayback_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ServerBattleRecord.Builder dataBuilder = entity.getServerBattleRecordBuilder();
            if (dataBuilder != null) {
                resultBuilder.setEnterFightData(dataBuilder.getEnterFight());
                resultBuilder.addAllFrameData(dataBuilder.getFrameDataList());
                resultBuilder.setBattleResult(dataBuilder.getBattleResult());
            }
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_BattlePlayback_VALUE, resultBuilder);
    }
}
