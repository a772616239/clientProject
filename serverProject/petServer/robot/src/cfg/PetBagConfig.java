/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import datatool.MapHelper;
import model.base.baseConfig;
import petrobot.util.ServerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetBagConfig", methodname = "initConfig")
public class  PetBagConfig extends baseConfig<PetBagConfigObject> {


    private static PetBagConfig instance = null;

    public static PetBagConfig getInstance() {

        if (instance == null)
            instance = new PetBagConfig();
        return instance;

    }

    /**
     * 扩容宠物全背包需要的钻石数
     */
    public static int enlargeWholePetBagDiamond;
    public static Map<Integer, PetBagConfigObject> _ix_enlargetime = new HashMap<Integer, PetBagConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetBagConfig) o;
        initConfig();
    }


    public void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetBagConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetBagConfigObject getByEnlargetime(int enlargetime) {

        return _ix_enlargetime.get(enlargetime);

    }

    public void putToMem(Map e, PetBagConfigObject config) {

        config.setEnlargetime(MapHelper.getInt(e, "EnlargeTime"));

        config.setPetbagconsume(MapHelper.getInts(e, "petBagConsume"));

        config.setPetbagenlarge(MapHelper.getInt(e, "petBagEnlarge"));

        config.setPetbagamount(MapHelper.getInt(e, "petBagAmount"));

        config.setPetrunebagconsume(MapHelper.getInts(e, "petRuneBagConsume"));

        config.setPetrunebagenlarge(MapHelper.getInt(e, "petRuneBagEnlarge"));

        config.setPetrunebagamount(MapHelper.getInt(e, "petRuneBagAmount"));


        _ix_enlargetime.put(config.getEnlargetime(), config);
        enlargeWholePetBagDiamond += config.getPetbagconsume()[2];
    }
}
