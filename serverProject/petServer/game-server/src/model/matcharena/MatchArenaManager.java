package model.matcharena;

import cfg.MatchArenaRobotPropertyCfg;
import cfg.MatchArenaRobotPropertyCfgObject;
import cfg.MatchArenaRobotTeam;
import com.alibaba.fastjson.JSON;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.tick.GlobalTick;
import common.tick.Tickable;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.matcharena.dto.MatchArenaRobotCfg;
import model.pet.dbCache.petCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.lang.StringUtils;
import protocol.Battle;
import protocol.MatchArena;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_NormalMatchArenaBattleInfo_VALUE;
import protocol.PetMessage;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaCancelMatch;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;
import util.RandomUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/05/26
 */
public class MatchArenaManager implements Tickable {

    private MatchArenaRobotCfg matchArenaRobotCfg = new MatchArenaRobotCfg();

    private static MatchArenaManager instance;


    public static MatchArenaManager getInstance() {
        if (instance == null) {
            synchronized (MatchArenaManager.class) {
                if (instance == null) {
                    instance = new MatchArenaManager();
                }
            }
        }
        return instance;
    }

    private MatchArenaManager() {
    }


    private final Set<String> matchingPlayerSet = new ConcurrentSet<>();

    private final Map<String, Integer> playerIpPortMap = new ConcurrentHashMap<>();

    public boolean init() {
        MatchArenaOpenManager.getInstance().init();
        initRobotIfExpire();
        return GlobalTick.getInstance().addTick(this);
    }

    @Override
    public void onTick() {
        initRobotIfExpire();
    }

    public void cancelAllPlayerMatch() {
        GS_BS_MatchArenaCancelMatch.Builder cancelBuilder = GS_BS_MatchArenaCancelMatch.newBuilder();
        for (String playerIdx : this.matchingPlayerSet) {
            int playerServerIndex = MatchArenaManager.getInstance().getPlayerServerIndex(playerIdx);
            BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClient(playerServerIndex);
            if (nettyClient == null) {
                LogUtil.error("model.matcharena.MatchArenaManager.cancelAllPlayerMatch, playerIdx:" + playerIdx
                        + ", battle server not found, send cancel msg failed");
                continue;
            }
            cancelBuilder.clear();
            cancelBuilder.setPlayerIdx(playerIdx);

            nettyClient.send(MsgIdEnum.GS_BS_MatchArenaCancelMatch_VALUE, cancelBuilder);
        }
    }


