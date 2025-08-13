/*
package server.handler.player;

import cfg.PlayerSkillConfig;
import cfg.PlayerSkillConfigObject;
import cfg.Playerskill;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_PlayerSkillUp;
import protocol.PlayerInfo.PlayerSkill;
import protocol.PlayerInfo.SC_PlayerSkillLvUp;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_PlayerSkillUp_VALUE)
public class SkillLvUpHandler extends AbstractBaseHandler<CS_PlayerSkillUp> {

    @Override
    protected CS_PlayerSkillUp parse(byte[] bytes) throws Exception {
        return CS_PlayerSkillUp.parseFrom(bytes);
    }

    private static final int upLv = 2;

    private static final int upStar = 1;

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PlayerSkillUp req, int codeNum) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PlayerSkillLvUp.Builder resultBuilder = SC_PlayerSkillLvUp.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PlayerSkillLvUp_VALUE, resultBuilder);
            return;
        }
        int skillId = req.getSkillId();
        PlayerSkill playerSkill = player.getDb_data().getPlayerSkillMap().get(skillId);
        if (playerSkill == null && Playerskill.getById(skillId) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_SkillNotExists));
            gsChn.send(MsgIdEnum.SC_PlayerSkillLvUp_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum codeEnum;
        //强化
        if (req.getType() == upLv) {
            codeEnum = playerSkillLvUp(req, playerId, player, skillId, playerSkill);
        } else if (req.getType() == upStar) {
            codeEnum = playerSkillUpStar(req, playerId, player, skillId, playerSkill);
        } else {
            codeEnum = RetCodeEnum.RCE_ErrorParam;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_PlayerSkillUp_VALUE, resultBuilder);
    }

    private RetCodeEnum playerSkillUpStar(CS_PlayerSkillUp req, String playerId, playerEntity player, int skillId, PlayerSkill playerSkill) {
        playerSkill = playerSkill == null ? PlayerSkill.newBuilder().setStarLv(-1).setSkillId(req.getSkillId()).build() : playerSkill;
        PlayerSkillConfigObject upConfig = PlayerSkillConfig.getBySkillIdTypeAndLv(skillId, req.getType(), playerSkill.getStarLv());
        if (upConfig == null) {
            return RetCodeEnum.RCE_Player_SkillMaxStarError;
        }
        Consume consume = ConsumeUtil.parseConsume(upConfig.getUpconsume());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PlayerSkillStarUp);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }
        PlayerSkill finalPlayerSkill = playerSkill;
        SyncExecuteFunction.executeConsumer(player, e -> {
            PlayerSkill newSkill = finalPlayerSkill.toBuilder().setStarLv(finalPlayerSkill.getStarLv() + 1).build();
            player.getDb_data().putPlayerSkill(finalPlayerSkill.getSkillId(), newSkill);
        });
        if (upConfig.getExtraproperty().length > 0 || upConfig.getIncreaseproperty().length > 0) {
            EventUtil.triggerRefreshAllPetData(playerId, reason);
        }
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum playerSkillLvUp(CS_PlayerSkillUp req, String playerId, playerEntity player, int skillId, PlayerSkill playerSkill) {
        RetCodeEnum retCodeEnum = RetCodeEnum.RCE_Success;
        if (playerSkill == null || playerSkill.getStarLv() <= 0) {
            return RetCodeEnum.RCE_Player_SkillNotActive;
        }
        int skillUpLv = playerSkill.getUpLv();
        Consume consume = null;
        boolean refreshPetData = false;
        while (true) {
            PlayerSkillConfigObject upConfig = PlayerSkillConfig.getBySkillIdTypeAndLv(skillId, req.getType(), skillUpLv);
            if (upConfig == null) {
                retCodeEnum = RetCodeEnum.RCE_Player_SkillMaxLvError;
                break;
            }
            if (!canMergeConsume(upConfig.getUpconsume(), consume)) {
                break;
            }
            consume = mergeConsume(upConfig.getUpconsume(), consume);
            if (!ConsumeManager.getInstance().materialIsEnough(playerId, consume)) {
                retCodeEnum = RetCodeEnum.RCE_MatieralNotEnough;
                break;
            }
            if (upConfig.getExtraproperty().length > 0 || upConfig.getIncreaseproperty().length > 0) {
                refreshPetData = true;
            }
            skillUpLv++;
            if (!req.getLvUpMax()) {
                break;
            }
        }

        if (skillUpLv == playerSkill.getUpLv()) {
            return retCodeEnum;

        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PlayerSkillLvUp);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        int finalUpLv = skillUpLv;
        SyncExecuteFunction.executeConsumer(player, e -> {
            PlayerSkill newSkill = playerSkill.toBuilder().setUpLv(finalUpLv).build();
            player.getDb_data().putPlayerSkill(playerSkill.getSkillId(), newSkill);
        });
        //触发全体宠物属性刷新
        if (refreshPetData) {
            EventUtil.triggerRefreshAllPetData(playerId, reason);
        }
        return retCodeEnum;
    }

    private boolean canMergeConsume(int[] upConsume, Consume consume) {
        if (upConsume.length < 3) {
            return false;
        }
        return consume.getRewardTypeValue() == upConsume[0] && consume.getId() == upConsume[1];
    }

    private Consume mergeConsume(int[] upConsume, Consume consume) {
        if (consume == null) {
            return ConsumeUtil.parseConsume(upConsume);
        }
        return ConsumeUtil.parseConsume(consume.getRewardTypeValue(), consume.getId(), consume.getCount() + upConsume[2]);
    }


}
*/
