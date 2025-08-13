package server.event;

import java.util.concurrent.ConcurrentHashMap;

import server.event.command.LockedCommandQueue;

/**
 * 一个TasksQueue的池子，该池子拥有多个TasksQueue
 * 
 * @author Administrator
 * @param <K>
 * @param <V>
 */
public class OrderedQueuePool<K, V> {

	/**
	 * 保存TasksQueue的map
	 */
	private final ConcurrentHashMap<K, LockedCommandQueue<V>> map = new ConcurrentHashMap<>();

	/**
	 * 获得任务队列.
	 * 用指定key获取一个TasksQueue，
	 * 如果该Key在改池子中不存在对应的TasksQueue，那么创建一个新的TasksQueue，
	 * 并且放进pool中.
	 * 
	 * @param key
	 * @return
	 */
	public LockedCommandQueue<V> getCommandQueue(K key) {
		LockedCommandQueue<V> queue = this.map.get(key);
		if(queue == null) {
			synchronized (this.map) {
				LockedCommandQueue<V> queue1 = this.map.get(key);
				if (queue1 == null) {
					queue1 = new LockedCommandQueue<V>();
					this.map.put(key, queue1);
				}
				return queue1;
			}
		}else {
			return queue;
		}
	}

	/**
	 * 获得全部任务队列
	 * @return
	 */
	public ConcurrentHashMap<K, LockedCommandQueue<V>> getCommandQueue() {
		return this.map;
	}

	/**
	 * 移除任务队列
	 * @param key
	 * @return
	 */
	public void removeCommandQueue(K key) {
		this.map.remove(key);
	}
}
