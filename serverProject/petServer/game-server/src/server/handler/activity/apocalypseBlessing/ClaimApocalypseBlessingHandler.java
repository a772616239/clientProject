package server.handler.activity.apocalypseBlessing;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.TargetSystemDB;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimApocalypseBlessingInfo_VALUE)
public class ClaimApocalypseBlessingHandler extends AbstractBaseHandler<Activity.CS_ClaimApocalypseBlessingInfo> {
    @Override
    protected Activity.CS_ClaimApocalypseBlessingInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimApocalypseBlessingInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimApocalypseBlessingInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_ClaimApocalypseBlessingInfo.Builder resultBuilder = Activity.SC_ClaimApocalypseBlessingInfo.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimApocalypseBlessingInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetSystemDB.DB_ApocalypseBlessing bless = entity.getDb_Builder().getSpecialInfo().getBless();
            resultBuilder.setCurPro(bless.getCurPro());
            resultBuilder.addAllMissionPro(bless.getMissionProMap().values());
        });
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimApocalypseBlessingInfo_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}

