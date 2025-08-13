package platform.logs.entity.petGem;

import cfg.PetGemConfig;
import cfg.PetGemNameIconConfig;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage.Gem;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PetGemDownLog extends AbstractPlayerLog {
    private String name;
    private String id;
    private String rarity;
    private int enhanceLv;
    private List<RewardLog> rewards = new ArrayList<>();

    private List<GemItemLog> gemReturn = new ArrayList<>();

    public PetGemDownLog(String playerId, Gem operateGem, List<Reward> rewards) {
        super(playerId);
        this.id = operateGem.getId();
        this.name = PetGemNameIconConfig.queryName(operateGem.getGemConfigId());
        this.enhanceLv = PetGemConfig.queryEnhanceLv(operateGem.getGemConfigId());
        for (Reward reward : rewards) {
            if (RewardTypeEnum.RTE_Gem != reward.getRewardType()) {
                this.rewards.add(new RewardLog(reward));
            } else {
                for (int i = 0; i < reward.getCount(); i++) {
                    gemReturn.add(new GemItemLog("", reward.getId()));
                }
            }
        }
        this.rarity = StatisticsLogUtil.getGemRarityName(PetGemConfig.queryRarity(operateGem.getGemConfigId()));

    }
}






