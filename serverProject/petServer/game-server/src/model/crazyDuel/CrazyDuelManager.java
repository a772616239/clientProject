package model.crazyDuel;

import cfg.*;
import common.GameConst;
import common.GlobalData;
import common.load.ServerConfig;
import lombok.Data;
import lombok.Getter;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.crazyDuel.entity.CrazyDuelPlayerDB;
import model.ranking.RankingManager;
import platform.logs.LogService;
import platform.logs.entity.GamePlayLog;
import protocol.CrayzeDuel.SC_UpdateChooseOpponent.Builder;
import protocol.CrazyDuelDB.CrazyDuelSettingDB;

import model.crazyDuel.entity.CrazyDuelTeamData;
import model.crazyDuel.entity.CrazyTeamsDb;
import model.crazyDuel.factory.CrazyDuelFactory;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaUtil;
import model.mainLine.dbCache.mainlineCache;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.*;
import protocol.RetCodeId.RetCodeEnum;
import redis.clients.jedis.Tuple;
import util.ArrayUtil;
import util.EventUtil;
import util.LogUtil;
import util.RandomUtil;

import java.util.Collection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static protocol.MessageId.MsgIdEnum.SC_CrazyDuelReset_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateBattlePlayer_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateChooseOpponent_VALUE;

@Data
public class CrazyDuelManager {

    private static final int maxCanChooseOpponent = 5;

    private List<Tuple> allPlayerAbility = Collections.emptyList();

    @Getter
    private static CrazyDuelManager instance = new CrazyDuelManager();


    public CrazyDuelPlayerDB findPlayerDb(String playerIdx) {
        return cache.findCrazyDuelDB(playerIdx);
    }

    public void createPlayerDb(String playerIdx) {
        cache.saveCrazyDuelDB(new CrazyDuelPlayerDB(playerIdx));
    }


    public boolean init() {
        //openManager必须先执行
        CrazyDuelOpenManager.getInstance().init();
        CrazyDuelRobotManager.getInstance().checkAndInitRobot();

        return true;
    }

    private CrazyDuelCache cache = CrazyDuelCache.getInstance();


    private static Map<String, List<CrazyDuelTeamData>> playerTeamData = new ConcurrentHashMap<>();


