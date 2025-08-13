package server.handler.player;

import cfg.ArtifactConfig;
import cfg.ArtifactConfigObject;
import cfg.ArtifactEnhancePointConfig;
import cfg.ArtifactEnhancePointConfigObject;
import cfg.ArtifactStarConfig;
import cfg.ArtifactStarConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.artifact.ArtifactLvUpLog;
import platform.logs.entity.artifact.ArtifactStartUpLog;
import platform.logs.statistics.ArtifactStatistics;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo;
import protocol.PlayerInfo.Artifact;
import protocol.PlayerInfo.ArtifactEnhancePoint;
import protocol.PlayerInfo.CS_ArtifactUp;
import protocol.PlayerInfo.SC_ArtifactUp;
import protocol.PlayerInfo.SC_PlayerSkillLvUp;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ArtifactUp_VALUE)
public class ArtifactUpHandler extends AbstractBaseHandler<CS_ArtifactUp> {

    @Override
    protected CS_ArtifactUp parse(byte[] bytes) throws Exception {
        return CS_ArtifactUp.parseFrom(bytes);
    }

    private static final int upLv = 2;

    private static final int upStar = 1;

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ArtifactUp req, int codeNum) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PlayerSkillLvUp.Builder resultBuilder = SC_PlayerSkillLvUp.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PlayerSkillLvUp_VALUE, resultBuilder);
            return;
        }
        ArtifactConfigObject artifactConfig = ArtifactConfig.getByKey(req.getArtifactId());
        if (artifactConfig == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_PlayerSkillLvUp_VALUE, resultBuilder);
            return;
        }

        LogUtil.info(" player:{} ArtifactUp,req:{}", playerId, req);

        RetCodeEnum codeEnum;
        //强化
        if (req.getType() == upLv) {
            codeEnum = artifactLvUp(playerId, player, artifactConfig, req.getLvUpMax());
        } else if (req.getType() == upStar) {
            codeEnum = playerSkillUpStar(playerId, player, artifactConfig);
        } else {
            codeEnum = RetCodeEnum.RCE_ErrorParam;
        }
        LogUtil.info(" player:{} ArtifactUp,result:{} ", playerId, codeEnum);

        resultBuilder.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_ArtifactUp_VALUE, resultBuilder);

        if (codeEnum == RetCodeEnum.RCE_Success) {
            petCache.getInstance().refreshAndSendTotalAbility(playerId);
        }

    }

    /**
     * 神器升星
     *
     * @param playerId
     * @param player
     * @param artifactConfig
     * @return
     */
    private RetCodeEnum playerSkillUpStar(String playerId, playerEntity player, ArtifactConfigObject artifactConfig) {
        int artifactId = artifactConfig.getKey();
        int skillId = artifactConfig.getPlayerskillid();
        int lastSkillLv = player.getSkillLv(skillId);

        ArtifactStarConfigObject starUpConfig = ArtifactStarConfig.getByArtifactIdAndStar(artifactId, lastSkillLv);
        if (starUpConfig == null) {
            return RetCodeEnum.RSE_ConfigNotExist;
        }
        Consume consume = ConsumeUtil.parseConsume(starUpConfig.getUpconsume());
        if (consume == null || consume.getCount() <= 0) {
            return RetCodeEnum.RCE_Player_SkillMaxStarError;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PlayerSkillStarUp);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }
        Map<Integer, Integer> lastAddition = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
        SyncExecuteFunction.executeConsumer(player, e -> {
            Artifact.Builder artifactBuilder = pullPlayerArtifactBuilder(player, artifactId, skillId);

            artifactBuilder.getPlayerSkillBuilder().setSkillLv(artifactBuilder.getPlayerSkill().getSkillLv() + 1);

            player.getDb_data().addArtifact(artifactBuilder);
        });
        int nowSkillLv = player.getSkillLv(skillId);
        // 埋点日志
        LogService.getInstance().submit(new ArtifactStartUpLog(playerId, artifactId, consume, lastSkillLv, nowSkillLv));
        if (lastSkillLv == 0 && nowSkillLv == 1) {
            //目标：解锁神器(额外条件:神器Id)
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TEE_Artifact_Unlock, 1, artifactConfig.getKey());
        }
        //目标:x个神器星级达到x(额外条件:星级),神器星级就是技能等级
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TEE_Artifact_StarReach, 1, nowSkillLv);
        EventUtil.triggerCollectArtifactExp(playerId,artifactId,lastSkillLv,nowSkillLv);
        if (ArrayUtils.isNotEmpty(starUpConfig.getIncreaseproperty())) {
            SyncExecuteFunction.executeConsumer(player, cache -> {
                player.refreshAllPetPropertyAddition(true);
            });
            player.sendGlobalAdditionMsg();
            //触发全体宠物属性刷新
            EventUtil.triggerAllPetAdditionUpdate(playerId, lastAddition, player.getDb_data().getGlobalAddition().getArtifactAdditionMap(), 1);
        }
        player.sendPlayerSkillUpdate(skillId);
        player.sendArtifactUpdate(artifactId);
        if (lastSkillLv == 0) {
            ArtifactStatistics.getInstance().addActive(artifactId);
        }
        ArtifactStatistics.getInstance().addStarLv(artifactId, nowSkillLv - lastSkillLv);
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 根据artifactId拉取一个并移除,没有返回一个新的
     *
     * @param player
     * @param skillId
     * @param artifactId
     * @return
     */
    private Artifact.Builder pullPlayerArtifactBuilder(playerEntity player, int artifactId, int skillId) {
        List<Artifact> artifactList = player.getDb_data().getArtifactList();

        for (int i = 0; i < artifactList.size(); i++) {
            Artifact artifact = artifactList.get(i);
            if (artifact.getArtifactId() == artifactId) {
                player.getDb_data().removeArtifact(i);
                return artifact.toBuilder();
            }

        }

        Artifact.Builder builder = Artifact.newBuilder().setArtifactId(artifactId);
        return builder.setPlayerSkill(PlayerInfo.PlayerSkill.newBuilder().setSkillCfgId(skillId).build());
    }


    /**
     * 神器强化
     *
     * @param playerId
     * @param player
     * @param artifactConfig
     * @param lvUpMax
     * @return
     */
    private RetCodeEnum artifactLvUp(String playerId, playerEntity player, ArtifactConfigObject artifactConfig, boolean lvUpMax) {
        int artifactId = artifactConfig.getKey();
        Map<Integer, Integer> lastAddition = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
        RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(player, e -> {
            RetCodeEnum retCodeEnum = null;
            int enhanceLv = 0;
            Artifact.Builder artifactBuilder = null;
            for (Artifact.Builder artifactData : player.getDb_data().getArtifactBuilderList()) {
                if (artifactData.getArtifactId() == artifactId) {
                    artifactBuilder = artifactData;
                    break;
                }
            }
            if (artifactBuilder == null) {
                artifactBuilder = Artifact.newBuilder().setArtifactId(artifactId);
                e.getDb_data().addArtifact(artifactBuilder);
            }

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PlayerSkillLvUp);
            List<Consume> allConsumes = new ArrayList<>();
            while (true) {
                Consume consume;
                ArtifactEnhancePoint.Builder point = ArtifactConfig.getNextEnhancePoint(artifactBuilder, artifactConfig);
                ArtifactEnhancePointConfigObject upConfig = ArtifactEnhancePointConfig.getByPointAndLv(point.getPointId(), point.getPointLevel());
                if (upConfig == null) {
                    retCodeEnum = RetCodeEnum.RSE_ConfigNotExist;
                    break;
                }
                if (upConfig.getNeedplayerlv() > player.getLevel()) {
                    retCodeEnum = RetCodeEnum.RCE_LvNotEnough;
                    break;
                }
                consume = ConsumeUtil.parseConsume(upConfig.getUpconsume());
                if (consume == null || consume.getCount() == 0) {
                    retCodeEnum = RetCodeEnum.RCE_Player_SkillMaxLvError;
                    break;
                }
                if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
                    retCodeEnum = RetCodeEnum.RCE_MatieralNotEnough;
                    break;
                }
                allConsumes.add(consume);
                point.setPointLevel(point.getPointLevel() + 1);
                enhanceLv++;
                if (!lvUpMax) {
                    break;
                }
            }
            if (enhanceLv <= 0) {
                return retCodeEnum;
            }
            player.sendArtifactUpdate(artifactBuilder.getArtifactId());
            // 埋点日志
            List<Consume> consumes = ConsumeUtil.mergeConsume(allConsumes);
            LogService.getInstance().submit(new ArtifactLvUpLog(playerId, artifactBuilder, consumes, lvUpMax, lastAddition, player.getDb_data().getGlobalAddition().getArtifactAdditionMap()));
            ArtifactStatistics.getInstance().addEnhanceLv(artifactBuilder.getArtifactId(), enhanceLv);

            return RetCodeEnum.RCE_Success;
        });
        if (codeEnum != RetCodeEnum.RCE_Success) {
            return codeEnum;
        }

        player.sendArtifactUpdate(artifactId);
        SyncExecuteFunction.executeConsumer(player, cache -> {
            player.refreshAllPetPropertyAddition(true);
        });
        player.sendGlobalAdditionMsg();
        //触发全体宠物属性刷新
        EventUtil.triggerAllPetAdditionUpdate(playerId, lastAddition, player.getDb_data().getGlobalAddition().getArtifactAdditionMap(), 1);

        //目标:x个神器等级达到x(额外条件:等级),神器等级取强化点的最低等级向下取整+1
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TEE_Artifact_LvReach, 1, player.getArtifactEnhanceLv(artifactId));
        return codeEnum;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_Artifact;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ArtifactUp_VALUE, SC_ArtifactUp.newBuilder().setRetCode(retCode));
    }
}
