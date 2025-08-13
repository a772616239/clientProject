package server.handler.recentPassed;

import cfg.EndlessSpireConfig;
import cfg.MainLineNode;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.recentpassed.RecentPassedUtil;
import model.recentpassed.dbCache.recentpassedCache;
import model.recentpassed.entity.recentpassedEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;
import protocol.RecentPassedOuterClass.CS_ClaimRecentPassed;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RecentPassedOuterClass.SC_ClaimRecentPassed;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimRecentPassed_VALUE)
public class ClaimRecentPassedHandler extends AbstractBaseHandler<CS_ClaimRecentPassed> {
    @Override
    protected CS_ClaimRecentPassed parse(byte[] bytes) throws Exception {
        return CS_ClaimRecentPassed.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRecentPassed req, int i) {
        SC_ClaimRecentPassed.Builder resultBuilder = SC_ClaimRecentPassed.newBuilder();
        if (!checkParam(req)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimRecentPassed_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        recentpassedEntity entity = recentpassedCache.getInstance().getEntity(req.getFunction(), req.getParams());
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                for (DB_RecentPlayerInfo playerInfo : entity.getDbBuilder().getRecentPlayerInfoList()) {
                    RecentPassed.Builder builder = builderRecentPassed(playerInfo);
                    if (builder != null) {
                        resultBuilder.addRecentPassed(builder);
                    }
                }
            });
        }
        gsChn.send(MsgIdEnum.SC_ClaimRecentPassed_VALUE, resultBuilder);
    }

    private boolean checkParam(CS_ClaimRecentPassed req) {
        if (req == null || !RecentPassedUtil.allowFunction(req.getFunction())) {
            return false;
        }

        if (req.getFunction() == EnumFunction.MainLine) {
            return MainLineNode.getById(req.getParams()) != null;
        } else if (req.getFunction() == EnumFunction.Endless) {
            return EndlessSpireConfig.getBySpirelv(req.getParams()) != null;
        }

        return false;
    }

    public static RecentPassed.Builder builderRecentPassed(DB_RecentPlayerInfo playerPassInfo) {
        if (playerPassInfo == null) {
            return null;
        }

        playerEntity player = playerCache.getByIdx(playerPassInfo.getPlayerIdx());
        if (player == null) {
            return null;
        }

        RecentPassed.Builder builder = RecentPassed.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setPlayerLv(player.getLevel());
        builder.setAvatarId(player.getAvatar());
        builder.setPlayerName(player.getName());
        builder.addAllPets(playerPassInfo.getPetsList());
        builder.addAllSkills(playerPassInfo.getSkillsList());
        builder.setVipLv(playerPassInfo.getVipLv());
        builder.setTotalAbility(playerPassInfo.getTotalAbility());
        builder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        builder.addAllArtifact(playerPassInfo.getArtifactList());
        builder.addAllArtifactAdditionKeys(playerPassInfo.getArtifactAdditionKeysList());
        builder.addAllArtifactAdditionValues(playerPassInfo.getArtifactAdditionValuesList());
        if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        builder.setTitleId(playerPassInfo.getTitleId());
        builder.addAllNewTitleId(playerPassInfo.getNewTitleIdList());
        builder.setCurEquipNewTitleId(playerPassInfo.getCurEquipNewTitleId());
        builder.setShortId(player.getShortid());
        return builder;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


}
