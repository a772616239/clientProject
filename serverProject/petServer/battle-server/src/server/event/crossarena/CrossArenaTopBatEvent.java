package server.event.crossarena;

import model.crossarena.CrossArenaTopManager;

public class CrossArenaTopBatEvent extends CrossArenaEventAbstractCommand {

    private long roomId;

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getWinCamp() {
        return winCamp;
    }

    public void setWinCamp(int winCamp) {
        this.winCamp = winCamp;
    }

    private int winCamp;

    @Override
    public void doAction() {
        CrossArenaTopManager.getInstance().settleMatchTable(getTableId(), roomId, winCamp);
    }
}
