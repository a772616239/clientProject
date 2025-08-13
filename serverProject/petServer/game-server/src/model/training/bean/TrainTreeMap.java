package model.training.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cfg.TrainingLuckHideBuffObject;
import cfg.TrainingMap;
import cfg.TrainingMapObject;
import cfg.TrainingPoint;
import cfg.TrainingPointObject;
import util.LogUtil;

/**
 * @author Autumn 存放点位数据结构
 */
public class TrainTreeMap {

	private int mapid;

	private Set<Integer> startIds = new HashSet<>();
	private int jifenLimt = 0;

	private Map<Integer, TrainPoint> allPoint = new HashMap<Integer, TrainPoint>();

	private List<Integer> allReset = new ArrayList<Integer>();

	private List<Integer> change2Two = new ArrayList<>();

	private Map<Integer, List<Integer>> unlockLevelMap = new HashMap<>();

	private Map<Integer, Integer> eventIdType = new HashMap<>();

	private Map<Integer, List<TrainingLuckHideBuffObject>> hiddBuff = new HashMap<>();

	private Map<Integer, List<Integer>> groupMap = new HashMap<>();

	private Map<Integer, List<Integer>> groupAllMap = new HashMap<>();

	private int maxLevel = 0;

	private int totalPointCount = 0; // 总节点数,用于结算计算进度

	public TrainTreeMap(int mapid) {
		this.mapid = mapid;
	}

	public void addPoint(int pid, int[] sons, boolean isReset) {
		if (isReset && !allReset.contains(pid)) {
			allReset.add(pid);
		}
		TrainPoint tpFather = allPoint.get(pid);
		if (null == tpFather) {
			tpFather = new TrainPoint(pid);
			allPoint.put(pid, tpFather);
		}
		// 创建一个新点，添加该点子节点
		for (int sonId : sons) {
			tpFather.getSon().add(sonId);
			// 添加子节点生成数据
			TrainPoint tpSon = allPoint.get(sonId);
			if (null == tpSon) {
				tpSon = new TrainPoint(sonId);
				allPoint.put(sonId, tpSon);
			}
			if (!tpSon.getFather().contains(pid)) {
				tpSon.getFather().add(pid);
			}
		}
	}

	public boolean checkPoint() {
		Set<Integer> startIds = new HashSet<>();
		for (TrainPoint tp : allPoint.values()) {
			TrainingPointObject obj = TrainingPoint.getByPointid(tp.getPid());
			if (obj != null) {
				if (!groupAllMap.containsKey(obj.getPointgroup())) {
					groupAllMap.put(obj.getPointgroup(), new ArrayList<>());
				}
				groupAllMap.get(obj.getPointgroup()).add(tp.getPid());
			}
			if (tp.getFather().isEmpty() && tp.getPid() < 10000000) {
//				if (startId == -6513554) {
//					startId = tp.getPid();
//				} else {
//					LogUtil.error("训练场数据初始化异常,地图=" + mapid + ",有多个起点，");
//					return false;
//				}
				startIds.add(tp.getPid());
			}
			if (tp.getFather().isEmpty() && tp.getSon().isEmpty() && tp.getPid() < 10000000) {
				LogUtil.error("训练场数据初始化异常,地图=" + mapid + ",有独立点，" + tp.getPid());
				return false;
			}

		}
		this.startIds = startIds;
//		if (!allReset.contains(startId)) {
//			allReset.add(startId);
//		}

		for (Integer startId : this.startIds) {
			TrainPoint tpstart = allPoint.get(startId);
			createAllFather(tpstart);
		}

		List<TrainPoint> bossAndBranch = new ArrayList<>();
		for (TrainPoint tp : allPoint.values()) {
			boolean add = false;
			if (tp.getSon().size() > 1 && !this.startIds.contains(tp.getPid())) {
				if (change2Two.contains(tp.getPid())) {
					List<Integer> groupList = new ArrayList<>();
					for (Integer i : tp.getSon()) {
						TrainingPointObject obj = TrainingPoint.getByPointid(i);
						if (obj != null) {
							groupList.add(obj.getPointgroup());
						}
					}
					groupMap.put(tp.getPid(), groupList);
				}
				bossAndBranch.add(tp);
				add = true;
			}
			TrainingPointObject obj = TrainingPoint.getByPointid(tp.getPid());
			if (obj != null && obj.getType() == TrainPointType.BOSS) {
				if (!add) {
					bossAndBranch.add(tp);
				}
			}
			// 生成可以返回的点
//			for (int pid : tp.getAllfather()) {
//				if (allReset.contains(pid)) {
//					tp.getCanReset().add(pid);
//				}
//			}
//			System.err.println(tp.getPid() + "_" + tp.getAllfather());
		}
		bossAndBranch.sort(new Comparator<TrainPoint>() {
			@Override
			public int compare(TrainPoint o1, TrainPoint o2) {
				return o1.getPid() - o2.getPid();
			}
		});

		// boss点到前方最近的分支点之间的点获得的buff都只能针对这个boss
		// 从最后一个boss点开始倒叙遍历设置节点所属boss
		for (TrainPoint point : bossAndBranch) {
			TrainingPointObject obj = TrainingPoint.getByPointid(point.getPid());
			if (obj == null) {
				continue;
			}
			for (Integer i : point.getAllfather()) {
				TrainPoint trainPoint = allPoint.get(i);
				if (trainPoint != null) {
					trainPoint.setNextBoss(point.getPid());
				}
			}
		}
		for (TrainPoint tp : allPoint.values()) {
			for (TrainPoint point : bossAndBranch) {
				if (tp.getAllfather().contains(point.getPid())) {
					tp.setBranchPoint(1);
				}
			}

		}
		return true;
	}
//					6	8		11	13
//1	2	3	4	5			10			15
//					7	9		12	14

