package server.handler.pet.fragment;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.petfragment.dbCache.service.PetFragmentService;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.FragmentUseResult;
import model.petfragment.entity.petfragmentEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.PetMessage;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.CS_PetFragmentAllUse_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetFragmentAllUse_VALUE;

/**
 * 宠物碎片一键合成
 */
@MsgId(msgId = CS_PetFragmentAllUse_VALUE)
public class PetFragmentAllUseHandler extends AbstractBaseHandler<PetMessage.CS_PetFragmentAllUse> {
    PetFragmentService petFragmentService = PetFragmentServiceImpl.getInstance();

    @Override
    protected PetMessage.CS_PetFragmentAllUse parse(byte[] bytes) throws Exception {
        return PetMessage.CS_PetFragmentAllUse.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, PetMessage.CS_PetFragmentAllUse csPetFragmentUse, int i) {
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        try {
            PetMessage.SC_PetFragmentAllUse.Builder result = PetMessage.SC_PetFragmentAllUse.newBuilder();
            petfragmentEntity queryResult = petFragmentService.getFragmentByPlayer(playerId);
            List<PetMessage.PetFragment> fragmentList = queryResult.getFragmentList();
            if (CollectionUtils.isEmpty(fragmentList)) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_FragmentNotEnough));
                gameServerTcpChannel.send(SC_PetFragmentAllUse_VALUE, result);
                return;
            }

            // 宠物背包容量足够
            if (petCache.getInstance().getRemainCapacity(playerId) < 1) {
                // 合成宠物
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_PetBagNotEnough));

            }
            FragmentUseResult fragmentUseResult = petFragmentService.useFragment(null, playerId, 0, false, true);
            if (!CollectionUtils.isEmpty(fragmentUseResult.getGainPet())) {
                result.addAllRewardList(fragmentUseResult.getGainPet());
            }
            result.setResult(GameUtil.buildRetCode(fragmentUseResult.getCodeEnum()));
            gameServerTcpChannel.send(SC_PetFragmentAllUse_VALUE, result);
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
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetFragmentAllUse_VALUE, PetMessage.SC_PetFragmentAllUse.newBuilder().setResult(retCode));
    }


}
