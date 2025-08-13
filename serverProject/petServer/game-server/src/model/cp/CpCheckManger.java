package model.cp;

import common.GameConst.RedisKey;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.cp.entity.CpTeamPublish;
import org.springframework.util.CollectionUtils;
import util.TimeUtil;

@Slf4j
public class CpCheckManger implements Tickable {


    @Getter
    private static CpCheckManger instance = new CpCheckManger();

    private CpTeamCache cache = CpTeamCache.getInstance();

    private CpTeamManger cpTeam = CpTeamManger.getInstance();

    private static final String lockKey = RedisKey.CpTeamPrefix + "expireTeamCheckLock";

    private long nextTick;

    @Override
    public void onTick() {
        if (nextTick > GlobalTick.getInstance().getCurrentTime()) {
            return;
        }

        if (!JedisUtil.lockRedisKey(lockKey, 9000L)){
            return;
        }

        List<String> allExpireTeamIds = findAllExpireTeamIds();

        removeExpireTeams(allExpireTeamIds);

        JedisUtil.unlockRedisKey(lockKey);

        nextTick = GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_S;
    }


    private void removeExpireTeams(List<String> allExpireTeamIds) {
        if (CollectionUtils.isEmpty(allExpireTeamIds)) {
            return;
        }
        for (String val : allExpireTeamIds) {
            int teamId = Integer.parseInt(val);
            String teamLockKey = CpTeamManger.getInstance().getLockTeamRedisKey(teamId);
            if (!JedisUtil.lockRedisKey(teamLockKey, 3000L)){
                continue;
            }
            CpTeamPublish team = cpTeam.findTeamByTeamId(teamId);
            if (team == null) {
                CpTeamCache.getInstance().removeTeamExpire(teamId);
                JedisUtil.unlockRedisKey(teamLockKey);
                continue;
            }
            if (team.isActiveCopy()) {
                CpTeamCache.getInstance().removeTeamExpire(teamId);
                JedisUtil.unlockRedisKey(teamLockKey);
                continue;
            }
            log.debug("remove expire cp team teamId:{}", teamId);
            cpTeam.disbandTeam(team);
            JedisUtil.unlockRedisKey(teamLockKey);
        }

    }

    private List<String> findAllExpireTeamIds() {
        long now = GlobalTick.getInstance().getCurrentTime();
        Map<String, String> allTeamExpire = cache.findAllTeamExpire();

        List<String> expireTeamIds = new ArrayList<>();

        for (Map.Entry<String, String> expire : allTeamExpire.entrySet()) {
            if (now > Long.parseLong(expire.getValue())) {
                expireTeamIds.add(expire.getKey());
            }
        }
        return expireTeamIds;
    }

    public boolean init() {
        GlobalTick.getInstance().addTick(this);
        return true;
    }


}
