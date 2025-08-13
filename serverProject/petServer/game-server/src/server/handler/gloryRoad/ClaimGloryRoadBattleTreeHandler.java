package server.handler.gloryRoad;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_ClaimGloryRoadBattleTree;
import protocol.GloryRoad.SC_ClaimGloryRoadBattleTree;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/18
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimGloryRoadBattleTree_VALUE)
public class ClaimGloryRoadBattleTreeHandler extends AbstractBaseHandler<CS_ClaimGloryRoadBattleTree> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimGloryRoadBattleTree.Builder resultBuilder = SC_ClaimGloryRoadBattleTree.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.CS_ClaimGloryRoadBattleTree_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimGloryRoadBattleTree parse(byte[] bytes) throws Exception {
        return CS_ClaimGloryRoadBattleTree.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimGloryRoadBattleTree req, int i) {
        GloryRoadManager.getInstance().sendBattleTreeMsg(String.valueOf(gsChn.getPlayerId1()));
    }
}
