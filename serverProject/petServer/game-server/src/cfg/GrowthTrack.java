/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.collections4.CollectionUtils;
import protocol.TargetSystem.TargetTypeEnum;
import util.ArrayUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@annationInit(value = "GrowthTrack", methodname = "initConfig")
public class GrowthTrack extends baseConfig<GrowthTrackObject> {


    private static GrowthTrack instance = null;

    public static GrowthTrack getInstance() {

        if (instance == null)
            instance = new GrowthTrack();
        return instance;

    }


    public static Map<Integer, GrowthTrackObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (GrowthTrack) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "GrowthTrack");

        for (Map e : ret) {
            put(e);
        }

    }

    public static GrowthTrackObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, GrowthTrackObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMissionlist(MapHelper.getInts(e, "missionList"));

        config.setDefaultunlock(MapHelper.getBoolean(e, "defaultUnlock"));

        config.setNextmissiongroup(MapHelper.getInts(e, "nextMissionGroup"));


        _ix_id.put(config.getId(), config);
    }

    public static List<MissionObject> getSatisfyMissions(TargetTypeEnum typeEnum) {
        if (typeEnum == null) {
            return null;
        }

        if (TARGET_TYPE_MISSIONS_MAP.isEmpty()) {
            synchronized (MONITOR) {
                if (TARGET_TYPE_MISSIONS_MAP.isEmpty()) {
                    initTargetTypeMissionsMap();
                }
            }
        }

        return TARGET_TYPE_MISSIONS_MAP.get(typeEnum.getNumber());
    }

    private final static Object MONITOR = new Object();

    private static void initTargetTypeMissionsMap() {
        for (GrowthTrackObject value : _ix_id.values()) {
            if (value.getId() <= 0) {
                continue;
            }

            for (int missionId : value.getMissionlist()) {
                MissionObject mission = Mission.getById(missionId);
                if (mission != null) {
                    List<MissionObject> list = TARGET_TYPE_MISSIONS_MAP.get(mission.getMissiontype());
                    if (list == null) {
                        list = new ArrayList<>();
                        TARGET_TYPE_MISSIONS_MAP.put(mission.getMissiontype(), list);
                    }
                    list.add(mission);
                }
            }
        }

    }

    public static Set<Integer> getDefaultMissionGroupIdsSet() {
        Set<Integer> defaultUnlock = _ix_id.values().stream()
                .filter(e -> e.getId() > 0 && e.getDefaultunlock())
                .map(GrowthTrackObject::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(defaultUnlock)) {
            LogUtil.error("GrowthTrack.getDefaultMissionGroupIdsSet, default unlock mission groups is empty");
        }
        return defaultUnlock;
    }

    private final static Map<Integer, Boolean> CONTAIN_LOGIN_MISSION_GROUP = new HashMap<>();
    /**
     * <TargetSystemEnumValue,
     */
    private final static Map<Integer, List<MissionObject>> TARGET_TYPE_MISSIONS_MAP = new HashMap<>();

    public static boolean containLoginMission(Collection<Integer> curMissionGroupIds) {
        if (CollectionUtils.isEmpty(curMissionGroupIds)) {
            return false;
        }

        if (CONTAIN_LOGIN_MISSION_GROUP.isEmpty()) {
            synchronized (MONITOR) {
                if (CONTAIN_LOGIN_MISSION_GROUP.isEmpty()) {
                    initContainLoginMissionGroup();
                }
            }
        }

        for (Integer curMissionGroupId : curMissionGroupIds) {
            if (Boolean.TRUE.equals(CONTAIN_LOGIN_MISSION_GROUP.get(curMissionGroupId))) {
                return true;
            }
        }

        return false;
    }

    private static void initContainLoginMissionGroup() {
        for (GrowthTrackObject value : _ix_id.values()) {
            if (value.getId() <= 0) {
                continue;
            }
            for (int missionId : value.getMissionlist()) {
                MissionObject mission = Mission.getById(missionId);
                if (mission != null) {
                    if (mission.getMissiontype() == TargetTypeEnum.TTE_CumuLogin_VALUE) {
                        CONTAIN_LOGIN_MISSION_GROUP.put(value.getId(), true);
                        LogUtil.info("cfg.GrowthTrack.initContainLoginMissionGroup, id:" + value.getId() + ", contains cumuLogin");
                        break;
                    }
                }
            }
        }
    }

    public static GrowthTrackObject getByMissionId(int missionId) {
        for (GrowthTrackObject value : _ix_id.values()) {
            if (ArrayUtil.intArrayContain(value.getMissionlist(), missionId)) {
                return value;
            }
        }
        return null;
    }
}
