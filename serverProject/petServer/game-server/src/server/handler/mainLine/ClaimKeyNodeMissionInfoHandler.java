package server.handler.mainLine;

import cfg.KeyNodeConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import protocol.Common;
import protocol.MainLine;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_KeyNodeMissionInfo_VALUE;


@MsgId(msgId = MessageId.MsgIdEnum.CS_KeyNodeMissionInfo_VALUE)
public class ClaimKeyNodeMissionInfoHandler extends AbstractBaseHandler<MainLine.CS_KeyNodeMissionInfo> {
    @Override
    protected MainLine.CS_KeyNodeMissionInfo parse(byte[] bytes) throws Exception {
        return MainLine.CS_KeyNodeMissionInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MainLine.CS_KeyNodeMissionInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine == null) {
            MainLine.SC_KeyNodeMissionInfo.Builder msg = MainLine.SC_KeyNodeMissionInfo.newBuilder();
            gsChn.send(SC_KeyNodeMissionInfo_VALUE, msg);
            return;
        }
        checkAllKeyNodeClaimPlayerThenOpenNewKeyNode(mainLine);

        mainLine.sendKeyNodeMissions();
    }


    /**
     * 检查玩家完成所有关键节点后，策划开放新关键节点，这个 时候需要修改数据
     * @param mainLine
     */
    private void checkAllKeyNodeClaimPlayerThenOpenNewKeyNode(mainlineEntity mainLine) {
        if (!mainLine.getDBBuilder().getCurKeyNodeClaim()){
            return;
        }
        SyncExecuteFunction.executeConsumer(mainLine, m->{
            int keyNodeId = m.getDBBuilder().getKeyNodeId();
            int nextNode = KeyNodeConfig.getInstance().findNextKeyNodeId(keyNodeId);
            if (nextNode > keyNodeId) {
                mainLine.getDBBuilder().setKeyNodeId(keyNodeId);
                m.getDBBuilder().setCurKeyNodeClaim(false);
            }
        });
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_KeyNodeMissionInfo_VALUE, MainLine.SC_KeyNodeMissionInfo.newBuilder());
    }
}
