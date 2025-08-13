package server.event.leitai;

import server.event.command.BaseCommand;

public abstract class LeitaiEventAbstractCommand extends BaseCommand {

    protected int leitaiId;

	public int getLeitaiId() {
		return leitaiId;
	}

	public void setLeitaiId(int leitaiId) {
		this.leitaiId = leitaiId;
	}

    public void run() {
        try {
            doAction();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
