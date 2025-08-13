package model.battle.preInfo;

import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolBattleResult;
import protocol.Battle;
import protocol.PrepareWar;

public class PatrolPreWarInfo extends AbstractPreWarInfo {

    public PatrolPreWarInfo() {
        typeEnum = Battle.BattleSubTypeEnum.BSTE_Patrol;
    }

    public PrepareWar.SC_PreWarInfo.Builder preBattleInfo(String playerIdx, PrepareWar.CS_PreWarInfo req) {
        protocol.PrepareWar.SC_PreWarInfo.Builder result = protocol.PrepareWar.SC_PreWarInfo.newBuilder();
        if (Battle.BattleSubTypeEnum.BSTE_Patrol == req.getType()) {
            int irritate;
            try {
                irritate = Integer.parseInt(req.getParamList(0));
            } catch (Exception e) {
                return result;
            }
            PatrolBattleResult patrolBattleResult = PatrolServiceImpl.getInstance().preBattleInfo(playerIdx, irritate);
            result.addAllBuffs(patrolBattleResult.getDebuffList());
            result.setExtendProp(patrolBattleResult.getMonsterExProperty());
            return result;
        }
        return result;
    }
}
