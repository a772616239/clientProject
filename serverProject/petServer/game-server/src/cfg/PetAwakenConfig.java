/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.Reward;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.PetAwake;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetAwakenConfig", methodname = "initConfig")
public class PetAwakenConfig extends baseConfig<PetAwakenConfigObject> {


    private static PetAwakenConfig instance = null;

    public static PetAwakenConfig getInstance() {

        if (instance == null)
            instance = new PetAwakenConfig();
        return instance;

    }


    public static Map<Integer, PetAwakenConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetAwakenConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetAwakenConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetAwakenConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetAwakenConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setAwaketype(MapHelper.getInt(e, "awakeType"));

        config.setOrientation(MapHelper.getInt(e, "orientation"));

        config.setUplvl(MapHelper.getInt(e, "upLvl"));

        config.setPetlvl(MapHelper.getInt(e, "petLvl"));

        config.setProperties(MapHelper.getIntArray(e, "properties"));

        config.setNeedexp(MapHelper.getInt(e, "needExp"));

        config.setCumuexp(MapHelper.getInt(e, "cumuExp"));

        config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));


        _ix_id.put(config.getId(), config);


    }


    public static PetAwakenConfigObject getConfig(int petUpLvl, int orientation, int awakeType) {
        return _ix_id.values().stream().filter(e -> e.getUplvl() == petUpLvl
                && e.getOrientation() == orientation && e.getAwaketype() == awakeType).findFirst().orElse(null);
    }


    public static int[][] getAwakePropertyAddition(Builder pet, int awakeType) {
        PetBasePropertiesObject baseConfig = PetBaseProperties.getByPetid(pet.getPetBookId());
        if (baseConfig == null) {
            LogUtil.error("can`t find PetBasePropertiesConfig by petBookId :{}", pet.getPetBookId());
            return null;
        }
        PetAwakenConfigObject awakeConfig = getConfig(pet.getPetUpLvl(), baseConfig.getPropertymodel(), awakeType);
        if (awakeConfig == null) {
            LogUtil.error("can`t find awakeConfig by awakeLv:{} ,propertyModel:{}, awakeType:{} ", pet.getPetUpLvl(), baseConfig.getPropertymodel(), awakeType);
            return null;
        }
        return awakeConfig.getProperties();
    }

    public static PetAwakenConfigObject getByTypeAndLv(int awakeType, int level, int orientation) {
        for (PetAwakenConfigObject config : _ix_id.values()) {
            if (config.getAwaketype() == awakeType && config.getUplvl() == level && config.getOrientation() == orientation) {
                return config;
            }
        }
        return null;
    }


    public static List<Reward> getSourceReturn(List<PetAwake> awakeList, int petOrientation) {
        if (CollectionUtils.isEmpty(awakeList)) {
            return Collections.emptyList();
        }
        List<Reward> rewards = new ArrayList<>();
        for (PetAwake awake : awakeList) {
            PetAwakenConfigObject config = PetAwakenConfig.getByTypeAndLv(awake.getType(), awake.getLevel(), petOrientation);
            if (config == null) {
                continue;
            }
            int totalExp = config.getCumuexp() + awake.getCurExp();
            List<Reward> curReward = ItemPetAwakeExpConfig.getSourceReturnByTypeAndExp(awake.getType(), totalExp);
            rewards.addAll(curReward);
        }
        return rewards;
    }

}
