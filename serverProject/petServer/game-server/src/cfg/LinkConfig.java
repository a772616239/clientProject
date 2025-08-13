/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Collection;

@annationInit(value = "LinkConfig", methodname = "initConfig")
public class LinkConfig extends baseConfig<LinkConfigObject> {


    private static LinkConfig instance = null;

    public static LinkConfig getInstance() {

        if (instance == null)
            instance = new LinkConfig();
        return instance;

    }


    public static Map<Integer, LinkConfigObject> _ix_id = new HashMap<>();

    public static List<Collection.LinkExp> convertToLinkExp(List<Integer> linkIds) {
        if (CollectionUtils.isEmpty(linkIds)) {
            return Collections.emptyList();
        }
        List<Collection.LinkExp> result = new ArrayList<>();
        for (Integer linkId : linkIds) {
            LinkConfigObject cfg = getById(linkId);
            if (cfg == null || cfg.getExp() <= 0) {
                continue;
            }
            result.add(Collection.LinkExp.newBuilder().setExp(cfg.getExp()).setCfgId(cfg.getId()).build());
        }
        return result;
    }

    public static List<LinkConfigObject> canTriggerLinkIds(int petBookId) {
        return petLinkIdsMap.get(petBookId);
    }

    public static int getPetLinkBuffId(int petBookId, Integer linkId) {
        LinkConfigObject cfg = getById(linkId);
        if (cfg == null) {
            return 0;
        }
        for (int i = 0; i < cfg.getNeedpet().length; i++) {
            if (cfg.getNeedpet()[i] == petBookId) {
                return cfg.getBufflist()[i];
            }
        }
        return 0;
    }

    public static int findPetIndex(LinkConfigObject cfg, int petBookId) {
        for (int i = 0; i < cfg.getNeedpet().length; i++) {
            if (petBookId == cfg.getNeedpet()[i]) {
                return i;
            }
        }
        return -1;
    }

    public static Iterable<Integer> getPetLinkBuffs(int petBookId, List<Integer> activeLinkList) {
        if (CollectionUtils.isEmpty(activeLinkList)) {
            return Collections.emptyList();
        }

        List<Integer> buffs = new ArrayList<>();
        for (Integer linkId : activeLinkList) {
            LinkConfigObject cfg = getById(linkId);
            if (cfg == null) {
                continue;
            }
            int petIndex = findPetIndex(cfg, petBookId);
            if (petIndex != -1) {
                buffs.add(cfg.getBufflist()[petIndex]);
            }
        }
        return buffs;
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (LinkConfig) o;
        initConfig();
        initField();
    }

    private static Map<Integer, List<LinkConfigObject>> petLinkIdsMap = new HashMap<>();

    private void initField() {
        for (LinkConfigObject cfg : _ix_id.values()) {
            for (int petId : cfg.getNeedpet()) {
                List<LinkConfigObject> linkIds = petLinkIdsMap.computeIfAbsent(petId, a -> new ArrayList<>());
                linkIds.add(cfg);
            }
        }
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "LinkConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static LinkConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, LinkConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setNeedpet(MapHelper.getInts(e, "needPet"));

        config.setBufflist(MapHelper.getInts(e, "buffList"));

        config.setFixfight(MapHelper.getInts(e, "fixFight"));

        config.setLvlfightfactor(MapHelper.getInts(e, "lvlFightFactor"));

        config.setExp(MapHelper.getInt(e, "exp"));


        _ix_id.put(config.getId(), config);


    }
}
