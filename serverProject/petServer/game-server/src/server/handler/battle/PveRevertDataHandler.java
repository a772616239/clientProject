package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattlePveRevertData;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BattlePveRevertData_VALUE)
public class PveRevertDataHandler extends AbstractBaseHandler<CS_BattlePveRevertData> {
    @Override
    protected CS_BattlePveRevertData parse(byte[] bytes) throws Exception {
        return CS_BattlePveRevertData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattlePveRevertData req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        if (BattleManager.getInstance().getBattleType(playerIdx) != BattleTypeEnum.BTE_PVE) {
            return;
        }

        BattleManager.getInstance().handleAllBattleFrameData(playerIdx, req.getFrameDataList());


//        BattleController bc = player.getBattleController();
//        if (bc.getBattleId() <= 0 || bc.getBattleType() != BattleTypeEnum.BTE_PVE_VALUE) {
//            return;
//        }
//        SyncExecuteFunction.executeConsumer(player, entity -> {
//            for (CS_FrameData frameData : req.getFrameDataList()) {
//                if (frameData.getFrameIndex() <= bc.getCurPveFrameIndex()) {
//                    continue;
//                }
//                entity.getBattleController().handleBattleFrameData(frameData);
//            }
//        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
