package model.cp;

import cfg.CpTeamLvCfg;
import common.GameConst;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.cp.entity.CpTeamMember;
import model.cp.entity.CpTeamPublish;
import model.player.util.PlayerUtil;
import org.springframework.util.CollectionUtils;
import util.LogUtil;
import util.TimeUtil;

@Slf4j
public class CpTeamMatchManger implements Tickable {


    @Getter
    private static CpTeamMatchManger instance = new CpTeamMatchManger();


    private Map<Integer, Map<String, Long>> matchPlayerMap = new HashMap<>();

    private CpTeamCache cache = CpTeamCache.getInstance();

    private static final String lockKey = GameConst.RedisKey.CpTeamPrefix + "lock:" + "matchTeam";
    private Set<String> allPlayerEnterScene = Collections.EMPTY_SET;
    private long interval = TimeUtil.MS_IN_A_S;
    private long nextTick;

    @Override
    public void onTick() {

        if (GlobalTick.getInstance().getCurrentTime() < nextTick) {
            return;
        }

        int fullMemberSize = CpTeamManger.getInstance().getFullMemberSize();
        if (!JedisUtil.lockRedisKey(lockKey, 9000L)) {
            return;
        }
        doAutoMatchPlayer(fullMemberSize);
        JedisUtil.unlockRedisKey(lockKey);
        updateFunctionFreePlayers();
        nextTick = GlobalTick.getInstance().getCurrentTime() + interval;
    }

    private void doAutoMatchPlayer(int fullMemberSize) {
        for (Integer teamLv : CpTeamLvCfg.getInstance().getTeamLvs()) {
            Map<String, Long> players = matchPlayerMap.get(teamLv);
            if (CollectionUtils.isEmpty(players)) {
                continue;
            }
            List<String> teamIds = cache.findOpenTeamInfo(teamLv, players.size());
            if (CollectionUtils.isEmpty(teamIds)) {
                continue;
            }
            for (String teamId : teamIds) {
                JedisUtil.syncExecBooleanSupplier(CpTeamManger.getInstance().getLockTeamRedisKey(Integer.parseInt(teamId)), () -> {
                    CpTeamPublish teamInfo = cache.loadTeamInfo(Integer.parseInt(teamId));
                    if (teamInfo == null) {
                        LogUtil.warn("match team,teamId in open team info ,but can`t find team cache,teamId:{}", teamId);
                        return false;
                    }
                    int canAddMember = fullMemberSize - teamInfo.getMemberSize();
                    for (Map.Entry<String, Long> playerAbility : players.entrySet()) {
                        if (teamInfo.getNeedAbility() > playerAbility.getValue()) {
                            continue;
                        }
                        if(CpTeamManger.getInstance().playerInTeam(playerAbility.getKey())){
                            cancelAddMatchPlayer(playerAbility.getKey());
                            continue;
                        }
                        CpTeamManger.getInstance().addMember(playerAbility.getKey(), teamInfo);
                        cancelAddMatchPlayer(playerAbility.getKey());
                        canAddMember--;
                        if (canAddMember <= 0) {
                            cache.removeOpenTeamInfo(teamLv, teamId);
                            break;
                        }
                    }
                    return true;
                });
            }
        }
    }

    /**
     * 更新大厅空闲玩家
     */
    private void updateFunctionFreePlayers() {
        if (GlobalTick.getInstance().getCurrentTime() < nextUpdatePlayerTime) {
            return;
        }
        findAllPlayerEnterScene();
        nextUpdatePlayerTime += 10 * TimeUtil.MS_IN_A_S;
    }

    private long nextUpdatePlayerTime;

    public boolean init() {
        GlobalTick.getInstance().addTick(this);
        CpTeamLvCfg.getInstance().getTeamLvs().forEach(lv ->
                matchPlayerMap.put(lv, Collections.synchronizedMap(new LinkedHashMap<>())));
        clearAllInCpPlayer();
        return true;
    }

    private void clearAllInCpPlayer() {
        cache.removeAllInCpPlayer();
    }

    public void addMatchPlayer(String playerIdx) {
        int teamLv = CpTeamLvCfg.queryTeamLv(PlayerUtil.queryPlayerLv(playerIdx));
        CpTeamMember cpTeamMember = cache.loadPlayerInfo(playerIdx);
        if (cpTeamMember == null) {
            LogUtil.error("player:{} addMatchPlayer failed, not upload player info", playerIdx);
            return;
        }
        matchPlayerMap.get(teamLv).put(playerIdx, cpTeamMember.getAbility());
    }

    public void cancelAddMatchPlayer(String playerIdx) {
        int teamLv = CpTeamLvCfg.queryTeamLv(PlayerUtil.queryPlayerLv(playerIdx));
        Map<String, Long> map = matchPlayerMap.get(teamLv);
        if (map != null) {
            map.remove(playerIdx);
        }
    }

    public void playerEnterScene(String playerIdx) {
        cache.addPlayerEnterScene(playerIdx);
    }

    public void playerLeaveScene(String playerIdx) {
        cache.removePlayerEnterScene(playerIdx);
    }

    public void findAllPlayerEnterScene() {
        this.allPlayerEnterScene = cache.findAllPlayerEnterScene();
    }

    public List<String> findTeamPlayers(String playerIdx) {
        return allPlayerEnterScene.parallelStream().filter(e -> !e.equals(playerIdx)).limit(20).collect(Collectors.toList());
    }
}
