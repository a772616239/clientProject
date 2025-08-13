package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_UseMistVipSkill;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_UseMistVipSkill;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UseMistVipSkill_VALUE)
public class UseVipSkillHandler extends AbstractBaseHandler<CS_UseMistVipSkill> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UseMistVipSkill_VALUE, SC_UseMistVipSkill.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_UseMistVipSkill parse(byte[] bytes) throws Exception {
        return CS_UseMistVipSkill.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UseMistVipSkill req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
//        boolean exist = false;
//        for (MistVipSkillData skillData : player.getDb_data().getMistForestData().getVipSkillDataList()) {
//            if (skillData.getSkillId() == req.getSkillId()) {
//                exist = true;
//                break;
//            }
//        }
//        if (exist) {
//            CrossServerManager.getInstance().transferMsgToMistForest(
//                    playerIdx, MsgIdEnum.CS_UseMistVipSkill_VALUE, req.toByteString(), true);
//        }

        CrossServerManager.getInstance().transferMsgToMistForest(
                playerIdx, MsgIdEnum.CS_UseMistVipSkill_VALUE, req.toByteString(), true);
    }
}
