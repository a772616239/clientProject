package petrobot.util;

import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;

public class TeamUtil {
    public static TeamTypeEnum getTeamType(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return TeamTypeEnum.TTE_Null;
        }
        if (teamNum.getNumber() >= TeamNumEnum.TNE_Team_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Team_5_VALUE) {
            return TeamTypeEnum.TTE_Common;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Courge_VALUE) {
            return TeamTypeEnum.TTE_CourageTrial;
        } else if (teamNum.getNumber() >= TeamNumEnum.TNE_Mine_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Mine_3_VALUE) {
            return TeamTypeEnum.TTE_Mine;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_FriendHelp_VALUE) {
            return TeamTypeEnum.TTE_FriendHelp;
        }

        return TeamTypeEnum.TTE_Null;
    }
}
