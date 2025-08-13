package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamMatchManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpEnterScene;
import protocol.CpFunction.SC_CpEnterScene;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 玩家进入组队场景
 */
@MsgId(msgId = MsgIdEnum.CS_CpEnterScene_VALUE)
public class CpEnterSceneHandler extends AbstractBaseHandler<CS_CpEnterScene> {
    @Override
    protected CS_CpEnterScene parse(byte[] bytes) throws Exception {
        return CS_CpEnterScene.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpEnterScene req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        CpTeamMatchManger.getInstance().playerEnterScene(playerIdx);
        SC_CpEnterScene.Builder msg = SC_CpEnterScene.newBuilder();
        gsChn.send(MsgIdEnum.SC_CpEnterScene_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpEnterScene_VALUE, SC_CpEnterScene.newBuilder());
    }
}
