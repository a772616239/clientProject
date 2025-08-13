package server.handler.arena;

import cfg.PetBaseProperties;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.arena.ArenaManager;
import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.Arena.ArenaPlayerTeamInfo.Builder;
import protocol.Arena.CS_ClaimArenaInfo;
import protocol.Arena.SC_ClaimArenaInfo;
import protocol.ArenaDB.DB_ArenaPlayerBaseInfo;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_JoinArena;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.03.09
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaInfo_VALUE)
public class ClaimArenaInfoHandler extends AbstractBaseHandler<CS_ClaimArenaInfo> {
    @Override
    protected CS_ClaimArenaInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaInfo req, int i) {
        SC_ClaimArenaInfo.Builder resultBuilder = SC_ClaimArenaInfo.newBuilder();

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (entity == null || player == null
                || PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimArenaInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDbBuilder().clearLastClaimRankingTime();
        });

        GS_CS_JoinArena.Builder builder = GS_CS_JoinArena.newBuilder();
        builder.setNeedRefreshOpponent(entity.getDbBuilder().getOpponentCount() <= 0);
        List<ArenaPlayerTeamInfo> teamInfos = buildPlayerArenaDefinedTeams(playerIdx, entity.getDbBuilder().getDan());
        if (CollectionUtils.isNotEmpty(teamInfos)) {
            builder.addAllDefinedTeams(teamInfos);
        } else {
            LogUtil.error("ClaimArenaInfoHandler, playerIdx:" + playerIdx + ", defined teams is empty");
        }
        builder.setBaseInfo(buildBaseInfo(player, ArenaUtil.calculateTotalAbility(teamInfos)));

        if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_JoinArena_VALUE, builder, false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_CanNotFindServer));
            gsChn.send(MsgIdEnum.SC_ClaimArenaInfo_VALUE, resultBuilder);
        }
    }

    private DB_ArenaPlayerBaseInfo buildBaseInfo(playerEntity entity, long ability) {
        if (entity == null) {
            return null;
        }

        DB_ArenaPlayerBaseInfo.Builder baseBuilder = DB_ArenaPlayerBaseInfo.newBuilder();
        baseBuilder.setPlayerIdx(entity.getIdx());
        baseBuilder.setAvatar(entity.getAvatar());
        baseBuilder.setName(entity.getName());
        baseBuilder.setLevel(entity.getLevel());
        baseBuilder.setServerIndex(ServerConfig.getInstance().getServer());
        baseBuilder.setFightAbility(ability);
        baseBuilder.setVipLv(entity.getVip());
        baseBuilder.setShowPetId(getDisPetId(entity.getIdx()));
        baseBuilder.setAvatarBorder(entity.getDb_data().getCurAvatarBorder());
        if (baseBuilder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            baseBuilder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(entity.getIdx()));
        }
        baseBuilder.setTitleId(entity.getTitleId());
        return baseBuilder.build();
    }

    /**
     * 构建玩家防御小队信息
     *
     * @return
     */
    private List<ArenaPlayerTeamInfo> buildPlayerArenaDefinedTeams(String playerIdx, int dan) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return null;
        }

        List<Team> teams = entity.getTeamsByTeamNum(ArenaUtil.getDefinedTeamNum(dan));
        if (GameUtil.collectionIsEmpty(teams)) {
            LogUtil.error("playerIdx:" + playerIdx + ", have no arena team");
            return null;
        }

        List<ArenaPlayerTeamInfo> result = new ArrayList<>();
        for (Team team : teams) {
            if (TeamsUtil.isArenaAttack(team.getTeamNum())) {
                continue;
            }
            Builder builder = ArenaPlayerTeamInfo.newBuilder();
            builder.setTeanNum(team.getTeamNum());
            List<BattlePetData> battlePetData = petCache.getInstance().getPetBattleData(playerIdx,
                    new ArrayList<>(team.getLinkPetMap().values()), BattleSubTypeEnum.BSTE_Arena);
            if (!GameUtil.collectionIsEmpty(battlePetData)) {
                builder.addAllPets(battlePetData);
            }
            builder.addAllSkills(team.getLinkSkillMap().values());

            result.add(builder.build());
        }
        return result;
    }

    public static final int DEFAULT_DIS_PET_ID = 1001;

    public int getDisPetId(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return DEFAULT_DIS_PET_ID;
        }

        int petId = PetBaseProperties.getPetIdByUnlockHeadId(entity.getAvatar());
        if (petId == -1) {
            return DEFAULT_DIS_PET_ID;
        }
        return petId;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaInfo_VALUE, SC_ClaimArenaInfo.newBuilder().setRetCode(retCode));
    }
}
