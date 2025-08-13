package platform.logs.entity.artifact;

import cfg.ArtifactConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;
import protocol.PlayerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
public class ArtifactLvUpLog extends AbstractPlayerLog {

    private int id;
    private String name;
    private int lvUpType;
    private List<ConsumeLog> consumes;
    private List<PetPropertyLog> lastAddition;
    private List<PetPropertyLog> nowAddition;
    private Map<Integer, List<PetPropertyLog>> pointsProperties = new HashMap<>();
    private int level;
    private int curCyclePoint;


    public ArtifactLvUpLog(String playerId, PlayerInfo.Artifact.Builder artifact, List<Consume> consumes, boolean lvUpMax, Map<Integer, Integer> lastAddition, Map<Integer, Integer> nowAddition) {
        super(playerId);
        this.id = artifact.getArtifactId();
        this.name = ArtifactConfig.queryName(id);
        this.consumes = StatisticsLogUtil.buildConsumeByList(consumes);
        this.lvUpType = lvUpMax ? 1 : 0;
        this.lastAddition = StatisticsLogUtil.buildPropertyList(lastAddition);
        this.nowAddition = StatisticsLogUtil.buildPropertyList(nowAddition);
        this.level = artifact.getEnhancePointList().stream().mapToInt(PlayerInfo.ArtifactEnhancePoint::getPointLevel).min().orElse(1);
        PlayerInfo.ArtifactEnhancePoint enhancePoint = ArtifactConfig.getCurEnhancePoint(artifact);
        if (enhancePoint == null) {
            return;
        }
        this.curCyclePoint = enhancePoint.getPointId() % 6;
        if (curCyclePoint == 0) {
            level++;
        }
    }

}

