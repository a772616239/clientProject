package model.mission;

import cfg.KeyNodeConfig;
import cfg.KeyNodeConfigObject;
import cfg.Mission;
import cfg.MissionObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class MissionManager {
    @Getter
    public static MissionManager instance = new MissionManager();

    public static final Map<Integer, List<MissionObject>> keyNodeMissions = new HashMap<>();

    public static final List<MissionObject> allKeyNodeMissions = new ArrayList<>();

    public static final Map<Integer, List<MissionObject>> keyNodeMissionsByType = new HashMap<>();

    public List<MissionObject> getMissions() {
        checkInit();
        return allKeyNodeMissions;
    }

    public List<MissionObject> getKeyNodeMissionsByMissionType(int type) {
        checkInit();
        return keyNodeMissionsByType.get(type);
    }

    public List<Integer> getKeyNodeMissionsByMissionKeyNode(int keyNode) {
        checkInit();
        List<MissionObject> missions = keyNodeMissions.get(keyNode);
        if (CollectionUtils.isEmpty(missions)) {
            return Collections.emptyList();
        }
        return missions.stream().map(MissionObject::getId).collect(Collectors.toList());
    }

    public List<MissionObject> getKeyNodeMissionObjectsByMissionKeyNode(int keyNode) {
        checkInit();
        List<MissionObject> missions = keyNodeMissions.get(keyNode);
        if (CollectionUtils.isEmpty(missions)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(missions);
    }


    private void checkInit() {
        if (MapUtils.isEmpty(keyNodeMissions)) {
            synchronized (this) {
                if (MapUtils.isEmpty(keyNodeMissions)) {
                    initMissions();
                }
            }
        }
    }

    private void initMissions() {
        for (KeyNodeConfigObject cfg : KeyNodeConfig._ix_id.values()) {
            List<MissionObject> keyNodeMission = new ArrayList<>();
            for (int missionId : cfg.getMissionids()) {
                MissionObject mission = Mission.getById(missionId);
                if (mission != null) {
                    keyNodeMission.add(mission);
                    allKeyNodeMissions.add(mission);
                    List<MissionObject> missionObjects = keyNodeMissionsByType.computeIfAbsent(mission.getMissiontype(), a -> new ArrayList<>());
                    missionObjects.add(mission);
                }
            }
            keyNodeMissions.put(cfg.getId(), keyNodeMission);
        }
    }
}
