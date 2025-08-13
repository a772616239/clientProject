package model.crazyDuel.entity;

import java.util.List;
import protocol.Battle;
import protocol.CrayzeDuel;

public class CrazyDuelTeamData {
    private CrayzeDuel.CrazyDuelTeam crazyDuelTeam;
    private List<Battle.BattlePetData> battleData;

    public CrayzeDuel.CrazyDuelTeam getCrazyDuelTeam() {
        return crazyDuelTeam;
    }

    public void setCrazyDuelTeam(CrayzeDuel.CrazyDuelTeam crazyDuelTeam) {
        this.crazyDuelTeam = crazyDuelTeam;
    }

    public List<Battle.BattlePetData> getBattleData() {
        return battleData;
    }

    public void setBattleData(List<Battle.BattlePetData> battleData) {
        this.battleData = battleData;
    }
}
