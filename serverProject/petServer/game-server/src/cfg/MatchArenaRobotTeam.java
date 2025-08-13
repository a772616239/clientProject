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
import model.pet.dbCache.petCache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.PetMessage;
import util.CollectionUtil;
import util.RandomUtil;

@annationInit(value = "MatchArenaRobotTeam", methodname = "initConfig")
public class MatchArenaRobotTeam extends baseConfig<MatchArenaRobotTeamObject> {


    private static MatchArenaRobotTeam instance = null;

    public static MatchArenaRobotTeam getInstance() {

        if (instance == null)
            instance = new MatchArenaRobotTeam();
        return instance;

    }


    public static Map<Integer, MatchArenaRobotTeamObject> _ix_id = new HashMap<>();

    public static List<MatchArenaRobotTeamObject> objects = new ArrayList<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MatchArenaRobotTeam) o;
        initConfig();
        objects.addAll(_ix_id.values());
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MatchArenaRobotTeam");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MatchArenaRobotTeamObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MatchArenaRobotTeamObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setTeam(MapHelper.getInts(e, "team"));

        if (config.getId() <= 0) {
            return;
        }

        _ix_id.put(config.getId(), config);


    }


    public static int[] randomTeam() {
        MatchArenaRobotTeamObject cfg = objects.get(RandomUtils.nextInt(objects.size()));
        if (cfg == null) {
            return new int[]{};
        }
        return cfg.getTeam();
    }

    public static List<PetMessage.Pet.Builder> randomTeamPets() {
        MatchArenaRobotTeamObject cfg = objects.get(RandomUtils.nextInt(objects.size()));
        if (cfg == null) {
            return Collections.emptyList();
        }
        List<PetMessage.Pet.Builder> result = new ArrayList<>();
        PetMessage.Pet.Builder petBuilder;
        for (int petId : cfg.getTeam()) {
            petBuilder = petCache.getInstance().getPetBuilder(petId, 0);
            if (petBuilder != null) {
                result.add(petBuilder);
            }
        }
        return result;
    }
}
