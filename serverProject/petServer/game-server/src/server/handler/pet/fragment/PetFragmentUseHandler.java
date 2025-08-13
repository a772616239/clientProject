package server.handler.pet.fragment;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.petfragment.dbCache.service.PetFragmentService;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.FragmentUseResult;
import protocol.Common.EnumFunction;
import protocol.PetMessage.CS_PetFragmentUse;
import protocol.PetMessage.SC_PetFragmentUse;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetFragmentUse_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetFragmentUse_VALUE;

/**
 * 处理客户端请求宠物碎片合成消息
 *
 * @author xiao_FL
 * @date 2019/5/20
 */
@MsgId(msgId = CS_PetFragmentUse_VALUE)
public class PetFragmentUseHandler extends AbstractBaseHandler<CS_PetFragmentUse> {
    PetFragmentService petFragmentService = PetFragmentServiceImpl.getInstance();

    @Override
    protected CS_PetFragmentUse parse(byte[] bytes) throws Exception {
        return CS_PetFragmentUse.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetFragmentUse req, int i) {
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());

        LogUtil.info("receive player:{} use petFragment,req:{}", playerId, req);
        try {
            SC_PetFragmentUse.Builder result = SC_PetFragmentUse.newBuilder();
            if (req.getAmount() <= 0) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gameServerTcpChannel.send(SC_PetFragmentUse_VALUE, result);
                return;
            }

            // 宠物背包容量足够
            if (petCache.getInstance().capacityEnough(playerId, req.getAmount())) {
                // 合成宠物
                FragmentUseResult useResult = petFragmentService.useFragment(req.getId(), playerId, req.getAmount(), true, false);
                result.setResult(GameUtil.buildRetCode(useResult.getCodeEnum()));
                LogUtil.info(" player:{} use petFragment,result:{}", playerId, useResult);
            } else {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_PetBagNotEnough));
            }
            gameServerTcpChannel.send(SC_PetFragmentUse_VALUE, result);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_PetFragment;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetFragmentUse_VALUE, SC_PetFragmentUse.newBuilder().setResult(retCode));
    }
}
