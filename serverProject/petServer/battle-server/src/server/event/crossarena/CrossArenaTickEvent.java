package server.event.crossarena;

import model.crossarena.CrossArenaManager;

public class CrossArenaTickEvent extends CrossArenaEventAbstractCommand {

    private long time = 0;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    @Override
    public void doAction() {
        if (tableId < 1000) {
    		CrossArenaManager.getInstance().onTickScene(time, tableId);
        } else {
    		CrossArenaManager.getInstance().onTick(time, tableId);
        }
    }
}
