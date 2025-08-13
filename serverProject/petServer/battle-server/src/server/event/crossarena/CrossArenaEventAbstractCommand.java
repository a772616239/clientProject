package server.event.crossarena;

import server.event.command.BaseCommand;

public abstract class CrossArenaEventAbstractCommand extends BaseCommand {

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    protected int tableId;

    public void run() {
        try {
            doAction();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
