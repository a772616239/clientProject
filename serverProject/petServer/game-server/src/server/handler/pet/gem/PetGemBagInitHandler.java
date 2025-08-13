package server.handler.pet.gem;

import org.apache.commons.lang.StringUtils;
import util.GameUtil;
import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_PetGemBagInit_VALUE;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetGemBagInit;
import protocol.PetMessage.Gem;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 客户端打开宝石背包
 */
@MsgId(msgId = CS_PetGemBagInit_VALUE)
public class PetGemBagInitHandler extends AbstractBaseHandler<CS_PetGemBagInit> {

    @Override
    protected CS_PetGemBagInit parse(byte[] bytes) throws Exception {
        return CS_PetGemBagInit.parseFrom(bytes);
    }

    private static final int startPage = 1;

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemBagInit csPetGemBagInit, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        if (StringUtils.isEmpty(playerId)){
            return;
        }
        petgemEntity gemCache = petgemCache.getInstance().getEntityByPlayer(playerId);
        PetMessage.SC_PetGemBagInit.Builder resultBuilder = PetMessage.SC_PetGemBagInit.newBuilder();

        resultBuilder.setCapacity(gemCache.getCapacity());
        resultBuilder.setEnlargeTime(gemCache.getBagEnlarge());
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        int gemSize = gemCache.getGemListBuilder().getGemsCount();
        if (gemSize <= 0) {
            gsChn.send(MsgIdEnum.SC_PetGemBagInit_VALUE, resultBuilder);
            return;
        }

        int msgMaxNum = GameConfig.getById(GameConst.CONFIG_ID).getMsgmaxnum();
        resultBuilder.setTotalPage((int) Math.ceil((1.0 * gemSize) / msgMaxNum));

        resultBuilder.setPageNum(startPage);
        for (Gem value : gemCache.getGemListBuilder().getGemsMap().values()) {
            resultBuilder.addGem(value);
            if (resultBuilder.getGemCount() >= msgMaxNum) {
                gsChn.send(MsgIdEnum.SC_PetGemBagInit_VALUE, resultBuilder);
                resultBuilder.clearGem();
                resultBuilder.setPageNum(resultBuilder.getPageNum() + 1);
            }
        }
        if (resultBuilder.getPageNum() == resultBuilder.getTotalPage()) {
            gsChn.send(MsgIdEnum.SC_PetGemBagInit_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemBagInit_VALUE, PetMessage.SC_PetGemBagInit.newBuilder().setResult(retCode));
    }
}
