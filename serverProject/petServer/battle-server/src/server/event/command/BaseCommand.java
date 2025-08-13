package server.event.command;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class BaseCommand implements ICommand {
	public ICommandQueue<BaseCommand> getCommandQueue() {
		return commandQueue;
	}

	public void setCommandQueue(ICommandQueue<BaseCommand> commandQueue) {
		this.commandQueue = commandQueue;
	}

	@JSONField(serialize = false)
	private ICommandQueue<BaseCommand> commandQueue;

}
