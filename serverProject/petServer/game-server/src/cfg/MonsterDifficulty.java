/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.mainLine.dbCache.mainlineCache;
import org.apache.commons.lang.StringUtils;
import util.LogUtil;

@annationInit(value = "MonsterDifficulty", methodname = "initConfig")
public class MonsterDifficulty extends baseConfig<MonsterDifficultyObject> {


    private static MonsterDifficulty instance = null;

    public static MonsterDifficulty getInstance() {

        if (instance == null)
            instance = new MonsterDifficulty();
        return instance;

    }


    public static Map<Integer, MonsterDifficultyObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MonsterDifficulty) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MonsterDifficulty");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MonsterDifficultyObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MonsterDifficultyObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMonsterlevelscope(MapHelper.getInts(e, "monsterLevelScope"));

        config.setMonsterstarscope(MapHelper.getInts(e, "monsterStarScope"));

        config.setPatrolsweepreward(MapHelper.getIntArray(e, "patrolSweepReward"));

        config.setPatrolfixedbastardreward(MapHelper.getIntArray(e, "patrolFixedBastardReward"));

        config.setPatrolfixedtreasurereward(MapHelper.getIntArray(e, "patrolFixedTreasureReward"));

        config.setPatrolfixedbossreward(MapHelper.getIntArray(e, "patrolFixedBossReward"));

        config.setPatrolconfig(MapHelper.getIntArray(e, "patrolConfig"));

        config.setPatrolbosscfg(MapHelper.getIntArray(e, "patrolBossCfg"));

        config.setForeigninvasioncommonmonsterreward(MapHelper.getIntArray(e, "foreignInvasionCommonMonsterReward"));

        config.setForeigninvasionelitemonsterreward(MapHelper.getIntArray(e, "foreignInvasionEliteMonsterReward"));

        config.setForeigninvasionbossreward(MapHelper.getIntArray(e, "foreignInvasionBossReward"));

        config.setForeigninvasionmonstercfg(MapHelper.getIntArray(e, "foreignInvasionMonsterCfg"));

        config.setForeigninvasionbosscfg(MapHelper.getInt(e, "foreignInvasionBossCfg"));

        config.setPetmissionrewardconfig(MapHelper.getIntArray(e, "petMissionRewardConfig"));

        config.setCpmonsterbaselv(MapHelper.getInt(e, "cpMonsterBaseLv"));

        config.setBravabaselv(MapHelper.getInt(e, "bravaBaseLv"));

        config.setBravepets(MapHelper.getIntArray(e, "bravePets"));

        config.setArenarobotexpropertylinkfightmake(MapHelper.getInt(e, "arenaRobotExPropertyLinkFightMake"));

        config.setNewforeigninvasionmonsterbaselv(MapHelper.getInt(e, "newForeignInvasionMonsterBaseLv"));

        config.setNewforeigninvasionmonster(MapHelper.getIntArray(e, "newForeignInvasionMonster"));

        config.setNewforeigninvasionmonsterrarity(MapHelper.getInt(e, "newForeignInvasionMonsterRarity"));


        _ix_id.put(config.getId(), config);
    }

        public static MonsterDifficultyObject getByPlayerIdx (String playerIdx){
            if (StringUtils.isBlank(playerIdx)) {
                return null;
            }

            int curCheckPoint = mainlineCache.getInstance().getPlayerCurNode(playerIdx);
            MonsterDifficultyObject diffCfg = getById(curCheckPoint);
            if (diffCfg == null) {
                LogUtil.error("cfg.MonsterDifficulty.getByPlayerIdx, can not find player monster diff, playerIdx:" + playerIdx + ", cur mainline check point:" + curCheckPoint);
            }
            return diffCfg;
        }
    }
