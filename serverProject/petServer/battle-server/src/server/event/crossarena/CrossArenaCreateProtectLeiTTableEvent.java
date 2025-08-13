package server.event.crossarena;

import model.crossarena.CrossArenaManager;

public class CrossArenaCreateProtectLeiTTableEvent extends CrossArenaEventAbstractCommand {

    private int scienceId;
    private String playerIdx;
    private int robotDifficult;

    public int getRobotDifficult() {
        return robotDifficult;
    }

    public void setRobotDifficult(int robotDifficult) {
        this.robotDifficult = robotDifficult;
    }

    public int getScienceId() {
        return scienceId;
    }

    public void setScienceId(int scienceId) {
        this.scienceId = scienceId;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    @Override
    public void doAction() {
        CrossArenaManager.getInstance().createProtectLeiTTable( scienceId, playerIdx,robotDifficult);
    }
}
