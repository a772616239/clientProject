package model.training.bean;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainRankMap {

	private Map<String, TrainRankSortInfo> jifens = new ConcurrentHashMap<String, TrainRankSortInfo>();

	private List<TrainRankSortInfo> sort = new LinkedList<TrainRankSortInfo>();
	
	/**
	 * 刷新积分
	 * @param pid
	 * @param jifen
	 */
	public void refJifen(String pid, int star, int jifen, long time) {
		TrainRankSortInfo trsi = jifens.computeIfAbsent(pid, k -> new TrainRankSortInfo(pid));
		if (trsi.getJifen() == jifen) {
			return;
		}
		trsi.setStar(star);
		trsi.setJifen(jifen);
		trsi.setRefTime(time);
	}
	
	public void refJifenAndSort(String pid, int star, int jifen, long time) {
		refJifen(pid, star, jifen, time);
		sort();
	}
	
	/**
	 * 积分排序
	 */
	public void sort() {
		List<TrainRankSortInfo> sortTemp = new LinkedList<TrainRankSortInfo>();
		sortTemp.addAll(jifens.values());
		Collections.sort(sortTemp, new Comparator<TrainRankSortInfo>() {
			public int compare(TrainRankSortInfo arg0, TrainRankSortInfo arg1) {
				if (arg0.getJifen() < arg1.getJifen()) {
					 return 1;
				} else if (arg0.getJifen() == arg1.getJifen()) {
					if (arg0.getRefTime() < arg1.getRefTime()) {  
	                    return -1;
	                } else {
	                	return 1;
	                }
				} else {
					return -1;
				}
			}
		});
		this.sort = sortTemp;
	}

	public List<TrainRankSortInfo> getSort() {
		return sort;
	}
	
	public int getRankById(String pid) {
		if (sort.isEmpty()) {
			return 0;
		}
		int i = 1;
		for (TrainRankSortInfo trsi : sort) {
			if (trsi.getPlayerId().equals(pid)) {
				return i;
			}
			++i;
		}
		return 0;
	}
	
}
