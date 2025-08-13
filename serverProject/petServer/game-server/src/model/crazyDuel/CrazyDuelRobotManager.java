package model.crazyDuel;

import cfg.CrazyDuelFloor;
import cfg.CrazyDuelFloorObject;
import cfg.CrazyDuelRobot;
import cfg.CrazyDuelRobotObject;
import cfg.Head;
import cfg.HeadBorder;
import common.GameConst;
import common.JedisUtil;
import common.load.ServerConfig;
import lombok.Data;
import lombok.Getter;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.pet.PetFactory;
import model.pet.dbCache.petCache;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Common;
import protocol.CrayzeDuel;
import protocol.CrazyDuelDB;
import protocol.PetMessage;
import util.ArrayUtil;
import util.ObjUtil;
import util.RandomUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CrazyDuelRobotManager {
    @Getter
    private static final String robotStar = "CrazyDuel-";


    @Getter
    private static CrazyDuelRobotManager instance = new CrazyDuelRobotManager();

    private CrazyDuelCache cache = CrazyDuelCache.getInstance();


    public void checkAndInitRobot() {
        if (!JedisUtil.lockRedisKey(GameConst.RedisKey.CrazyDuelRobot, 100000)){
            return;
        }
        if (CrazyDuelCache.getInstance().getPagePlayerSize() > 0) {
            return;
        }

        Map<String, Long> robotAbility = new HashMap<>();

        for (CrazyDuelRobotObject cfg : CrazyDuelRobot._ix_id.values()) {
            for (int i = 0; i < cfg.getNeedcount(); i++) {
                CrazyDuelDB.CrazyDuelSettingDB.Builder db = CrazyDuelDB.CrazyDuelSettingDB.newBuilder();
                db.setPlayerIdx(robotStar + cfg.getId() + "-" + i);
                db.setPublish(true);
                for (CrazyDuelFloorObject floorCfg : CrazyDuelFloor._ix_floor.values()) {
                    if (floorCfg.getFloor() <= 0) {
                        continue;
                    }
                    CrayzeDuel.CrazyDuelBuffSetting.Builder buffSetting = createRobotBuffSetting(floorCfg);
                    db.putBuffSetting(floorCfg.getFloor(), buffSetting.build());
                }

                List<PetMessage.Pet> petList = PetFactory.createPetList(cfg.getPetcount(), RandomUtil
                        .randomInScope(cfg.getPetlvrange()[0], cfg.getPetlvrange()[1]));

                List<Battle.BattlePetData> battlePetData = petCache.getInstance().buildPetBattleData(null, petList, null, true);
                db.addAllBattleData(battlePetData);
                CrazyDuelManager.getInstance().savePlayerSettingDb(db.build());

                CrazyDuelManager.getInstance().initPlayerScore(db.getPlayerIdx());

                long ability = battlePetData.stream().mapToLong(Battle.BattlePetData::getAbility).sum();

                robotAbility.put(db.getPlayerIdx(), ability);

                CrazyDuelPlayerPageDB pageDB = buildRobotPagePlayer(db.getPlayerIdx(), cfg.getHonrlv(), ability);
                cache.saveShowPagePlayer(pageDB);
                cache.savePlayerFromSvrIndex(db.getPlayerIdx(), ServerConfig.getInstance().getServer());
                CrazyDuelManager.getInstance().createPlayerDb(pageDB.getPlayerId());
            }
        }
        JedisUtil.unlockRedisKey(GameConst.RedisKey.CrazyDuelRobot);
        CrazyDuelCache.getInstance().savePlayerAbility(robotAbility);
    }

    private CrayzeDuel.CrazyDuelBuffSetting.Builder createRobotBuffSetting(CrazyDuelFloorObject floorCfg) {
        CrayzeDuel.CrazyDuelBuffSetting.Builder buffSetting = CrayzeDuel.CrazyDuelBuffSetting.newBuilder();
        buffSetting.setFloor(floorCfg.getFloor());
        List<Integer> fixBuff = RandomUtil.batchRandomFromList(ArrayUtil.intArrayToList(floorCfg.getFixbuffpool()), floorCfg.getFixbuffnum(), false);
        buffSetting.addAllBuff(fixBuff);
        int emptyPos = 2 - fixBuff.size();
        setEmptyPos(buffSetting, emptyPos);
        int num = floorCfg.getExbuffposappeare().length <= 3 ? 0 : floorCfg.getExbuffposappeare()[2];
        for (int i1 = 0; i1 < num; i1++) {
            List<Integer> buffs = CrazyDuelManager.getInstance().randomBuffPoolByCfg(floorCfg.getExbuffposappeare()[0], floorCfg.getRandombuff());
            if (CollectionUtils.isEmpty(buffs)) {
                buffSetting.addBuff(0);
            } else {
                buffSetting.addBuff(buffs.get(0));
            }
        }
        emptyPos = 2 - num;
        setEmptyPos(buffSetting, emptyPos);
        List<Integer> buffs = CrazyDuelManager.getInstance().randomBuffPoolByCfg(floorCfg.getLastbuffposrarity(), floorCfg.getRandombuff());
        if (CollectionUtils.isEmpty(buffs)) {
            buffSetting.addBuff(0);
        } else {
            buffSetting.addBuff(buffs.get(0));
        }
        return buffSetting;
    }

    private void setEmptyPos(CrayzeDuel.CrazyDuelBuffSetting.Builder buffSetting, int emptyPos) {
        if (emptyPos <= 0) {
            return;
        }
        for (int i1 = 0; i1 < emptyPos; i1++) {
            buffSetting.addBuff(0);
        }
    }

    public CrazyDuelPlayerPageDB buildRobotPagePlayer(String robotIdx, int honrlv, long ability) {
        CrazyDuelPlayerPageDB player = new CrazyDuelPlayerPageDB();
        player.setPlayerId(robotIdx);
        player.setName(ObjUtil.createRandomName(Common.LanguageEnum.forNumber(ServerConfig.getInstance().language)));

        player.setAbility(ability);
        player.setHeadBorderId(HeadBorder.getDefaultHeadBorder());
        player.setPlayerLevel(RandomUtil.randomInScope(1, 100));
        player.setPublishTime(System.currentTimeMillis());
        player.setHonLv(honrlv);
        player.setHeadId(Head.randomGetAvatar());
        player.setRobot(true);
        player.setPublish(true);
        return player;
    }


    public boolean isRobot(String playerIdx) {
        return playerIdx.startsWith(robotStar);
    }
}
