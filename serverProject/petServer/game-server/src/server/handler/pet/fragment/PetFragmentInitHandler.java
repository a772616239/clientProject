package server.handler.pet.fragment;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petfragment.dbCache.service.PetFragmentService;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.petfragmentEntity;
import protocol.Common.EnumFunction;
import protocol.PetMessage.CS_PetFragmentInit;
import protocol.PetMessage.SC_PetFragmentInit;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetFragmentInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetFragmentInit_VALUE;

/**
 * 处理客户端获取所有碎片请求
 *
 * @author xiao_FL
 * @date 2019/5/20
 */
@MsgId(msgId = CS_PetFragmentInit_VALUE)
public class PetFragmentInitHandler extends AbstractBaseHandler<CS_PetFragmentInit> {
    private PetFragmentService petFragmentService = PetFragmentServiceImpl.getInstance();

    @Override
    protected CS_PetFragmentInit parse(byte[] bytes) throws Exception {
        return CS_PetFragmentInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetFragmentInit csPetFragmentInit, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        // 获取玩家的所有碎片
        petfragmentEntity queryResult = petFragmentService.getFragmentByPlayer(playerId);
        // 返回
        SC_PetFragmentInit.Builder result = SC_PetFragmentInit.newBuilder();
        if (queryResult.getFragmentList() != null) {
            result.addAllPetFragment(queryResult.getFragmentList());
        }
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        gameServerTcpChannel.send(SC_PetFragmentInit_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_PetFragment;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetFragmentInit_VALUE, SC_PetFragmentInit.newBuilder().setResult(retCode));
    }


}
