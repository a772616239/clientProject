package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRift;
import model.stoneRift.entity.DbStoneRiftEvent;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_TriggerStoneRiftEvent;
import protocol.StoneRift.SC_TriggerStoneRiftEvent;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_TriggerStoneRiftEvent_VALUE;

@MsgId(msgId = MsgIdEnum.CS_TriggerStoneRiftEvent_VALUE)
public class StoneRiftTriggerEventHandler extends AbstractBaseHandler<CS_TriggerStoneRiftEvent> {

    @Override
    protected CS_TriggerStoneRiftEvent parse(byte[] bytes) throws Exception {
        return CS_TriggerStoneRiftEvent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_TriggerStoneRiftEvent req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_TriggerStoneRiftEvent.Builder msg = triggerStoneRiftEvent(playerId, req);

        GlobalData.getInstance().sendMsg(playerId, SC_TriggerStoneRiftEvent_VALUE, msg);

    }

    private SC_TriggerStoneRiftEvent.Builder triggerStoneRiftEvent(String playerId, CS_TriggerStoneRiftEvent req) {
        SC_TriggerStoneRiftEvent.Builder msg = SC_TriggerStoneRiftEvent.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        DbStoneRiftEvent event = entity.getDB_Builder().getEvent();
        if (event.isAlreadyTrigger()||event.getEvent()== StoneRift.StoneRiftEvent.SRE_NULL_VALUE){
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_EventInCD));
            return msg;
        }
        //下次时间段算奖励
        SyncExecuteFunction.executeConsumer(entity,et->{
            entity.randomEvent();
        });

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_TriggerStoneRiftEvent_VALUE, SC_TriggerStoneRiftEvent.newBuilder().setRetCode(retCode));

    }
}