    public void addMatchingPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        this.matchingPlayerSet.add(playerIdx);
    }

    public void removeMatchingPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        this.matchingPlayerSet.remove(playerIdx);
    }

    public boolean isMatching(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return false;
        }
        return this.matchingPlayerSet.contains(playerIdx);
    }


    public void savePlayerServerIndex(String playerIdx, int serverIndex) {
        if (StringUtils.isEmpty(playerIdx) || serverIndex <= 0) {
            return;
        }
        this.playerIpPortMap.put(playerIdx, serverIndex);
    }

    public int getPlayerServerIndex(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return 0;
        }
        return this.playerIpPortMap.get(playerIdx);
    }

    public void onPlayerLogin(String playerIdx) {
        if (!isMatching(playerIdx)) {
            return;
        }

        SC_MathArenaStartMatch.Builder builder = SC_MathArenaStartMatch.newBuilder()
                .setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_MathArenaStartMatch_VALUE, builder);
    }

    public PetMessage.Pet recreateBattlePet(int petBookId) {
        if (matchArenaRobotCfg == null) {
            return null;
        }

        PetMessage.Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(petBookId, 0);
        if (petBuilder == null) {
            return null;
        }
        petBuilder.setPetRarity(matchArenaRobotCfg.getRarity());
        petBuilder.setPetLvl(matchArenaRobotCfg.getLevel());
        petCache.getInstance().refreshPetData(petBuilder, null, null,
                false, matchArenaRobotCfg.getExProperty(), null,false);
        return petBuilder.build();
    }

    public Iterable<Battle.BattlePetData> randomNormalArenaPetList() {
        int[] team = MatchArenaRobotTeam.randomTeam();
        List<PetMessage.Pet> petList = new ArrayList<>();
        for (int robot : team) {
            PetMessage.Pet pet = recreateBattlePet(robot);
            if (pet != null) {
                petList.add(pet);
            }
        }
        return petCache.getInstance().buildPlayerPetBattleData(null, petList, Battle.BattleSubTypeEnum.BSTE_MatchArena);
    }

    private void initRobotIfExpire() {
        if (matchArenaRobotCfg != null && matchArenaRobotCfg.getExpireTime() > GlobalTick.getInstance().getCurrentTime()) {
            return;
        }
        while (!initRobot()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean initRobot() {
        if (loadFromRedis()) {
            return true;
        }

        if (JedisUtil.lockRedisKey(RedisKey.MatchArenaMatchRobotInit, 5000L)) {
            if (loadFromRedis()) {
                return true;
            }
            this.matchArenaRobotCfg = randomRobotConfig();
            jedis.set(RedisKey.MatchArenaMatchRobotInfo, JSON.toJSONString(this.matchArenaRobotCfg));
            return true;
        }
        return true;
    }

    private boolean loadFromRedis() {
        MatchArenaRobotCfg cfg = loadCfgFromRedis();
        if (cfg != null && cfg.getExpireTime() > GlobalTick.getInstance().getCurrentTime()) {
            this.matchArenaRobotCfg = cfg;
            return true;
        }
        return false;
    }

    private MatchArenaRobotCfg loadCfgFromRedis() {
        String robotCfg = jedis.get(RedisKey.MatchArenaMatchRobotInfo);
        return JSON.parseObject(robotCfg, MatchArenaRobotCfg.class);
    }


    private MatchArenaRobotCfg randomRobotConfig() {
        MatchArenaRobotCfg robotCfg = new MatchArenaRobotCfg();
        MatchArenaRobotPropertyCfgObject cfg = MatchArenaRobotPropertyCfg.randomCfg();
        robotCfg.setRarity(cfg.getRarity());

        robotCfg.setExProperty(MapUtil.exPropertyToMap(cfg.getExproperty()));
        int[] levelRange = cfg.getLevel();
        robotCfg.setLevel(RandomUtil.randomInScope(levelRange[0], levelRange[1]));
        robotCfg.setExpireTime(GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN * 5);
        return robotCfg;
    }


    public void onArenaClose() {
        cancelAllPlayerMatch();
    }

    public GameConst.ArenaType getCurArenaType() {
        return MatchArenaOpenManager.getInstance().getCurArenaType();
    }

    public boolean isOpen() {
        return getCurArenaType() != GameConst.ArenaType.Null;
    }

    public List<Battle.BattlePetData> recreateBattlePets(String playerId, List<Integer> petIds) {
        List<PetMessage.Pet> newPets = new ArrayList<>();
        PetMessage.Pet pet;
        for (Integer petId : petIds) {
            pet = recreateBattlePet(petId);
            if (pet != null) {
                newPets.add(pet);
            }
        }
        return petCache.getInstance().buildPetBattleData(playerId, newPets, Battle.BattleSubTypeEnum.BSTE_ArenaMatchNormal, true);
    }

    public MatchArenaRobotCfg getCurRobotCfg() {
        return this.matchArenaRobotCfg;
    }

    public void sendNormalMatchArenaBattleInfo(String playerIdx) {
        MatchArena.SC_NormalMatchArenaBattleInfo.Builder msg = MatchArena.SC_NormalMatchArenaBattleInfo.newBuilder();
        msg.setPetLevel(matchArenaRobotCfg.getLevel());
        msg.setPetRarity(matchArenaRobotCfg.getRarity());
        msg.setExpireTime(matchArenaRobotCfg.getExpireTime());
        GlobalData.getInstance().sendMsg(playerIdx, SC_NormalMatchArenaBattleInfo_VALUE, msg);
    }
}