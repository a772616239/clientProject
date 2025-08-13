package model.cp.factory;

import cfg.CpTeamLvCfg;
import cfg.CpTeamRobotCfg;
import common.GameConst;
import common.JedisUtil;
import common.load.ServerConfig;
import java.util.List;
import model.arena.ArenaManager;
import model.cp.entity.CpTeamMember;
import model.cp.entity.CpTeamPublish;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.PrepareWar;
import server.handler.cp.CpFunctionUtil;
import util.LogUtil;
import util.RandomUtil;

import static model.cp.CpRedisKey.CpTeamId;

public class CpTeamPublishFactory {
    private static final int teamIdStart = 100001;

    public static CpTeamPublish createCpTeamPublish(String playerIdx, String teamName,
                                                    long needAbility, boolean autoJoin) {

        CpTeamMember cpTeamMember = createCpTeamMember(playerIdx);
        if (cpTeamMember == null) {
            return null;
        }
        CpTeamPublish cpTeamPublish = new CpTeamPublish();
        cpTeamPublish.setTeamId(incrAndGetTeamId());
        cpTeamPublish.setTeamName(teamName);
        cpTeamPublish.setNeedAbility(Math.max(1, needAbility));
        cpTeamPublish.setTeamLv(CpTeamLvCfg.queryTeamLv(PlayerUtil.queryPlayerLv(playerIdx)));
        cpTeamPublish.addMember(playerIdx);
        cpTeamPublish.setAutoJoin(autoJoin);
        cpTeamPublish.setLeaderAbility(cpTeamMember.getAbility());
        cpTeamPublish.setLeaderIdx(playerIdx);
        initCpTeamPublishRobotAbility(cpTeamPublish);
        return cpTeamPublish;

    }

    private static void initCpTeamPublishRobotAbility(CpTeamPublish cpTeamPublish) {
        long leaderAbility = cpTeamPublish.getLeaderAbility();
        for (Integer integer : CpTeamRobotCfg._ix_id.keySet()) {
            cpTeamPublish.putAbility(CpFunctionUtil.getRobotId(integer), randomRobotAbility(leaderAbility));
        }

    }

    private static long randomRobotAbility(long leaderAbility) {
        return (long) (leaderAbility * (RandomUtil.randomInScope(900, 1100) / 1000.0));
    }

    public static CpTeamMember createCpTeamMember(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }

        List<Battle.BattlePetData> battlePetData = teamCache.getInstance()
                .buildBattlePetData(playerIdx, PrepareWar.TeamTypeEnum.TTE_LtCP, null);

        if (CollectionUtils.isEmpty(battlePetData)) {
            LogUtil.error("CpTeamPublishFactory player:{} createCpTeamMember error " +
                    ",battlePet is empty by teamType:{}", playerIdx, PrepareWar.TeamTypeEnum.TTE_LtCP);
            return null;

        }
        long ability = battlePetData.stream().mapToLong(Battle.BattlePetData::getAbility).sum();

        CpTeamMember cpTeamMember = new CpTeamMember();
        cpTeamMember.setPlayerIdx(playerIdx);
        cpTeamMember.setPlayerName(player.getName());
        cpTeamMember.setHeader(player.getAvatar());
        cpTeamMember.setPlayerLv(player.getLevel());
        cpTeamMember.setAbility(ability);
        cpTeamMember.setPetData(battlePetData);
        cpTeamMember.setVipLv(player.getVip());
        cpTeamMember.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (cpTeamMember.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            cpTeamMember.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(playerIdx));
        }
        cpTeamMember.setTitleId(PlayerUtil.queryPlayerTitleId(playerIdx));
        cpTeamMember.setCurEquipNewTitleId(player.getCurEquipNewTitleId());
        cpTeamMember.setShortId(player.getShortid());
        cpTeamMember.setServerIndex(ServerConfig.getInstance().getServer());
        cpTeamMember.setSex(player.getSex());
        return cpTeamMember;
    }

    public static int incrAndGetTeamId() {
        long teamId = JedisUtil.jedis.incrBy(CpTeamId, 1);
        return (int) (teamId + teamIdStart);
    }
}
