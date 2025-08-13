package platform.logs.entity.petGem;

import cfg.PetGemConfig;
import cfg.PetGemNameIconConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PetGemLvUpLog extends AbstractPlayerLog {

    private String name;
    private String id;
    private String originalRarity;
    private int originalLv;
    List<PetPropertyLog> originalProperty;
    private List<ConsumeLog> consumeList;

    private int targetLv;
    List<PetPropertyLog> targetProperty;

    public PetGemLvUpLog(String playerId, String id, int originalCfgId, List<Consume> consumes, int targetCfgId) {
        super(playerId);
        this.id = id;
        this.name = PetGemNameIconConfig.queryName(originalCfgId);
        this.originalRarity = StatisticsLogUtil.getGemRarityName(PetGemConfig.queryRarity(originalCfgId));
        this.originalLv = PetGemConfig.queryEnhanceLv(originalCfgId);
        this.originalProperty = StatisticsLogUtil.buildPropertyList(PetGemConfig.getGemAdditionByGemCfgId(originalCfgId));
        this.consumeList = StatisticsLogUtil.buildConsumeByList(consumes);
        this.targetLv = PetGemConfig.queryEnhanceLv(targetCfgId);
        this.targetProperty = StatisticsLogUtil.buildPropertyList(PetGemConfig.getGemAdditionByGemCfgId(targetCfgId));

    }
}

