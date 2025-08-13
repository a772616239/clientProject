package platform.logs.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.LogClass.PetLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import platform.logs.AbstractPlayerLog;
import protocol.Common.Reward;
import protocol.PetMessage.Pet;

@Getter
@Setter
@NoArgsConstructor
public class PetDischargeLog extends AbstractPlayerLog {
    private List<RewardLog> rewardList;
    private List<PetLog> pet;
    private int dischargeType;//0是放生宠物，1是重生宠物

    public PetDischargeLog(String playerId, List<Pet> petList, List<Reward> rewardList, int dischargeType) {
        super(playerId);
        this.rewardList = StatisticsLogUtil.buildRewardLogList(rewardList);
        this.pet = StatisticsLogUtil.buildPetLogByPetList(petList);
        this.dischargeType = dischargeType;
    }
}

