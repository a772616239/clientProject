package server.http.entity;

import cfg.Item;
import cfg.PetFragmentConfig;
import common.GameConst.RankingName;
import common.load.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import common.tick.GlobalTick;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.pet.dbCache.petCache;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.petfragmentEntity;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.targetsystem.dbCache.targetsystemCache;
import platform.logs.StatisticsLogUtil;
import protocol.Activity.EnumRankingType;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.Patrol.PatrolStatus;
import protocol.PetMessage.PetFragment;
import protocol.PlayerDB;
import protocol.PlayerDB.DB_PlayerData;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020.02.27
 */
@Getter
@Setter
public class PlatformPlayerDetailsInfo {
    private String roleName;
    private String roleId;
    private int shortId;
    private String zone;
    private int serverIndex;
    private int level;
    private int diamond;
    private long gold;
    private int coupon;
    private int vipLv;
    private long monthCardExpireTime;
    private boolean firstRecharge;
    private long totalAbility;
    private int achievementPoint;
    private int endlessSpire;
    private int endlessSpireRanking;
    private int mistTransRanking;
    private int mistLocalRanking;
    private int mistPoint;
    private int mainlinePro;
    private int mainLineRanking;
    private long createRoleTime;
    private long lastLogInTime;
    private long lastLogOutTime;
    private String ip;
    private boolean onlineState;
    private List<PlatformItemInfo> item = new ArrayList<>();
    private BraveChallengeInfo braveChallenge;
    private PatrolInfo patrol;
    private List<PlatformMonthCardInfo> monthCardInfos = new ArrayList<>();


    private List<FragmentInfo> fragment = new ArrayList<>();
//    private List<RuneInfo> rune = new ArrayList<>();

    public PlatformPlayerDetailsInfo(playerEntity player) {
        if (player == null) {
            return;
        }
        String playerIdx = player.getIdx();
        this.zone = ServerConfig.getInstance().getZone();
        this.serverIndex = ServerConfig.getInstance().getServer();
        this.roleId = player.getIdx();
        this.shortId = player.getShortid();
        this.roleName = player.getName();
        this.level = player.getLevel();
        this.diamond = player.getDiamond();
        this.gold = player.getGold();
        this.coupon = player.getCoupon();
        this.vipLv = player.getVip();
        this.firstRecharge = !targetsystemCache.getInstance().firstRechargeNotActive(playerIdx);
        this.totalAbility = petCache.getInstance().totalAbility(playerIdx);
        this.achievementPoint = 0;

        DB_PlayerData.Builder db_data = player.getDb_data();
        if (db_data != null) {
            this.endlessSpire = db_data.getEndlessSpireInfo().getMaxSpireLv();
            for (PlayerDB.DB_MonthCardInfo monthCardInfo : db_data.getRechargeCardsBuilder().getMonthCardListList()) {
                if (monthCardInfo.getRemainDays() > 0) {
                    this.monthCardInfos.add(new PlatformMonthCardInfo(monthCardInfo.getCarId(), TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), monthCardInfo.getRemainDays())));
                }
            }
        }
        this.endlessSpireRanking = RankingManager.getInstance().queryPlayerRanking(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_Spire), playerIdx);

        this.createRoleTime = player.getCreatetime() == null ? 0 : player.getCreatetime().getTime();
        this.lastLogInTime = player.getLogintime() == null ? 0 : player.getLogintime().getTime();
        this.lastLogOutTime = player.getLogouttime() == null ? 0 : player.getLogouttime().getTime();
        this.ip = player.getIp();
        this.onlineState = player.isOnline();

        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine != null && mainLine.getDBBuilder() != null) {
            this.mainlinePro = mainLine.getDBBuilder().getOnHookIncome().getCurOnHookNode();
        }
        this.mainLineRanking = RankingManager.getInstance().queryPlayerRanking(RankingName.RN_MainLinePassed, playerIdx);

        ChallengeProgress challengeProgress = bravechallengeCache.getInstance().getPlayerProgress(playerIdx);
        if (challengeProgress != null) {
            //TODO 难度已经摒弃
            this.braveChallenge = new BraveChallengeInfo(0, challengeProgress.getProgress());
        }

        PatrolStatus patrolStatus = PatrolServiceImpl.getInstance().getStateByPlayerIdx(playerIdx);
        if (patrolStatus != null) {
            this.patrol = new PatrolInfo(patrolStatus.getMapId(), patrolStatus.getLocation().getX(), patrolStatus.getLocation().getY());
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag != null) {
            Map<Integer, Long> itemsMap = itemBag.getDb_data().getItemsMap();
            for (Entry<Integer, Long> entry : itemsMap.entrySet()) {
                this.item.add(new PlatformItemInfo(entry.getKey(), entry.getValue(), playerIdx));
            }
        }

        petfragmentEntity fragmentByPlayer = PetFragmentServiceImpl.getInstance().getFragmentByPlayer(playerIdx);
        if (fragmentByPlayer != null) {
            List<PetFragment> fragmentList = fragmentByPlayer.getFragmentList();
            if (fragmentList != null) {
                for (PetFragment petFragment : fragmentList) {
                    this.fragment.add(new FragmentInfo(petFragment.getCfgId(), petFragment.getNumber()));
                }
            }
        }

//        PetRuneCacheTemp runeByPlayer = PetRuneServiceImpl.getInstance().getRuneByPlayer(playerIdx);
//        if (runeByPlayer != null) {
//            List<Rune> runeList = runeByPlayer.getRuneList();
//            if (runeList != null) {
//                Map<String, RuneInfo> runeInfoMap = new HashMap<>();
//                for (Rune rune : runeList) {
//                    String key = rune.getRuneBookId() + "_" + rune.getRuneLvl();
//                    if (runeInfoMap.containsKey(key)) {
//                        runeInfoMap.get(key).addCount();
//                    } else {
//                        runeInfoMap.put(key, new RuneInfo(rune.getRuneBookId(), 1, rune.getRuneLvl()));
//                    }
//                }
//                this.rune.addAll(runeInfoMap.values());
//            }
//        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class BraveChallengeInfo {
        private int difficult;
        private int id;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class PatrolInfo {
        private int mapId;
        private int x;
        private int y;
    }

    @Setter
    @Getter
    static class PlatformItemInfo {
        private String name;
        private int id;
        private long count;
        private long expireTime;

        public PlatformItemInfo(int id, long count, String playerIdx) {
            this.id = id;
            this.count = count;
            this.name = Item.getItemName(id);
            this.expireTime = -1;
//            ItemObject item = Item.getById(id);
//            if (item != null) {
//                //如果为迷雾深林道具
//                if (item.getSpecialtype() == ItemType.MIST_BOX) {
//                    this.expireTime = timerCache.getInstance().getTimerNextTriggerTime(TimerIdx.TI_RESET_DAILY_DATE);
//                }
//            }
        }
    }

    @Setter
    @Getter
    static class FragmentInfo {
        private String name;
        private int id;
        private int count;
        private String qualityValue;

        public FragmentInfo(int id, int count) {
            this.id = id;
            this.count = count;
            this.name = PetFragmentConfig.getNameById(id);
            this.qualityValue = StatisticsLogUtil.getQualityName(PetFragmentConfig.getQualityByCfgId(id));
        }
    }

    @Getter
    @Setter
    static class PlatformMonthCardInfo {
        private int cardId;
        private long expireTime;

        public PlatformMonthCardInfo(int cardId, long expireTime) {
            this.cardId = cardId;
            this.expireTime = expireTime;
        }
    }


}
