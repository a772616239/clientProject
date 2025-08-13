package server.handler.stoneRift.worldMap;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.stoneRift.StoneRiftManager;
import model.stoneRift.StoneRiftWorldMapManager;
import model.stoneRift.entity.StoneRiftMsg;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_EnterStoneRiftWorldMap;
import protocol.StoneRift.SC_EnterStoneRiftWorldMap;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_EnterStoneRiftWorldMap_VALUE;

@MsgId(msgId = MsgIdEnum.CS_EnterStoneRiftWorldMap_VALUE)
public class EnterStoneRiftWorldMapHandler extends AbstractBaseHandler<CS_EnterStoneRiftWorldMap> {

    @Override
    protected CS_EnterStoneRiftWorldMap parse(byte[] bytes) throws Exception {
        return CS_EnterStoneRiftWorldMap.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnterStoneRiftWorldMap req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_EnterStoneRiftWorldMap.Builder msg = EnterStoneRiftWorldMap(playerId);
        GlobalData.getInstance().sendMsg(playerId, SC_EnterStoneRiftWorldMap_VALUE, msg);

    }

    private SC_EnterStoneRiftWorldMap.Builder EnterStoneRiftWorldMap(String playerId) {
        SC_EnterStoneRiftWorldMap.Builder msg = SC_EnterStoneRiftWorldMap.newBuilder();
        StoneRiftWorldMapManager.getInstance().enterRiftWorldMap(playerId);
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_EnterStoneRiftWorldMap_VALUE, SC_EnterStoneRiftWorldMap.newBuilder());

    }
}
