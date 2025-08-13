package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.PetLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.Battle.SkillBattleDict;
import protocol.Common.Reward;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2010/1/14
 */
@Getter
@Setter
@NoArgsConstructor
public class BattleLog extends AbstractPlayerLog {
    private int playerLv;
    /**
     * 敌方阵容id
     */
    private int fightId;
    /**
     * 关卡id
     **/
    private int pointId;
    private String battleType;
    private int battleTypeValue;
    /**
     * -1为平局，PVE中1为玩家胜利，2为怪物胜利，3为玩家投降
     **/
    private int battleResult;
    private List<RewardLog> rewards;
    /**
     * 玩家宠物阵容
     **/
    private List<PetLog> playerLineUp;

    /**
     * 玩家技能(这里技能拼接，因为平台表示list无法查询)
     */
    private String playerSkill;

    /**
     * 战斗用时
     */
    private long useTime;

    public BattleLog(String playerIdx, int battleResult, List<Reward> rewards,
                     Builder battleData, BattleSubTypeEnum battleType, int pointId, long useTime) {
        super(playerIdx);
        this.playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        this.battleResult = battleResult;
        if (battleType != null) {
            this.battleType = StatisticsLogUtil.getBattleSubTypeName(battleType);
            this.battleTypeValue = battleType.getNumber();
        }
        this.rewards = StatisticsLogUtil.buildRewardLogList(rewards);
        if (battleData != null) {
            this.fightId = battleData.getFightMakeId();
            this.playerLineUp = StatisticsLogUtil.buildPetLogListByBattleData(battleData, 1);
            this.playerSkill = getLogBattleSkillId(battleData);
        }

        this.pointId = pointId;
        this.useTime = useTime;
    }

    private String getLogBattleSkillId(Builder battleData) {
        Optional<BattlePlayerInfo> playerInfo = battleData.getPlayerInfoList().stream().filter(e -> e.getCamp() == 1).findFirst();
        if (playerInfo.isPresent()) {
            return StringUtils.join(playerInfo.get().getPlayerSkillIdListList().stream()
                    .map(SkillBattleDict::getSkillId).collect(Collectors.toList()), ",");
        }
        return "";
    }
}
