package model.training.bean;
/*
*@author Hammer
*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cfg.TrainingShopObject;

public class TrainShopData {
	private Map<Integer, List<TrainingShopObject>> map = new HashMap<>();

	public Map<Integer, List<TrainingShopObject>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<TrainingShopObject>> map) {
		this.map = map;
	}


}
