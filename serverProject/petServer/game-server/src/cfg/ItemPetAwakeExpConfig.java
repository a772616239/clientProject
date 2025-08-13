/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@annationInit(value = "ItemPetAwakeExpConfig", methodname = "initConfig")
public class ItemPetAwakeExpConfig extends baseConfig<ItemPetAwakeExpConfigObject> {


    private static ItemPetAwakeExpConfig instance = null;

    public static ItemPetAwakeExpConfig getInstance() {

        if (instance == null)
            instance = new ItemPetAwakeExpConfig();
        return instance;

    }


    public static Map<Integer, ItemPetAwakeExpConfigObject> _ix_itemid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ItemPetAwakeExpConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ItemPetAwakeExpConfig");

        for (Map e : ret) {
            put(e);
        }

        map = _ix_itemid.values().stream().collect(Collectors.groupingBy(ItemPetAwakeExpConfigObject::getPropertytype));

        map.values().forEach(list -> list.sort((o1, o2) -> o2.getExp() - o1.getExp()));
    }

    public static ItemPetAwakeExpConfigObject getByItemid(int itemid) {

        return _ix_itemid.get(itemid);

    }


    public void putToMem(Map e, ItemPetAwakeExpConfigObject config) {

        config.setItemid(MapHelper.getInt(e, "itemId"));

        config.setPropertytype(MapHelper.getInt(e, "propertyType"));

        config.setProperty(MapHelper.getInts(e, "property"));

        config.setExp(MapHelper.getInt(e, "exp"));


        _ix_itemid.put(config.getItemid(), config);


    }

    private static Map<Integer, List<ItemPetAwakeExpConfigObject>> map = new HashMap();


    public static List<Reward> getSourceReturnByTypeAndExp(int type, int totalExp) {
        List<ItemPetAwakeExpConfigObject> configs = map.get(type);
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyList();
        }
        List<Reward> rewardList = new ArrayList<>();
        int num;
        for (ItemPetAwakeExpConfigObject config : configs) {
            num = totalExp / config.getExp();
            if (num <= 0) {
                continue;
            }
            totalExp = totalExp % config.getExp();
            rewardList.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Item, config.getItemid(), num));
        }
        return rewardList;

    }

}