	public Map<Integer, List<Integer>> getGroupAllMap() {
		return groupAllMap;
	}

	/**
	 * 生成全部父节点
	 * 
	 * @param tpstart
	 */
	public void createAllFather(TrainPoint tpstart) {
		for (int ent : tpstart.getSon()) {
			TrainPoint son = allPoint.get(ent);
			son.getAllfather().addAll(tpstart.getAllfather());
			son.getAllfather().addAll(son.getFather());
			createAllFather(son);
		}
	}

	public Map<Integer, List<Integer>> getGroupMap() {
		return groupMap;
	}

	public List<Integer> getCanResetPoint(int pointCurr) {
		List<Integer> can = new ArrayList<Integer>();
		TrainPoint tp = allPoint.get(pointCurr);
		if (null == tp) {
			return can;
		}
		return tp.getCanReset();
	}

	public boolean hasPoint(int pid) {
		return allPoint.containsKey(pid);
	}

	public boolean hasNextPoint(int pid, int nextId) {
		if (allPoint.containsKey(pid) && allPoint.containsKey(nextId)) {
			return allPoint.get(pid).getSon().contains(nextId);
		} else {
			return false;
		}
	}

	public int getMapid() {
		return mapid;
	}

	public void setMapid(int mapid) {
		this.mapid = mapid;
	}

	public Set<Integer> getStartIds() {
		return startIds;
	}

	public void setStartIds(Set<Integer> startIds) {
		this.startIds = startIds;
	}

	public void setAllReset(List<Integer> allReset) {
		this.allReset = allReset;
	}

	public int getJifenLimt() {
		return jifenLimt;
	}

	public void setJifenLimt(int jifenLimt) {
		this.jifenLimt = jifenLimt;
	}

	public Map<Integer, TrainPoint> getAllPoint() {
		return allPoint;
	}

	public void setAllPoint(Map<Integer, TrainPoint> allPoint) {
		this.allPoint = allPoint;
	}

	public List<Integer> getAllReset() {
		return allReset;
	}

	public Map<Integer, List<Integer>> getUnlockLevelMap() {
		return unlockLevelMap;
	}

	public void setUnlockLevelMap(Map<Integer, List<Integer>> unlockLevelMap) {
		int level = 0;
		for (Integer i : unlockLevelMap.keySet()) {
			if (level == 0) {
				level = i;
			} else {
				if (level < i) {
					level = i;
				}
			}
		}
		this.maxLevel = level;
		this.unlockLevelMap = unlockLevelMap;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getTotalPointCount() {
		return totalPointCount;
	}

	public void setTotalPointCount(int totalPointCount) {
		this.totalPointCount = totalPointCount;
	}

	public Map<Integer, Integer> getEventIdType() {
		return eventIdType;
	}

	public void setEventIdType(Map<Integer, Integer> eventIdType) {
		this.eventIdType = eventIdType;
	}

	public Map<Integer, List<TrainingLuckHideBuffObject>> getHiddBuff() {
		return hiddBuff;
	}

	public void setHiddBuff(Map<Integer, List<TrainingLuckHideBuffObject>> hiddBuff) {
		this.hiddBuff = hiddBuff;
	}

	public List<Integer> getChange2Two() {
		return change2Two;
	}

	public void setChange2Two(List<Integer> change2Two) {
		this.change2Two = change2Two;
	}

	public int getReportCard(int jifen) {
		TrainingMapObject mapCfg = TrainingMap.getByMapid(getMapid());
		if (mapCfg != null) {
			int[] tmp = mapCfg.getReportcard();
			int level = 1;
			for (Integer i : tmp) {
				if (jifen >= i) {
					return level;
				}
				level++;
			}
		}
		return 0;
	}
}
