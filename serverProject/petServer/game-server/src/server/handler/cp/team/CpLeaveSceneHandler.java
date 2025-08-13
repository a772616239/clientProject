package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamMatchManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpLeaveScene;
import protocol.CpFunction.SC_CpLeaveScene;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 玩家离开组队场景
 */
@MsgId(msgId = MsgIdEnum.CS_CpLeaveScene_VALUE)
public class CpLeaveSceneHandler extends AbstractBaseHandler<CS_CpLeaveScene> {
    @Override
    protected CS_CpLeaveScene parse(byte[] bytes) throws Exception {
        return CS_CpLeaveScene.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpLeaveScene req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CpTeamMatchManger.getInstance().playerLeaveScene(playerIdx);
        SC_CpLeaveScene.Builder msg = SC_CpLeaveScene.newBuilder();
        gsChn.send(MsgIdEnum.SC_CpLeaveScene_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpLeaveScene_VALUE, SC_CpLeaveScene.newBuilder());
    }
}
