package model.cp.broadcast;

import common.IdGenerator;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class CpCopyActive implements Serializable {
    private static final long serialVersionUID = -3655282365786543103L;
    private List<String> playerIdx;
    private String msgId;

    public CpCopyActive() {
        this.msgId = IdGenerator.getInstance().generateId();
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    private List<String> alreadySettlePlayerIdx;

    public void addAlreadySettlePlayerIdx(String playerIdx) {
        alreadySettlePlayerIdx.add(playerIdx);
    }

    public List<String> getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(List<String> playerIdx) {
        this.playerIdx = playerIdx;
    }

    public void addAllSettlePlayerIdx(Set<String> localPlayerIdx) {
        alreadySettlePlayerIdx.addAll(localPlayerIdx);

    }

    public List<String> getAlreadySettlePlayerIdx() {
        return alreadySettlePlayerIdx;
    }

    public void setAlreadySettlePlayerIdx(List<String> alreadySettlePlayerIdx) {
        this.alreadySettlePlayerIdx = alreadySettlePlayerIdx;
    }

    public boolean allSettle() {
        return playerIdx.size() == alreadySettlePlayerIdx.size();
    }
}
