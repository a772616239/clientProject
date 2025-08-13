package server.event.command;

public interface ICommand extends Runnable{

	/**
	 * 执行动作
	 */
	void doAction();
}
