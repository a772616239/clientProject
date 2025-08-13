/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import protocol.PlayerInfo.Artifact;
import protocol.PlayerInfo.ArtifactEnhancePoint;
import protocol.PlayerInfo.ArtifactEnhancePoint.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "ArtifactConfig", methodname = "initConfig")
public class ArtifactConfig extends baseConfig<ArtifactConfigObject> {


    private static ArtifactConfig instance = null;

    public static ArtifactConfig getInstance() {

        if (instance == null)
            instance = new ArtifactConfig();
        return instance;

    }


    public static Map<Integer, ArtifactConfigObject> _ix_key = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArtifactConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArtifactConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArtifactConfigObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, ArtifactConfigObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setPlayerskillid(MapHelper.getInt(e, "PlayerSkillID"));

        config.setEnhancepointid(MapHelper.getInts(e, "EnhancePointId"));

        config.setStarconfgid(MapHelper.getInts(e, "StarConfgId"));

        config.setTip_name(MapHelper.getStr(e, "tip_name"));

        config.setTalent(MapHelper.getIntArray(e, "Talent"));


        _ix_key.put(config.getKey(), config);

    }

    public static String queryName(int artifactId) {
        ArtifactConfigObject config = _ix_key.get(artifactId);
        if (config == null) {
            return "";
        }
        return config.getTip_name();

    }


    /**
     * 获取下一个强化点位
     *
     * @param artifact
     * @param artifactConfig 当前神器config
     * @return
     */
    public static Builder getNextEnhancePoint(Artifact.Builder artifact, ArtifactConfigObject artifactConfig) {
        if (artifact.getEnhancePointCount() < artifactConfig.getEnhancepointid().length) {
            int pointId = artifactConfig.getEnhancepointid()[artifact.getEnhancePointCount()];
            ArtifactEnhancePoint.Builder builder = ArtifactEnhancePoint.newBuilder().setPointId(pointId);
            artifact.addEnhancePoint(builder);
        }
        return artifact.getEnhancePointBuilderList().stream().min((o1, o2) -> {
            if (o1.getPointLevel() == o2.getPointLevel()) {
                return o1.getPointId() - o2.getPointId();
            }
            return o1.getPointLevel() - o2.getPointLevel();
        }).get();


    }

    /**
     * 当前最大强化点位
     *
     * @param artifact
     * @return
     */
    public static ArtifactEnhancePoint getCurEnhancePoint(Artifact artifact) {
        return getCurEnhancePoint(artifact.getEnhancePointList());
    }

    /**
     * 当前最大强化点位
     *
     * @param artifact
     * @return
     */
    public static ArtifactEnhancePoint getCurEnhancePoint(Artifact.Builder artifact) {
        return getCurEnhancePoint(artifact.getEnhancePointList());
    }

    private static ArtifactEnhancePoint getCurEnhancePoint(List<ArtifactEnhancePoint> list) {
        return list.stream().max((o1, o2) -> {
            if (o1.getPointLevel() == o2.getPointLevel()) {
                return o1.getPointId() - o2.getPointId();
            }
            return o1.getPointLevel() - o2.getPointLevel();
        }).orElse(null);
    }
}
