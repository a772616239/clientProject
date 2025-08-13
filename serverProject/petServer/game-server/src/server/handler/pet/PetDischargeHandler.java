package server.handler.pet;

import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetDisCharge;
import protocol.PetMessage.SC_PetDisCharge;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetDisCharge_VALUE;

/**
 * 处理客户端宠物放生请求
 *
 * @author xiao_FL
 * @date 2019/5/20
 */
@MsgId(msgId = CS_PetDisCharge_VALUE)
public class PetDischargeHandler extends AbstractBaseHandler<CS_PetDisCharge> {

    @Override
    protected CS_PetDisCharge parse(byte[] bytes) throws Exception {
        return CS_PetDisCharge.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetDisCharge req, int i) {
        SC_PetDisCharge.Builder result = SC_PetDisCharge.newBuilder();
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        LogUtil.info("receive player:{} pet discharge,req:{}", playerId, req);
        // 放生宠物
        Common.RewardSourceEnum sourceEnum = GameConst.Discharge.free == req.getDischarge() ? Common.RewardSourceEnum.RSE_PetDischarge : Common.RewardSourceEnum.RSE_PetReBorn;
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(sourceEnum);
        RetCodeEnum retCodeEnum = petCache.getInstance().dischargePet(playerId, req.getIdList(), req.getDischarge(), result, reason);
        LogUtil.info("receive player:{} pet discharge,result:{}", playerId, retCodeEnum);
        result.setResult(GameUtil.buildRetCode(retCodeEnum));
        gameServerTcpChannel.send(MsgIdEnum.SC_PetDisCharge_VALUE, result);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDischarge;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetDisCharge_VALUE, protocol.PetMessage.SC_PetDisCharge.newBuilder().setResult(retCode));
    }


 }
