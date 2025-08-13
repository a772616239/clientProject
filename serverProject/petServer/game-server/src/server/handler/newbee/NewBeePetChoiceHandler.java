package server.handler.newbee;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.Newbee.CS_NewBeePetChoice;
import protocol.Newbee.SC_NewBeePetChoice;
import protocol.RetCodeId.RetCodeEnum;
import util.ArrayUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_NewBeePetChoice_VALUE)
public class NewBeePetChoiceHandler extends AbstractBaseHandler<CS_NewBeePetChoice> {
    @Override
    protected CS_NewBeePetChoice parse(byte[] bytes) throws Exception {
        return CS_NewBeePetChoice.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_NewBeePetChoice req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_NewBeePetChoice.Builder resultBuilder = SC_NewBeePetChoice.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        int petBookId = req.getPetBookId();
        if (!ArrayUtil.intArrayContain(GameConfig.getById(GameConst.CONFIG_ID).getNewbeechoicepet(), petBookId)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        if (player.getDb_data() == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
            return;
        }

        //判断节点是否已经领取
        boolean claimed = SyncExecuteFunction.executeFunction(player, p -> player.getDb_data().getNewBeeInfoBuilder().getNewBeePet());
        if (claimed){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
            return;
        }

        RewardManager.getInstance().doReward(playerIdx, RewardUtil.parseReward(RewardTypeEnum.RTE_Pet, petBookId, 1),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_NewBee), true);

        //添加领取的节点
        SyncExecuteFunction.executeConsumer(player, p -> player.getDb_data().getNewBeeInfoBuilder().setNewBeePet(true));

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_NewBeeName_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
