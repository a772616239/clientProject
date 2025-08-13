package model.cp.broadcast;

import common.IdGenerator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import server.handler.cp.CpFunctionUtil;

public class CpPlayerLeaveCopy implements Serializable {

    private static final long serialVersionUID = 1580807311804017416L;

    private String msgId;

    private String leavePlayerIdx;

    private boolean win;

    public CpPlayerLeaveCopy(String leavePlayerIdx, List<String> members, boolean win) {
        this.msgId = IdGenerator.getInstance().generateId();
        this.leavePlayerIdx = leavePlayerIdx;
        this.members.addAll(CpFunctionUtil.findPlayerIds(members));
        this.win = win;
    }

    public CpPlayerLeaveCopy() {
    }

    private List<String> members = new ArrayList<>();
    private List<String> alreadySettlePlayerIdx = new LinkedList<>();

    public void addAllSettlePlayerIdx(Set<String> localPlayerIdx) {
        this.alreadySettlePlayerIdx.addAll(localPlayerIdx);

    }

    public boolean allSettle() {
        return alreadySettlePlayerIdx.size() >= members.size();
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getLeavePlayerIdx() {
        return leavePlayerIdx;
    }

    public void setLeavePlayerIdx(String leavePlayerIdx) {
        this.leavePlayerIdx = leavePlayerIdx;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getAlreadySettlePlayerIdx() {
        return alreadySettlePlayerIdx;
    }

    public void setAlreadySettlePlayerIdx(List<String> alreadySettlePlayerIdx) {
        this.alreadySettlePlayerIdx = alreadySettlePlayerIdx;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }
}
