package model.cp.broadcast;

import common.IdGenerator;
import java.io.Serializable;
import java.util.List;
import model.cp.entity.CpTeamPublish;
import server.handler.cp.CpFunctionUtil;

public class CpTeamUpdate implements Serializable {
    private static final long serialVersionUID = 6467927868960042571L;
    private String msgId;

    private int teamId;

    private List<String> members;

    public CpTeamUpdate() {
    }

    public CpTeamUpdate(CpTeamPublish team) {
        this.msgId = IdGenerator.getInstance().generateId();
        this.teamId = team.getTeamId();
        this.members = CpFunctionUtil.findPlayerIds(team.getMembers());
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
