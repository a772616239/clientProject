package server.handler.stoneRift.worldMap;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftManager;
import model.stoneRift.StoneRiftWorldMapManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_LeaveStoneRiftWorldMap;
import protocol.StoneRift.SC_LeaveStoneRiftWorldMap;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_LeaveStoneRiftWorldMap_VALUE;

/**
 * 离开世界地图
 */
@MsgId(msgId = MsgIdEnum.CS_LeaveStoneRiftWorldMap_VALUE)
public class LeaveStoneRiftWorldMapHandler extends AbstractBaseHandler<CS_LeaveStoneRiftWorldMap> {

    @Override
    protected CS_LeaveStoneRiftWorldMap parse(byte[] bytes) throws Exception {
        return CS_LeaveStoneRiftWorldMap.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LeaveStoneRiftWorldMap req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_LeaveStoneRiftWorldMap.Builder msg = leaveStoneRiftWorldMap(playerId);
        GlobalData.getInstance().sendMsg(playerId, SC_LeaveStoneRiftWorldMap_VALUE, msg);

    }

    private SC_LeaveStoneRiftWorldMap.Builder leaveStoneRiftWorldMap(String playerId) {
        SC_LeaveStoneRiftWorldMap.Builder msg = SC_LeaveStoneRiftWorldMap.newBuilder();
        StoneRiftWorldMapManager.getInstance().leaveRiftWorldMap(playerId);
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_LeaveStoneRiftWorldMap_VALUE, SC_LeaveStoneRiftWorldMap.newBuilder());

    }
}
