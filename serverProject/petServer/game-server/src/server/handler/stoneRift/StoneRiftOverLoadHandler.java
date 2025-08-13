package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_StoneRiftOverLoad;
import protocol.StoneRift.SC_StoneRiftOverLoad;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_StoneRiftOverLoad_VALUE;

/**
 * 超载
 */
@MsgId(msgId = MsgIdEnum.CS_StoneRiftOverLoad_VALUE)
public class StoneRiftOverLoadHandler extends AbstractBaseHandler<CS_StoneRiftOverLoad> {

    @Override
    protected CS_StoneRiftOverLoad parse(byte[] bytes) throws Exception {
        return CS_StoneRiftOverLoad.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StoneRiftOverLoad req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_StoneRiftOverLoad.Builder msg = doOverLoad(playerId);

        GlobalData.getInstance().sendMsg(playerId, SC_StoneRiftOverLoad_VALUE, msg);

    }

    private SC_StoneRiftOverLoad.Builder doOverLoad(String playerId) {
        SC_StoneRiftOverLoad.Builder msg = SC_StoneRiftOverLoad.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        if (!entity.isOverloadUnlock()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            return msg;
        }
        if (entity.getDB_Builder().getNextCanOverLoad() < GlobalTick.getInstance().getCurrentTime()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.useOverLoad();
        });
        entity.sendOverLoadUpdate();
        sendAllFactoryUpdate(entity);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;
    }

    private void sendAllFactoryUpdate(stoneriftEntity entity) {
        for (Integer factoryId : entity.getDB_Builder().getFactoryMap().keySet()) {
            entity.sendFactoryUpdate(factoryId);
        }
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_StoneRiftOverLoad_VALUE, StoneRift.SC_StoneRiftOverLoad.newBuilder().setRetCode(retCode));

    }
}