    public void updateRankScore(String playerIdx, int score) {
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, Activity.EnumRankingType.ERT_Lt_Duel, score,CrazyDuelManager.getInstance().findPlayerAttackWinRate(playerIdx));
    }


    public CrazyDuelPlayerPageDB buildShowPagePlayer(String playerIdx, long ability) {
        playerEntity playerEntity = playerCache.getByIdx(playerIdx);
        if (playerEntity == null) {
            return null;
        }
        CrazyDuelPlayerPageDB player = new CrazyDuelPlayerPageDB();
        player.setPlayerId(playerEntity.getIdx());
        player.setName(playerEntity.getName());

        player.setAbility(ability);
        if (ability > 0) {
            player.setPublish(true);
        }

        player.setHeadBorderId(playerEntity.getDb_data().getCurAvatarBorder());
        player.setPlayerLevel(playerEntity.getLevel());
        player.setPublishTime(System.currentTimeMillis());
        player.setHonLv(CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx));
        player.setHeadId(playerEntity.getAvatar());
        return player;
    }

    public CrazyDuelSettingDB findPlayerSetting(String playerIdx) {
        return cache.findPlayerSetting(playerIdx);
    }


    public void saveSetting(String playerIdx, List<PrepareWar.PositionPetMap> mapsList) {
        CrazyDuelDB.CrazyDuelSettingDB playerData = cache.findPlayerSetting(playerIdx);
        CrazyDuelSettingDB.Builder builder = playerData.toBuilder();
        builder.clearPetPos().addAllPetPos(mapsList);
        buildBattlePet(playerIdx, builder);
        long teamAbility = fixDefendTeamAbility(builder.getBattleDataList());
        savePlayerSettingDb(builder.build());

        cache.savePlayerAbility(Collections.singletonMap(playerIdx, mainlineCache.getInstance().findMaxPassAbility(playerIdx)));
        CrazyDuelPlayerPageDB pagePlayer = cache.findPagePlayer(playerIdx);
        if (pagePlayer != null) {
            pagePlayer.setAbility(teamAbility);
            cache.saveShowPagePlayer(pagePlayer);
        }
    }

    public void savePlayerSettingDb(CrazyDuelSettingDB playerData) {
        cache.savePlayerSetting(playerData);
    }


    public Collection<CrayzeDuel.CrazyDuelBuffSetting> findBuffSetting(String playerIdx) {
        CrazyDuelSettingDB playerData = cache.findPlayerSetting(playerIdx);
        if (playerData == null) {
            return Collections.emptyList();
        }

        return playerData.getBuffSettingMap().values();
    }


    public boolean isPlayerPublish(String player) {
        CrazyDuelSettingDB playerData = cache.findPlayerSetting(player);
        if (playerData == null) {
            return false;
        }
        return playerData.getPublish();
    }



    /**
     * 玩家初始化buff设置
     *
     * @return
     */
    public List<CrayzeDuel.CrazyDuelBuffSetting> initOrRefreshBuffSetting(Collection<CrayzeDuel.CrazyDuelBuffSetting> beforeSetting) {
        int floor;
        List<CrayzeDuel.CrazyDuelBuffSetting> result = new ArrayList<>();
        for (CrazyDuelFloorObject cfg : CrazyDuelFloor._ix_floor.values()) {
            floor = cfg.getFloor();
            if (floor <= 0) {
                continue;
            }
            int maxExPosCount = getCfgExBuffNum(cfg, beforeSetting);
            int exBuffRarity = cfg.getExbuffposappeare().length > 0 ? cfg.getExbuffposappeare()[0] : 0;
            CrayzeDuel.CrazyDuelBuffSetting.Builder setting = CrayzeDuel.CrazyDuelBuffSetting.newBuilder();
            setting.setFloor(floor);
            //buff索引[固定buff 固定buff 随机buff
            List<Integer> fixBuff = RandomUtil.batchRandomFromList(ArrayUtil.intArrayToList(cfg.getFixbuffpool()), cfg.getFixbuffnum(), false);
            //初始化buff集合共5位,前几位放入固定buff,最后一位放入固定品质buff,其他的或者没有的buff都用0代替
            for (int i = 0; i < 5; i++) {
                if (i < 2) {
                    if (cfg.getFixbuffnum() > i) {
                        setting.addBuff(fixBuff.get(i));
                    } else {
                        setting.addBuff(0);
                    }
                    setting.addExBuffPos(0);
                   /* setting.addExBuffPos(0);
                    setting.addBuffPos(CrayzeDuel.BuffPos.newBuilder().build());*/
                } else if (i == 2 || i == 3) {
                    if (maxExPosCount - i + 2 > 0) {
                        CrayzeDuel.BuffPos.Builder buffPos = CrayzeDuel.BuffPos.newBuilder();
                        setting.addBuff(0);
                        buffPos.setBuffPos(i);
                        buffPos.addAllBuffPool(randomBuffPoolByCfg(exBuffRarity, cfg.getRandombuff()));
                        setting.addBuffPos(buffPos);
                        setting.addExBuffPos(exBuffRarity);
                    } else {
                        //0表示占位
                        setting.addBuff(0);
                        setting.addExBuffPos(0);
                        setting.addBuffPos(CrayzeDuel.BuffPos.getDefaultInstance());
                    }

                } else if (cfg.getLastbuffposrarity() > 0) {
                    setting.addBuff(0);
                    CrayzeDuel.BuffPos.Builder buffPos = CrayzeDuel.BuffPos.newBuilder();
                    buffPos.setBuffPos(i);
                    buffPos.addAllBuffPool(randomBuffPoolByCfg(cfg.getLastbuffposrarity(), cfg.getRandombuff()));
                    setting.addExBuffPos(cfg.getLastbuffposrarity());
                    setting.addBuffPos(buffPos);
                } else {
                    //0表示占位
                    setting.addBuff(0);
                    setting.addExBuffPos(0);
                    setting.addBuffPos(CrayzeDuel.BuffPos.getDefaultInstance());
                }
            }

            result.add(setting.build());

        }
        return result;
    }


    private int getCfgExBuffNum(CrazyDuelFloorObject cfg, Collection<CrayzeDuel.CrazyDuelBuffSetting> beforeSetting) {
        if (cfg == null || CollectionUtils.isEmpty(beforeSetting)) {
            return 0;
        }
        Optional<CrayzeDuel.CrazyDuelBuffSetting> first = beforeSetting.stream().filter(e -> e.getFloor() == cfg.getFloor()).findFirst();
        if (!first.isPresent()) {
            return 0;
        }
        int max = cfg.getExbuffposappeare().length > 0 ? cfg.getExbuffposappeare()[2] : 0;
        if (max <= 0) {
            return 0;
        }
        int before = Math.max(0, (int) (first.get().getBuffList().stream().filter(e -> e > 0).count() - 1));
        if (before >= max) {
            return max;
        }
        boolean addNum = RandomUtils.nextInt(1000) < cfg.getExbuffposappeare()[1];
        if (addNum) {
            return before + 1;
        }
        return before;
    }


    public List<Integer> randomBuffPoolByCfg(int rarity, int[][] buffPool) {
        for (int[] data : buffPool) {
            if (data.length > 0 && data[0] == rarity) {
                List<Integer> buffs = ArrayUtil.intArrayToList(data);
                buffs.remove(0);
                return RandomUtil.batchRandomFromList(buffs, 3, false);
            }
        }
        LogUtil.error("Crazy duel buff config error,config ex rarity:{} ,but pool not:{} contains", rarity, buffPool);
        return Collections.emptyList();

    }


    private int randomExBuffByRarity(int rarity, int[][] buffPool) {
        for (int[] data : buffPool) {
            if (data.length > 0 && data[0] == rarity) {
                List<Integer> buffs = ArrayUtil.intArrayToList(data);
                buffs.remove(0);
                return RandomUtil.randomOneFromList(buffs);
            }
        }
        LogUtil.error("Crazy duel buff config error,config ex rarity:{} ,but pool not:{} contains", rarity, buffPool);
        return 0;
    }


    public CrazyDuelSettingDB findOrInitPlayerData(String playerIdx) {
        CrazyDuelPlayerDB playerDb = findPlayerDb(playerIdx);
        boolean needBuffWeeklyReset =false;

        CrazyDuelSettingDB playerSetting = cache.findPlayerSetting(playerIdx);

        if (playerDb == null) {
            needBuffWeeklyReset = true;
            createPlayerDb(playerIdx);
           // initPlayerScore(playerIdx);
            CrazyDuelPlayerPageDB pageDB = buildShowPagePlayer(playerIdx, getAbilityFromSetting(playerSetting));
            cache.saveShowPagePlayer(pageDB);
            cache.savePlayerFromSvrIndex(playerIdx, ServerConfig.getInstance().getServer());
        }

        if (playerSetting == null) {
            playerSetting = CrazyDuelFactory.creteCrazyDuelPlayerDB(playerIdx);
            savePlayerSettingDb(playerSetting);
        } else if (needBuffWeeklyReset) {
            resetPlayerBuffSetting(playerSetting);
        }

        return playerSetting;
    }

    private long getAbilityFromSetting(CrazyDuelSettingDB playerSetting) {
        if (playerSetting==null){
            return 0L;
        }
        return fixDefendTeamAbility(playerSetting.getBattleDataList());
    }

    private void resetPlayerBuffSetting(CrazyDuelSettingDB playerSetting) {
        List<CrayzeDuel.CrazyDuelBuffSetting> buffSettings = CrazyDuelManager.getInstance().initOrRefreshBuffSetting(null);
        CrazyDuelSettingDB.Builder builder = playerSetting.toBuilder().clearBuffSetting();
        for (CrayzeDuel.CrazyDuelBuffSetting buffSetting : buffSettings) {
            builder.putBuffSetting(buffSetting.getFloor(), buffSetting);
        }
        savePlayerSettingDb(builder.build());
    }


    public List<CrazyTeamsDb> findPlayerTeams(String playerIdx) {
        return cache.loadTeamsDb(playerIdx);
    }

    private void buildBattlePet(String playerIdx, CrazyDuelSettingDB.Builder playerData) {
        List<String> petIds = playerData.getPetPosList().stream().map(PrepareWar.PositionPetMap::getPetIdx).collect(Collectors.toList());

        List<PetMessage.Pet> petByIdList = petCache.getInstance().getPetByIdList(playerIdx, petIds);
        playerData.clearBattleData().addAllBattleData(petCache.getInstance().buildPetBattleData(playerIdx, petByIdList,
                Battle.BattleSubTypeEnum.BSTE_CrazyDuel, true));

    }


    public long fixDefendTeamAbility(List<Battle.BattlePetData> battleData) {
        // return additionTeamAbility(floor, ability);
        return battleData.stream().mapToLong(Battle.BattlePetData::getAbility).sum();
    }


    public RetCodeId.RetCodeEnum checkCanBattle(String playerIdx, String battlePlayerIdx, int battleFloor) {
        CrazyDuelPlayerDB playerDb = CrazyDuelManager.getInstance().findPlayerDb(playerIdx);
        if (playerDb == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        if (playerDb.getDefeatPlayer().contains(battlePlayerIdx)) {
            return RetCodeId.RetCodeEnum.RCE_CrazyDuel_PlayerAlreadyDefeat;
        }

        Integer floor = playerDb.getBattingData().get(battlePlayerIdx);

        if (floor == null && playerDb.getBattingData().size() + playerDb.getDefeatPlayer().size() >= maxCanChooseOpponent) {
            return RetCodeId.RetCodeEnum.RCE_CrazyDuel_CantBattleMorePlayer;
        }

        int alreadyBattleFloor = floor == null ? 0 : floor;

        if (battleFloor != alreadyBattleFloor + 1) {
            return RetCodeId.RetCodeEnum.RCE_CrazyDuel_FloorError;
        }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    public void updatePlayerBattleFloor(boolean win, String playerIdx, String battlePlayer,
                                        int battleFloor, boolean defeat) {
        CrazyDuelPlayerDB playerDb = findPlayerDb(playerIdx);
        useAndSendUpdateCanDuelCount(playerIdx);
        boolean newBattlePlayer = !playerDb.getBattingData().containsKey(battlePlayer);
        if (newBattlePlayer) {
            playerDb.setTotalNum(playerDb.getTotalNum() + 1);
        }
        if (win) {
            if (defeat) {
                playerDb.getDefeatPlayer().add(battlePlayer);
                sendFinishChallenge(playerIdx, battlePlayer);
                playerDb.setWinNum(playerDb.getWinNum() + 1);
                CrossArenaHonorManager.getInstance().honorVueFirst(playerIdx, CrossArenaUtil.HR_FIRST_FKDJ);
            }
            playerDb.getBattingData().put(battlePlayer, battleFloor);
            if (battleFloor == 5) {
                CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_FKDJ_5PASS, 1);
            }
            //GRADE_CrazyDuel_Join 被策划改为胜利次数
            EventUtil.triggerUpdateCrossArenaWeeklyTask(playerIdx, CrossArena.CrossArenaGradeType.GRADE_CrazyDuel_Join, 1);
            CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_FKDJ_JION, 1);
            CrossArenaManager.getInstance().savePlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.FKDJ_LAYERMAX, battleFloor, CrossArenaUtil.DbChangeRepMax);
        } else {
            if (newBattlePlayer) {
                playerDb.getBattingData().put(battlePlayer, 0);
            }
        }
        cache.saveCrazyDuelDB(playerDb);
        CrazyDuelDataUpdateManager.getInstance().updateDefendPlayerData(battlePlayer, newBattlePlayer, defeat);

        if (newBattlePlayer){
            sendChoosePlayerUpdate(playerDb);
        }
    }

    private void sendChoosePlayerUpdate(CrazyDuelPlayerDB playerDb) {
        Builder msg = CrayzeDuel.SC_UpdateChooseOpponent.newBuilder().addAllPlayerIdx(playerDb.getChoosePlayers());
        GlobalData.getInstance().sendMsg(playerDb.getPlayerIdx(), SC_UpdateChooseOpponent_VALUE, msg);
    }

    private void useAndSendUpdateCanDuelCount(String playerIdx) {
        // cache.incrPlayTimes(playerIdx);
             LogService.getInstance().submit(new GamePlayLog(playerIdx, Common.EnumFunction.LtCrazyDuel));
       /* CrayzeDuel.SC_UpdatePlayerCanDuelCount.Builder msg = CrayzeDuel.SC_UpdatePlayerCanDuelCount.newBuilder();
        msg.setCanDuelCount(queryPlayerCanBattleTime(playerIdx));
        msg.setNextRefreshTime(CrazyDuelDataUpdateManager.getInstance().getNextRestPlayerPlayCountTime());
        GlobalData.getInstance().sendMsg(playerIdx, SC_UpdatePlayerCanDuelCount_VALUE, msg);
*/
    }

    private void sendFinishChallenge(String playerIdx, String battlePlayer) {
        CrayzeDuel.SC_UpdateBattlePlayer.Builder msg = CrayzeDuel.SC_UpdateBattlePlayer.newBuilder().setPlayerIdx(battlePlayer).setFinishChallenge(true);
        GlobalData.getInstance().sendMsg(playerIdx, SC_UpdateBattlePlayer_VALUE, msg);
    }

    public boolean finishBattle(int battleFloor) {
        return battleFloor >= CrazyDuelFloor.getInstance().getMaxFloor();
    }


    public List<CrazyDuelPlayerPageDB> claimLobbyTeam(String playerIdx) {
        CrazyDuelPlayerDB playerDb = findPlayerDb(playerIdx);
        if (playerDb == null) {
            return Collections.emptyList();
        }
        List<String> lastOpponentIds = playerDb.getLastOpponentIds();

        if (!CollectionUtils.isEmpty(lastOpponentIds)) {
            return findPagePlayersByIds(lastOpponentIds);
        }

        List<String> alreadySeePlayers = playerDb.getAlreadySeePlayers();

        long maxPassAbility = mainlineCache.getInstance().findMaxPassAbility(playerIdx);

        List<String> nextOpponents = new ArrayList<>();
        Map<String, Integer> scoreAddition = playerDb.getScoreAddition();
        for (int[] ints : CrazyDuelCfg.getById(GameConst.CONFIG_ID).getMatchscope()) {
            Tuple matchPlayer = findMatchPlayer(playerIdx, ints, maxPassAbility);
            if (matchPlayer == null) {
                matchPlayer = randomOneMatchPlayer(nextOpponents);
            }
            if (matchPlayer != null) {
                nextOpponents.add(matchPlayer.getElement());
                scoreAddition.put(matchPlayer.getElement(), ints[2]);
            }
        }

        alreadySeePlayers.addAll(nextOpponents);

        playerDb.getAlreadySeePlayers().addAll(nextOpponents);
        playerDb.setLastOpponentIds(nextOpponents);
        cache.saveCrazyDuelDB(playerDb);
        return findPagePlayersByIds(nextOpponents);

    }


    private Tuple randomOneMatchPlayer(List<String> nextOpponents) {
        if (nextOpponents.size() >= allPlayerAbility.size() + 1) {
            return null;
        }
        while (true) {
            Tuple tuple = allPlayerAbility.get(ThreadLocalRandom.current().nextInt(allPlayerAbility.size()));
            if (!nextOpponents.contains(tuple.getElement())) {
                return tuple;
            }
        }
    }

    private Tuple findMatchPlayer(String playerIdx, int[] ints, long playerAbility) {
        double floor = playerAbility / 1000.0 * ints[0];
        double ceil = playerAbility / 1000.0 * ints[1];
        List<Tuple> collect = allPlayerAbility.stream().filter(e -> e.getScore() >= floor && e.getScore() < ceil && !e.getElement().equals(playerIdx)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)){
            return null;
        }
        return collect.get(RandomUtils.nextInt(collect.size()));
    }

    private List<CrazyDuelPlayerPageDB> findPagePlayersByIds(List<String> playerIdx) {
        return cache.findPagePlayers(playerIdx);
    }

    public void reloadPlayerAbility() {
        this.allPlayerAbility = new ArrayList<>(CrazyDuelCache.getInstance().findAllPlayerAbility());
    }


    public CrazyDuelPlayerPageDB findPagePlayerById(String playerIdx) {
        return cache.findPagePlayer(playerIdx);
    }


    public RetCodeId.RetCodeEnum updateSettingBuff(String playerIdx, CrayzeDuel.CS_UpdateCrazyDuelTeamBuff req) {
        CrazyDuelSettingDB playerData = cache.findPlayerSetting(playerIdx);
        if (playerData==null){
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }

        CrayzeDuel.CrazyDuelBuffSetting setting = playerData.getBuffSettingMap().get(req.getFloor());

        if (setting == null || req.getBuffCount() != setting.getBuffCount() || !checkBuff(setting, req.getBuffList())) {
            return RetCodeId.RetCodeEnum.RCE_ErrorParam;
        }

        CrayzeDuel.CrazyDuelBuffSetting.Builder newSetting = setting.toBuilder().clearBuff().addAllBuff(req.getBuffList());

        CrazyDuelSettingDB.Builder builder = playerData.toBuilder().putBuffSetting(req.getFloor(), newSetting.build());


        savePlayerSettingDb(builder.build());
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    private boolean checkBuff(CrayzeDuel.CrazyDuelBuffSetting setting, List<Integer> buffList) {
        for (Integer buffId : buffList) {
            if (buffId==0){
                continue;
            }
            if (!setting.getBuffList().contains(buffId)&& setting.getBuffPosList().stream().noneMatch(e->e.getBuffPoolList().contains(buffId))){
                return false;
            }
        }

        return true;
    }



    public List<CrayzeDuel.CrazyBattleRecord> findPlayerRecord(String playerIdx) {
        return cache.findPlayerRecord(playerIdx);
    }

    public void saveBattleRecord(String playerIdx, CrayzeDuel.CrazyBattleRecord playerRecord) {
        cache.saveBattleRecord(playerIdx, playerRecord);
    }

    public void settleReward(Set<String> crazyDuelDbPlayerIds) {
        for (String playerIdx : crazyDuelDbPlayerIds) {
            int score = findPlayerScore(playerIdx);
            if (score <= 0) {
                continue;
            }
            CrazyDuelReward._ix_id.values().stream().filter(cfg ->
                    cfg.getLowerlimit() <= score && cfg.getUpperlimit() > score).findFirst().ifPresent(
                    e -> doWeeklySettleReward(playerIdx, e, score)
            );

        }
    }

    private void doWeeklySettleReward(String playerIdx, CrazyDuelRewardObject cfg, int score) {
        EventUtil.triggerAddMailEvent(playerIdx,
                MailTemplateUsed.getById(GameConst.CONFIG_ID).getCaryzdule(),
                RewardUtil.parseRewardIntArrayToRewardList(cfg.getReward()),
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrazyDuel, "疯狂对决周结算"), score + "");
    }

    public int findPlayerScore(String playerIdx) {
        return cache.findPlayerScore(playerIdx);
    }

    public void initPlayerScore(String playerIdx) {
        cache.savePlayerScore(playerIdx, CrazyDuelCfg.getById(GameConst.CONFIG_ID).getPlayerinitscore());
    }

    public void updateWeeklyData() {
        Set<String> crazyDuelDbPlayerIds =cache.findCrazyDuelDbPlayerIds();
        Set<String> localPlayers = playerCache.getInstance()._ix_id.keySet();
        crazyDuelDbPlayerIds.retainAll(localPlayers);

        if (CollectionUtils.isEmpty(crazyDuelDbPlayerIds)) {
            return ;
        }
        settleReward(crazyDuelDbPlayerIds);
        //移除玩家积分
        cache.clearAllPlayerScore(crazyDuelDbPlayerIds);
        //删除刷新次数
        cache.clearPlayerRefreshTimes(crazyDuelDbPlayerIds);

        cache.clearAllPlayerDataDb(crazyDuelDbPlayerIds);

        sendPlayerResetFunction(crazyDuelDbPlayerIds);
    }

    private void sendPlayerResetFunction(Set<String> crazyDuelDbPlayerIds) {
        CrayzeDuel.SC_CrazyDuelReset.Builder msg = CrayzeDuel.SC_CrazyDuelReset.newBuilder();
        crazyDuelDbPlayerIds.forEach(player -> GlobalData.getInstance().sendMsg(player, SC_CrazyDuelReset_VALUE, msg));
    }

    public List<CrayzeDuel.CrazyDuelBuffSetting> refreshPlayerSetting(String playerIdx) {
        Collection<CrayzeDuel.CrazyDuelBuffSetting> buffSetting = findBuffSetting(playerIdx);
        return initOrRefreshBuffSetting(buffSetting);
    }

    public int findPlayerAttackWinRate(String playerIdx) {
        CrazyDuelPlayerDB crazyDuelDB = cache.findCrazyDuelDB(playerIdx);
        if (crazyDuelDB == null || crazyDuelDB.getTotalNum() <= 0) {
            return 0;
        }
        return crazyDuelDB.getWinNum() * GameConst.commonMagnification / crazyDuelDB.getTotalNum();
    }

    public int findPlayerDefendWinRate(String playerIdx) {
        CrazyDuelPlayerPageDB db = cache.findPagePlayer(playerIdx);
        if (db == null || db.getDuelCount() <= 0) {
            return 0;
        }
        return Math.min(GameConst.commonMagnification, db.getWinCount() * GameConst.commonMagnification / db.getDuelCount());
    }

    public int findPlayerCurBattleFloor(String playerIdx, String battlePlayer) {
        CrazyDuelPlayerDB dbPlayer = findPlayerDb(playerIdx);
        if (dbPlayer == null) {
            return getMaxFloor();
        }

        Integer floor = dbPlayer.getBattingData().get(battlePlayer);
        return floor == null ? 0 : floor;
    }

    private Integer getMaxFloor() {
        return CrazyDuelFloor._ix_floor.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    public int findPlayerRefreshTime(String playerIdx) {
        String playerRefreshTime = cache.findPlayerRefreshTime(playerIdx);
        if (playerRefreshTime == null) {
            return 0;
        }
        return Integer.parseInt(playerRefreshTime);

    }

    public void incrPlayerRefreshTime(String playerIdx) {
        cache.incrPlayerRefreshTime(playerIdx);
    }

    public int queryBattleScoreAddition(String playerIdx, String battlePlayerIdx) {
        CrazyDuelPlayerDB db = findPlayerDb(playerIdx);
        if (db == null) {
            return 0;
        }
        return db.getScoreAddition().getOrDefault(battlePlayerIdx, 0);
    }
}
