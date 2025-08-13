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

@annationInit(value = "ArenaDan", methodname = "initConfig")
public class ArenaDan extends baseConfig<ArenaDanObject> {


    private static ArenaDan instance = null;

    public static ArenaDan getInstance() {

        if (instance == null)
            instance = new ArenaDan();
        return instance;

    }


    public static Map<Integer, ArenaDanObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArenaDan) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArenaDan");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArenaDanObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ArenaDanObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setServername(MapHelper.getInt(e, "servername"));

        config.setRoommaxsize(MapHelper.getInt(e, "roomMaxSize"));

        config.setStartscore(MapHelper.getInt(e, "startScore"));

        config.setFightmap(MapHelper.getInt(e, "fightMap"));

        config.setOpponentrange(MapHelper.getIntArray(e, "opponentRange"));

        config.setUpgradescore(MapHelper.getInt(e, "upgradeScore"));

        config.setUpgraderanking(MapHelper.getInt(e, "upgradeRanking"));

        config.setUpgradedirectcount(MapHelper.getInt(e, "upgradeDirectCount"));

        config.setNextdan(MapHelper.getInt(e, "nextDan"));

        config.setNeedsettledan(MapHelper.getBoolean(e, "needSettleDan"));

        config.setRobotpetpool(MapHelper.getInts(e, "robotPetPool"));

        config.setDanreachreward(MapHelper.getIntArray(e, "danReachReward"));

        config.setTitleid(MapHelper.getInt(e, "titleId"));


        _ix_id.put(config.getId(), config);


        if (config.getId() > maxDan) {
            this.maxDan = config.getId();
        }
    }

    private int maxDan;

    public int getMaxDan() {
        return maxDan;
    }

}
