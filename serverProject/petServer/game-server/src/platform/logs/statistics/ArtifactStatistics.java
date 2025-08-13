package platform.logs.statistics;

import cfg.ArtifactConfig;
import com.alibaba.fastjson.annotation.JSONField;
import db.entity.BaseEntity;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.ObjectUtils;
import protocol.PlayerInfo.Artifact;
import protocol.PlayerInfo.ArtifactEnhancePoint;
import util.MapUtil;

/**
 * @Description 神器统计
 * @Author hanx
 * @Date2020/10/14 0014 14:51
 **/
public class ArtifactStatistics extends AbstractStatistics implements Serializable {
    private static final long serialVersionUID = -3067952197419187598L;
    private static final ArtifactStatistics instance = new ArtifactStatistics();

    public static ArtifactStatistics getInstance() {
        return instance;
    }


    /**
     * <神器id，总强化等级>
     */
    @JSONField(serialize = false)
    private final Map<Integer, Long> enhanceLvMap = new ConcurrentHashMap<>();

    /**
     * <神器id，总星级>
     */
    @JSONField(serialize = false)
    private final Map<Integer, Long> starMap = new ConcurrentHashMap<>();


    /**
     * <神器id，总激活人数>
     */
    @JSONField(serialize = false)
    private final Map<Integer, Long> activeMap = new ConcurrentHashMap<>();


    @Override
    public void init() {
        for (BaseEntity value : playerCache.getInstance()._ix_id.values()) {
            playerEntity player = (playerEntity) value;
            for (Artifact artifact : player.getDb_data().getArtifactList()) {
                int enhanceLv = artifact.getEnhancePointList().stream().mapToInt(ArtifactEnhancePoint::getPointLevel).sum();
                MapUtil.add2LongMapValue(enhanceLvMap, artifact.getArtifactId(), (long) enhanceLv);
                MapUtil.add2LongMapValue(activeMap, artifact.getArtifactId(), 1L);
                MapUtil.add2LongMapValue(starMap, artifact.getArtifactId(), (long) artifact.getPlayerSkill().getSkillLv());
            }
        }
    }


    public synchronized void addEnhanceLv(int artifactId, long incrLv) {
        MapUtil.add2LongMapValue(enhanceLvMap, artifactId, incrLv);
    }

    public synchronized void addStarLv(int artifactId, long incrLv) {
        MapUtil.add2LongMapValue(starMap, artifactId, incrLv);
    }


    public synchronized void addActive(int artifactId) {
        MapUtil.add2LongMapValue(activeMap, artifactId, 1L);
    }


// ------------------以下为http返回转json的必要接口------------------

    /**
     * 平台查询 获取平均强化等级
     *
     * @return
     */
    public Map<String, Double> getAvgEnhanceLvMap() {
        return activeMap.entrySet().stream().collect(Collectors.toMap(
                entry -> ArtifactConfig.queryName(entry.getKey()), entry -> getAvg((Long) ObjectUtils.defaultIfNull(enhanceLvMap.get(entry.getKey()), 0L), entry.getValue())));

    }


    /**
     * 平台查询 获取平均升星等级
     *
     * @return
     */
    public Map<String, Double> getAvgStarUpMap() {
        return activeMap.entrySet().stream().collect(Collectors.toMap(
                entry -> ArtifactConfig.queryName(entry.getKey()), entry -> getAvg(starMap.get(entry.getKey()), entry.getValue())));

    }


}
