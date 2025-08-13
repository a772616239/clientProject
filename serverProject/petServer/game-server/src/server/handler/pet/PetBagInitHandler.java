package server.handler.pet;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;

import java.util.Collection;

import model.pet.entity.petEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetBagInit;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetBagInit;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.GameUtil;


/**
 * 处理客户端打开背包请求
 *
 * @author xiao_FL
 * @date 2019/5/15
 */
@MsgId(msgId = MsgIdEnum.CS_PetBagInit_VALUE)
public class PetBagInitHandler extends AbstractBaseHandler<CS_PetBagInit> {

    @Override
    protected CS_PetBagInit parse(byte[] bytes) throws Exception {
        return CS_PetBagInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetBagInit req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity petCacheTemp = model.pet.dbCache.petCache.getInstance().getEntityByPlayer(playerId);

        SC_PetBagInit.Builder resultBuilder = SC_PetBagInit.newBuilder();
        if (petCacheTemp == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetBagInit_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setCapacity(petCacheTemp.getCapacity());
        resultBuilder.setEnlargeTime(petCacheTemp.getBagenlarge());
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        Collection<Pet> allPet = petCacheTemp.peekAllPetByUnModify();
        //默认至少有一页
        int msgMaxNum = GameConfig.getById(GameConst.CONFIG_ID).getMsgmaxnum();
        int totalPage = Math.max((int) Math.ceil((1.0 * allPet.size()) / msgMaxNum), 1);
        resultBuilder.setTotalPage(totalPage);
        if (allPet.isEmpty()) {
            gsChn.send(MsgIdEnum.SC_PetBagInit_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setPageNum(1);
        for (Pet pet : allPet) {
            resultBuilder.addPet(pet);

            if (resultBuilder.getPetCount() >= msgMaxNum) {
                gsChn.send(MsgIdEnum.SC_PetBagInit_VALUE, resultBuilder);

                //设置下次发送页数
                resultBuilder.setPageNum(resultBuilder.getPageNum() + 1);
                resultBuilder.clearPet();
            }
        }

        //还有剩余宠物
        if (resultBuilder.getPetCount() > 0) {
            gsChn.send(MsgIdEnum.SC_PetBagInit_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetBagInit_VALUE, protocol.PetMessage.SC_PetBagInit.newBuilder().setResult(retCode));
    }


 }
