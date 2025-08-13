package model.matchArena;

import java.util.List;
import lombok.Data;
import protocol.Battle;
import protocol.MatchArenaDB;
import protocol.ServerTransfer;
import util.LogUtil;

@Data
public class NormalMatchPlayer {
    private String playerIdx;
    private int fromSvrIndex;
    private List<Integer> petCfgIds;
    private Battle.PlayerBaseInfo playerBaseInfo;
    private MatchArenaDB.MatchArenaTeamInfo teamInfo;
    private List<Battle.BattlePetData> battlePetData;

    public ServerTransfer.PvpBattlePlayerInfo buildPvpPlayerInfo(int camp, int battlePetLimit) {
        if (this.playerBaseInfo == null || this.teamInfo == null) {
            LogUtil.error("model.matchArena.MatchArenaPlayer.buildPvpPlayerInfo, player info is null");
            return null;
        }
        ServerTransfer.PvpBattlePlayerInfo.Builder resultBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();
        resultBuilder.setPlayerInfo(this.playerBaseInfo);
        resultBuilder.setFromSvrIndex(getFromSvrIndex());
        resultBuilder.setCamp(camp);
        resultBuilder.addAllPetList(battlePetData);
        resultBuilder.addAllPlayerSkillIdList(this.teamInfo.getPlayerSkillIdListList());
        resultBuilder.addExtendProp(Battle.ExtendProperty.newBuilder().setCamp(camp).setBattlePetLimit(battlePetLimit).build());
        return resultBuilder.build();
    }
}
