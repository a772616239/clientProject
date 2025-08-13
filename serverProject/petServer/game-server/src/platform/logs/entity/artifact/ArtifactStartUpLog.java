package platform.logs.entity.artifact;

import cfg.ArtifactConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import protocol.Common.Consume;


@Getter
@Setter
@NoArgsConstructor
public class ArtifactStartUpLog extends AbstractPlayerLog {

    private int id;

    /**
     * 上一级
     */
    private int lastStar;
    /**
     * 当前等级
     */
    private int nowStar;

    /**
     * 消耗
     */
    private ConsumeLog consume;

    private String name;


    public ArtifactStartUpLog(String playerId, int artifactId, Consume consume, int originalLv, int skillLv) {
        super(playerId);
        this.id = artifactId;
        this.name = ArtifactConfig.queryName(artifactId);
        this.consume = new ConsumeLog(consume);
        this.lastStar = originalLv;
        this.nowStar = skillLv;
    }
}


