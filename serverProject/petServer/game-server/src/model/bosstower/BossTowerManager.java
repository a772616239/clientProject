package model.bosstower;

import java.util.HashMap;
import java.util.Map;

import entity.UpdateDailyData;
import protocol.BossTower.EnumBossTowerDifficult;

/**
 * @author huhan
 * @date 2020/07/29
 */
public class BossTowerManager implements UpdateDailyData {
	private static BossTowerManager instance;

	public static BossTowerManager getInstance() {
		if (instance == null) {
			synchronized (BossTowerManager.class) {
				if (instance == null) {
					instance = new BossTowerManager();
				}
			}
		}
		return instance;
	}

	private BossTowerManager() {
	}

	/**
	 * <BossPrefixType, >
	 */
	private final Map<Integer, BossTowerBuffContainer> prefixMap = new HashMap<>();

	public boolean init() {
		return true;
	}

	/**
	 * 刷新每日数据
	 */
	@Override
	public synchronized void updateDailyData() {

	}

	/**
	 * 此方法仅限gm调用
	 *
	 * @param difficult
	 * @param buffId
	 * @return
	 */
	public boolean setBuff(EnumBossTowerDifficult difficult, int... buffId) {

		return false;
	}
}