package model.battle.preInfo;

import lombok.Getter;
import protocol.PrepareWar;

@Getter
public abstract class AbstractPreWarInfo {

    protected protocol.Battle.BattleSubTypeEnum typeEnum;

    public abstract PrepareWar.SC_PreWarInfo.Builder preBattleInfo(String playerIdx, PrepareWar.CS_PreWarInfo req);
}
