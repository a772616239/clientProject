package model.crazyDuel;

import cfg.CrazyDuelCfg;
import common.GameConst;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.crazyDuel.entity.BattleUpdateFloor;
import model.crazyDuel.entity.BattleUpdatePackage;
import model.crazyDuel.entity.CrazyTeamsDb;
import org.springframework.util.CollectionUtils;
import util.TimeUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Data
public class CrazyDuelDataUpdateManager implements Tickable {
    @Getter
    private static CrazyDuelDataUpdateManager instance = new CrazyDuelDataUpdateManager();

    public boolean init() {
        return GlobalTick.getInstance().addTick(this);
    }

    @Setter
    private long dataVersion;

    private Map<String, BattleUpdatePackage> dateUpdate = new HashMap<>();

    private Set<String> pagePlayerUpdate = Collections.synchronizedSet(new HashSet<>());

    private static final String crayDuelUpdate = GameConst.RedisKey.CrazyDuelLockPrefix + "update";

    private Map<String, CrazyDuelPlayerPageDB> pageUpdate = new HashMap<>();


    /**
     * 完全击败算击败
     *
     * @param playerIdx       被挑战玩家idx
     * @param newBattlePlayer 新挑战
     * @param defeat          是否击败
     */
    public synchronized void updateDefendPlayerData(String playerIdx,
                                                    boolean newBattlePlayer, boolean defeat) {
        BattleUpdatePackage update = dateUpdate.computeIfAbsent(playerIdx, a -> new BattleUpdatePackage(playerIdx));
        if (newBattlePlayer) {
            update.setWinIncr(update.getWinIncr() + 1);
        } else if (defeat) {
            update.setWinIncr(update.getWinIncr() - 1);
            update.setFailIncr(update.getFailIncr() + 1);
        }
    }

    public void addUpdateRecord(String... playerIds) {
        this.pagePlayerUpdate.addAll(Arrays.asList(playerIds));
    }

    private long nextTick;

    private long interval = TimeUtil.MS_IN_A_MIN;

    @Getter
    private long nextRestPlayerPlayCountTime;

    private Set<String> addPlayers = Collections.synchronizedSet(new HashSet<>());


    private CrazyDuelCache cache = CrazyDuelCache.getInstance();

    /**
     * 添加新增玩家
     */
    public void addAddPlayer(String playerIdx) {
        addPlayers.add(playerIdx);
    }

    private List<Long> dailyPlayResetTime = new ArrayList<>();

    @Override
    public void onTick() {
        long now = GlobalTick.getInstance().getCurrentTime();

        if (now < nextTick) {
            return;
        }

        uploadLocalData();

        updateLocalData();

        nextTick = GlobalTick.getInstance().getCurrentTime() + interval;

    }

    /**
     * 上传本地更新
     */
    private void updateLocalData() {

        CrazyDuelManager.getInstance().reloadPlayerAbility();
    }


    private void uploadLocalData() {
        if (CollectionUtils.isEmpty(dateUpdate) && CollectionUtils.isEmpty(addPlayers)) {
            return;
        }

        JedisUtil.syncExecBooleanSupplier(crayDuelUpdate, () -> {
            Set<String> versionUpdatePlayers = new HashSet<>();
            if (!CollectionUtils.isEmpty(dateUpdate)) {
                handlerDataUpdate(versionUpdatePlayers);
            }

            if (!CollectionUtils.isEmpty(addPlayers)) {
                Set<String> temPlayer = addPlayers;
                addPlayers = Collections.synchronizedSet(new HashSet<>());
                versionUpdatePlayers.addAll(temPlayer);
            }
            if (CollectionUtils.isEmpty(pagePlayerUpdate)) {
                versionUpdatePlayers.addAll(pagePlayerUpdate);
            }
            long version = CrazyDuelCache.getInstance().incrVersion();
            CrazyDuelCache.getInstance().recordVersionUpdate(version, versionUpdatePlayers);
            return true;
        });

    }

    private void handlerDataUpdate(Set<String> versionUpdatePlayers) {
        int failIncr;
        int winIncr;
        Map<String, BattleUpdatePackage> dateUpdateMap = dateUpdate;
        dateUpdate = new ConcurrentHashMap<>();

        for (BattleUpdatePackage entity : dateUpdateMap.values()) {
            for (BattleUpdateFloor value : entity.getUpdateFloorMap().values()) {

                failIncr = value.getFailIncr();
                winIncr = value.getWinIncr();

                updateFloorData(failIncr, winIncr, entity, value);

            }

            updatePageData(entity);


            versionUpdatePlayers.add(entity.getPlayerIdx());
        }
    }


    private void updatePageData(BattleUpdatePackage entity) {
        CrazyDuelPlayerPageDB pagePlayer = ModifyPagePlayerData(entity);
        if (pagePlayer == null) {
            return;
        }

        CrazyDuelCache.getInstance().saveShowPagePlayer(pagePlayer);
    }

    private CrazyDuelPlayerPageDB ModifyPagePlayerData(BattleUpdatePackage entity) {
        CrazyDuelPlayerPageDB pagePlayer = CrazyDuelCache.getInstance().findPagePlayer(entity.getPlayerIdx());
        if (pagePlayer == null) {
            return null;
        }
        int winCount = pagePlayer.getWinCount() + entity.getWinIncr();
        int total = pagePlayer.getDuelCount() + entity.getFailIncr() + entity.getWinIncr();
        pagePlayer.setWinCount(winCount);
        pagePlayer.setDuelCount(total);
        pagePlayer.setSuccessRate(1 - clulateSuccessRate(winCount, total));
        return pagePlayer;
    }


    private void updateFloorData(int failIncr, int winIncr, BattleUpdatePackage entity, BattleUpdateFloor value) {
        CrazyTeamsDb crazyTeamsDb = CrazyDuelCache.getInstance().loadTeamsDbByFloor(entity.getPlayerIdx(), value.getFloor());

        int winCount = crazyTeamsDb.getWinCount() + winIncr;

        int failCount = crazyTeamsDb.getFailCount() + failIncr;

        int total = winCount + failCount + crazyTeamsDb.getDuelCount();

        crazyTeamsDb.setDuelCount(total);

        crazyTeamsDb.setWinCount(winCount);

        crazyTeamsDb.setFailCount(failCount);

        crazyTeamsDb.setSuccessRate(clulateSuccessRate(winCount, total));


        CrazyDuelCache.getInstance().saveTeamsDb(entity.getPlayerIdx(), crazyTeamsDb);
    }


    private double clulateSuccessRate(int winCount, int total) {
        return new BigDecimal((float) winCount / total)
                .setScale(4, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

}
