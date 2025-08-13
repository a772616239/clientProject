package server.handler.pet.mission;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetMissionAbandon;
import protocol.PetMessage.SC_PetMissionAbandon;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionAbandon_VALUE;

/**
 * 处理客户端放弃委托请求
 *
 * @author xiao_FL
 * @date 2019/6/24
 */
@MsgId(msgId = CS_PetMissionAbandon_VALUE)
public class PetMissionAbandonHandler extends AbstractBaseHandler<CS_PetMissionAbandon> {

    @Override
    protected CS_PetMissionAbandon parse(byte[] bytes) throws Exception {
        return CS_PetMissionAbandon.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetMissionAbandon csPetMissionAbandon, int i) {
//        LogUtil.info("recv petMissionAbandon msg:" + csPetMissionAbandon.toString());
//        try {
//            // 获取当前channel对应playerId
//            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
//            MissionResult missionResult = petMissionService.abandonPetMission(playerId, csPetMissionAbandon.getMissionId());
//            // 返回
//            SC_PetMissionAbandon.Builder result = SC_PetMissionAbandon.newBuilder();
//            RetCode.Builder retCode = RetCode.newBuilder();
//            if (missionResult.isSuccess()) {
//                retCode.setRetCode(RetCodeEnum.RCE_Success);
//                result.setMission(missionResult.getPetMission());
//                result.setResult(retCode);
//            } else {
//                retCode.setRetCode(missionResult.getCode());
//                result.setResult(retCode);
//            }
//            gameServerTcpChannel.send(SC_PetMissionAbandon_VALUE, result);
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionAbandon_VALUE, SC_PetMissionAbandon.newBuilder().setResult(retCode));
    }
}
