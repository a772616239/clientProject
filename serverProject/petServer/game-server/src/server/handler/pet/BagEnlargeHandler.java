package server.handler.pet;

import protocol.Common.EnumFunction;
import cfg.PetBagConfig;
import cfg.PetBagConfigObject;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetBagEnlarge;
import protocol.PetMessage.SC_PetBagEnlarge;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.GameUtil;
import static protocol.MessageId.MsgIdEnum.CS_PetBagEnlarge_VALUE;

import java.util.Collections;
import java.util.List;

/**
 * 处理客户端扩容背包（宠物和符文）请求
 *
 * @author xiao_FL
 * @date 2019/5/20
 */
@MsgId(msgId = CS_PetBagEnlarge_VALUE)
public class BagEnlargeHandler extends AbstractBaseHandler<CS_PetBagEnlarge> {

    @Override
    protected CS_PetBagEnlarge parse(byte[] bytes) throws Exception {
        return CS_PetBagEnlarge.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetBagEnlarge req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetBagEnlarge.Builder resultBuilder = SC_PetBagEnlarge.newBuilder();

        RetCodeEnum retCode = null;
        int capacity;
        int bagEnlarge;

        //宠物背包扩容
        if (req.getBagType() == 0) {
            petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
            if (entity == null) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PetBagEnlarge_VALUE, resultBuilder);
                return;
            }
            retCode = petBagEnlarge(entity,req.getEnlargeTimes());
            capacity = entity.getCapacity();
            bagEnlarge = entity.getBagenlarge();

            //符文背包扩容
        } else {
            petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerId);
            if (entity == null) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PetBagEnlarge_VALUE, resultBuilder);
                return;
            }

            retCode = runeBagEnlarge(entity);
            capacity = entity.getCapacity();
            bagEnlarge = entity.getBagEnlarge();
        }

        if (retCode != RetCodeEnum.RCE_Success) {
            resultBuilder.setResult(GameUtil.buildRetCode(retCode));
        } else {
            resultBuilder.setCapacity(capacity);
            resultBuilder.setEnlargeTime(bagEnlarge);
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        }
        gsChn.send(MsgIdEnum.SC_PetBagEnlarge_VALUE, resultBuilder);
    }

    private RetCodeEnum petBagEnlarge(petEntity entity, int reqEnlargeTimes) {
        int curEnlargeTime = entity.getBagenlarge();
        //背包增加容易
        int bagIncrease = 0;
        //扩容消耗
        List<Consume> consumes = null;
        for (int i = 0; i < reqEnlargeTimes; i++) {
            PetBagConfigObject bagConfig = PetBagConfig.getByEnlargetime(curEnlargeTime + i);
            if (bagConfig == null) {
                return RetCodeEnum.RCE_ErrorParam;
            }
            consumes = ConsumeUtil.mergeConsumeByTypeAndId(consumes, Collections.singletonList(ConsumeUtil.parseConsume(bagConfig.getPetbagconsume())));
            bagIncrease += bagConfig.getPetbagenlarge();
        }
        if (!ConsumeManager.getInstance().consumeMaterialByList(entity.getPlayeridx(), consumes,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetBagLvlUp))) {
            return RetCodeEnum.RCE_PrepareWar_DiamondNotEnought;
        }

        int finalBagIncrease = bagIncrease;
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            cacheTemp.setBagenlarge(curEnlargeTime + reqEnlargeTimes);
            cacheTemp.setCapacity(cacheTemp.getCapacity() + finalBagIncrease);
        });

        return RetCodeEnum.RCE_Success;
    }

    public RetCodeEnum runeBagEnlarge(petruneEntity entity) {
        int enlargeTime = entity.getBagEnlarge();
        // 扣除物品
        PetBagConfigObject bagConfig = PetBagConfig.getByEnlargetime(enlargeTime);
        if (bagConfig == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        Consume consume = ConsumeUtil.parseConsume(bagConfig.getPetrunebagconsume());
        if (!ConsumeManager.getInstance().consumeMaterial(entity.getPlayeridx(), consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneBagLvlUp))) {
            return RetCodeEnum.RCE_PrepareWar_DiamondNotEnought;
        }

        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            cacheTemp.setBagEnlarge(enlargeTime + 1);
            cacheTemp.setCapacity(cacheTemp.getCapacity() + bagConfig.getPetrunebagenlarge());
        });

        return RetCodeEnum.RCE_Success;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetBagEnlarge_VALUE, PetMessage.SC_PetBagEnlarge.newBuilder().setResult(retCode));
    }


 }
