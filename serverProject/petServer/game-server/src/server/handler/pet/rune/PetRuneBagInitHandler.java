package server.handler.pet.rune;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetRuneBagInit;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneBagInit;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneBagInit_VALUE;

/**
 * 处理客户端打开符文背包请求
 *
 * @author xiao_FL
 * @date 2019/5/31
 */
@MsgId(msgId = CS_PetRuneBagInit_VALUE)
public class PetRuneBagInitHandler extends AbstractBaseHandler<CS_PetRuneBagInit> {

    @Override
    protected CS_PetRuneBagInit parse(byte[] bytes) throws Exception {
        return CS_PetRuneBagInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneBagInit csPetRuneBagInit, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetRuneBagInit.Builder resultBuilder = SC_PetRuneBagInit.newBuilder();
        petruneEntity runeCache = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (runeCache == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetRuneBagInit_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setCapacity(runeCache.getCapacity());
        resultBuilder.setEnlargeTime(runeCache.getBagEnlarge());
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        int runeSize = runeCache.getRuneListBuilder().getRuneCount();
        if (runeSize <= 0) {
            gsChn.send(MsgIdEnum.SC_PetRuneBagInit_VALUE, resultBuilder);
            return;
        }

        int msgMaxNum = GameConfig.getById(1).getMsgmaxnum();
        resultBuilder.setTotalPage((int) Math.ceil((1.0 * runeSize) / msgMaxNum));

        resultBuilder.setPageNum(1);
        for (Rune value : runeCache.getRuneListBuilder().getRuneMap().values()) {
            resultBuilder.addRune(value);
            if (resultBuilder.getRuneCount() >= msgMaxNum) {
                gsChn.send(MsgIdEnum.SC_PetRuneBagInit_VALUE, resultBuilder);
                resultBuilder.clearRune();
                resultBuilder.setPageNum(resultBuilder.getPageNum() + 1);
            }
        }
        if (resultBuilder.getPageNum() == resultBuilder.getTotalPage()) {
            gsChn.send(MsgIdEnum.SC_PetRuneBagInit_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneBag;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetRuneBagInit_VALUE, SC_PetRuneBagInit.newBuilder().setResult(retCode));
    }
}
