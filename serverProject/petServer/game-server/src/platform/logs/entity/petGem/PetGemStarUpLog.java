package platform.logs.entity.petGem;

import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import cfg.PetGemNameIconConfig;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Reward;
import protocol.PetMessage.Gem;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PetGemStarUpLog extends AbstractPlayerLog {

    private List<GemItemLog> materials = new ArrayList<>();
    private GemItemLog newGem;
    private List<RewardLog> sourceReturn;


    public PetGemStarUpLog(String playerId, Gem lastGem, List<Gem> materialGems, Gem newGem, List<Reward> sourceReturn) {
        super(playerId);
        if (lastGem != null) {
            materials.add(new GemItemLog(lastGem.getId(), lastGem.getGemConfigId()));
        }

        if (CollectionUtils.isNotEmpty(materialGems)) {
            for (Gem gem : materialGems) {
                materials.add(new GemItemLog(gem.getId(), gem.getGemConfigId()));
            }
        }
        if (newGem != null) {
            this.newGem = new GemItemLog(newGem.getId(), newGem.getGemConfigId());
        }
        if (CollectionUtils.isNotEmpty(sourceReturn)) {
            this.sourceReturn = StatisticsLogUtil.buildRewardLogList(sourceReturn);
        }
    }
}

@Data
class GemItemLog {
    private String name;
    private String id;
    private String rarity;
    private int enhanceLv;
    private int star;

    public GemItemLog(String id, int cfgId) {
        this.id = id;
        PetGemConfigObject config = PetGemConfig.getById(cfgId);
        if (config != null) {
            this.name = PetGemNameIconConfig.queryName(cfgId);
            this.rarity = StatisticsLogUtil.getGemRarityName(config.getRarity());
            this.star = config.getStar();
            this.enhanceLv = config.getLv();
        }
    }

}

