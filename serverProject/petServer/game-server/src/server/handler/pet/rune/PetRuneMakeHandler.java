package server.handler.pet.rune;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.reward.RewardUtil;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RuneReward;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetRuneMake;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneLvlUp;
import protocol.PetMessage.SC_PetRuneMake;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneMake_VALUE;


@MsgId(msgId = CS_PetRuneMake_VALUE)
public class PetRuneMakeHandler extends AbstractBaseHandler<CS_PetRuneMake> {

    @Override
    protected CS_PetRuneMake parse(byte[] bytes) throws Exception {
        return CS_PetRuneMake.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetRuneMake req, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());

        SC_PetRuneMake.Builder result = SC_PetRuneMake.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();

        if(req.getIdCount() <= 0){
            retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
            result.setResult(retCode);
            LogUtil.info("player:{} make petRune req id param error.now req:{}", playerId,req);
            gameServerTcpChannel.send(MsgIdEnum.SC_PetRuneMake_VALUE, result);
            return;
        }

        List<Rune> newRuneList = petruneCache.getInstance().makeRuneList(playerId, req);
        if (newRuneList != null && !newRuneList.isEmpty()) {
            // 成功
            retCode.setRetCode(RetCodeEnum.RCE_Success);
            result.setResult(retCode);
            result.addAllNewRune(newRuneList);

            List<RuneReward> runeRewardList = RewardUtil.runeToRuneReward(newRuneList);
            GlobalData.getInstance().sendDisMultiRewardsMsg(playerId, null, runeRewardList, RewardSourceEnum.RSE_MakeRune);

            petruneEntity.sendRuneGet(playerId, newRuneList);
        } else {
            retCode.setRetCode(RetCodeEnum.RCE_UnknownError);
            result.setResult(retCode);
        }

        gameServerTcpChannel.send(MsgIdEnum.SC_PetRuneMake_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRune;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetRuneMake_VALUE, SC_PetRuneLvlUp.newBuilder().setResult(retCode));
    }


}
