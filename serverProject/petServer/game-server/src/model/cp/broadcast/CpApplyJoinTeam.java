package model.cp.broadcast;

import java.io.Serializable;

public class CpApplyJoinTeam implements Serializable {
    private static final long serialVersionUID = 7738363005540069562L;

    private String msgId;

    private String applyJoinTeamPlayer;

    private String leaderIdx ;


    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getApplyJoinTeamPlayer() {
        return applyJoinTeamPlayer;
    }

    public void setApplyJoinTeamPlayer(String applyJoinTeamPlayer) {
        this.applyJoinTeamPlayer = applyJoinTeamPlayer;
    }

    public String getLeaderIdx() {
        return leaderIdx;
    }

    public void setLeaderIdx(String leaderIdx) {
        this.leaderIdx = leaderIdx;
    }
}
