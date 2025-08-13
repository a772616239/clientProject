package model.battle.pve;


import lombok.extern.slf4j.Slf4j;
import protocol.Battle.BattleSubTypeEnum;

@Slf4j
public class EpisodeNormalPveBattleController extends BaseEpisodePveBattleController {


    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_EpisodeGeneral;
    }

}
