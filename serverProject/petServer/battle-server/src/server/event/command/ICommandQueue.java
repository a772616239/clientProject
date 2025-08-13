package server.event.command;


public interface ICommandQueue<V> {

	/**
	 * 下一执行命令
	 * 
	 * @return
	 */
	public V poll();

	/**
	 * 增加执行指令
	 * 
	 * @param command
	 * @return
	 */
	public boolean offer(V value);

	/**
	 * 清理
	 */
	public void clear();

	/**
	 * 获取指令数量
	 * 
	 * @return
	 */
	public int size();

	public boolean isProcessingCompleted();

	public void setProcessingCompleted(boolean processingCompleted);
}
