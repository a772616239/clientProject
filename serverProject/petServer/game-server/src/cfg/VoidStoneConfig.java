/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import lombok.Getter;
import lombok.Setter;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@annationInit(value = "VoidStoneConfig", methodname = "initConfig")
public class VoidStoneConfig extends baseConfig<VoidStoneConfigObject> {

    //初始品级
    @Getter
    @Setter
    private static int initRarity;

    //初始等级
    @Getter
    @Setter
    private static int initLv;

    private static VoidStoneConfig instance = null;

    public static VoidStoneConfig getInstance() {

        if (instance == null)
            instance = new VoidStoneConfig();
        return instance;

    }


    public static Map<Integer, VoidStoneConfigObject> _ix_id = new HashMap<Integer, VoidStoneConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (VoidStoneConfig) o;
        initConfig();

        initField();
        for (VoidStoneConfigObject value : _ix_id.values()) {
            Optional<VoidStoneConfigObject> any = _ix_id.values().stream().filter(e -> e.getId() != value.getId() && e.getRarity() == value.getRarity() && e.getLv() == value.getLv()).findAny();
            if (!any.isPresent()) {
                System.out.println(1);
            }
        }
    }

    private void initField() {
        //这里等级和品质不一定是1开始,从配置中查找最低级的
        Optional<VoidStoneConfigObject> min = _ix_id.values().stream().min((o1, o2) -> {
            if (o1.getRarity() == o2.getRarity()) {
                return o1.getLv() - o2.getLv();
            }
            return o1.getRarity() - o2.getRarity();
        });
        min.ifPresent(config -> {
            initRarity = config.getRarity();
            initLv = config.getLv();
        });
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "VoidStoneConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static VoidStoneConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, VoidStoneConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setLv(MapHelper.getInt(e, "lv"));

        config.setResourcelv(MapHelper.getInt(e, "resourceLv"));

        config.setNeedpetlv(MapHelper.getInt(e, "needPetLv"));

        config.setProbability(MapHelper.getInt(e, "probability"));

        config.setPropertytype(MapHelper.getInt(e, "propertyType"));

        config.setProperties(MapHelper.getIntArray(e, "properties"));

        config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));

        config.setChangeconsume(MapHelper.getIntArray(e, "changeConsume"));

        config.setLockconsume(MapHelper.getIntArray(e, "lockConsume"));


        _ix_id.put(config.getId(), config);


    }
}
