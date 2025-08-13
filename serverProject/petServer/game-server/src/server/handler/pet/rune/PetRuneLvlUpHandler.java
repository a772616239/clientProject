package server.handler.pet.rune;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.PetRuneUpResult;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetRuneLvlUp;
import protocol.PetMessage.SC_PetRuneLvlUp;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneLvlUp_VALUE;

/**
 * @author xiao_FL
 * @date 2019/6/3
 */
@MsgId(msgId = CS_PetRuneLvlUp_VALUE)
public class PetRuneLvlUpHandler extends AbstractBaseHandler<CS_PetRuneLvlUp> {

    @Override
    protected CS_PetRuneLvlUp parse(byte[] bytes) throws Exception {
        return CS_PetRuneLvlUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetRuneLvlUp req, int i) {
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());

        LogUtil.info("receive player:{} petRuneLv up,req", playerId, req);
        SC_PetRuneLvlUp.Builder result = SC_PetRuneLvlUp.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        result.setResult(retCode);
        // 升级
        PetRuneUpResult petRuneUpResult = petruneCache.getInstance().runeLvlUp(playerId, req);
        if (petRuneUpResult.isSuccess()) {
            // 成功
            retCode.setRetCode(RetCodeEnum.RCE_Success);
            result.setResult(retCode);
            if (!CollectionUtils.isEmpty(petRuneUpResult.getConvertRuneExp())) {
                result.addAllRemainRuneExp(petRuneUpResult.getConvertRuneExp());
            }
            result.setRune(petRuneUpResult.getRune());
        } else {
            retCode.setRetCode(petRuneUpResult.getCode());
            result.setResult(retCode);
        }
        if (petRuneUpResult.getRune() != null) {
            result.setRuneLv(petRuneUpResult.getRune().getRuneLvl());
        }
        LogUtil.info("player:{} petRuneLv up success,now rune:{}", playerId, petRuneUpResult.getRune());
        gameServerTcpChannel.send(MsgIdEnum.SC_PetRuneLvlUp_VALUE, result);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneUp;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneLvlUp_VALUE, SC_PetRuneLvlUp.newBuilder().setResult(retCode));
    }


}
