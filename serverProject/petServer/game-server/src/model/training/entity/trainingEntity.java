/**
 *created by tool DAOGenerate
 */
package model.training.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst.EventType;
import model.obj.BaseObj;
import model.team.util.TeamsUtil;
import model.training.dbCache.trainingCache;
import protocol.TrainingDB.TrainDBData;
import protocol.TrainingDB.TrainDBMap;
import protocol.TrainingDB.TrainDBMap.Builder;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class trainingEntity extends BaseObj {

	public String getClassType() {
		return "trainingEntity";
	}

	/**
	 * 主键
	 */
	private String idx;

	/**
	 * 玩家id
	 */
	private String playeridx;

	/**
	 * 等待开启的地图
	 */
	private String opens;

	/**
	 * 玩家当前状态
	 */
	private byte[] infos;

	/**
	 * 完成情况
	 */
	private String finishinfo = "";

	/**
	 * 鑾峰緱主键
	 */
	public String getIdx() {
		return idx;
	}

	/**
	 * 璁剧疆主键
	 */
	public void setIdx(String idx) {
		this.idx = idx;
	}

	/**
	 * 鑾峰緱玩家id
	 */
	public String getPlayeridx() {
		return playeridx;
	}

	/**
	 * 璁剧疆玩家id
	 */
	public void setPlayeridx(String playeridx) {
		this.playeridx = playeridx;
	}

	/**
	 * 鑾峰緱等待开启的地图
	 */
	public String getOpens() {
		return opens;
	}

	/**
	 * 璁剧疆等待开启的地图
	 */
	public void setOpens(String opens) {
		this.opens = opens;
	}

	/**
	 * 鑾峰緱玩家当前状态
	 */
	public byte[] getInfos() {
		return infos;
	}

	/**
	 * 璁剧疆玩家当前状态
	 */
	public void setInfos(byte[] infos) {
		this.infos = infos;
	}

	/**
	 * 鑾峰緱完成情况
	 */
	public String getFinishinfo() {
		return finishinfo;
	}

	/**
	 * 璁剧疆完成情况
	 */
	public void setFinishinfo(String finishinfo) {
		this.finishinfo = finishinfo;
	}

	public String getBaseIdx() {
		return idx;
	}

	@Override
	public void putToCache() {
		trainingCache.put(this);
	}

	@Override
	public void transformDBData() {
		refresh();
	}

	/*************************** 分割 **********************************/
	private TrainDBData.Builder infoDB;

	public TrainDBData.Builder getInfoDB() {
		if (infoDB == null) {
			infoDB = parseTrainDbData();
		}
		return infoDB;
	}

	public void toBuilder() {
		infoDB = parseTrainDbData();
	}

	public TrainDBData.Builder parseTrainDbData() {
		try {
			if (infos != null) {
				return TrainDBData.parseFrom(infos).toBuilder();
			} else {
				return TrainDBData.newBuilder();
			}
		} catch (InvalidProtocolBufferException e) {
			LogUtil.printStackTrace(e);
			return TrainDBData.newBuilder();
		}
	}

	public TrainDBMap.Builder getCurTrainMap() {
		return getTrainMapByMapId(getInfoDB().getCurrMap());
	}
	public TrainDBMap.Builder getTrainMapByMapId(int mapId) {
		for (int i = 0; i < getInfoDB().getMapsCount(); i++) {
			if (getInfoDB().getMapsBuilder(i).getMapId() == mapId) {
				return getInfoDB().getMapsBuilder(i);
			}
		}
		return null;
	}

	public void refresh() {
		infos = getInfoDB().build().toByteArray();
//		finishinfo = FastJSON.toJSONString(currPath);
	}

	public void updateDailyData() {
		// 过天处理商店数据
//		TrainDBMap tMap = getInfoDB().getMapsOrDefault(infoDB.getCurrMap(), null);
//		if (null == tMap) {
//			return;
//		}
//		TrainDBMap.Builder tMapb = tMap.toBuilder();
//		tMapb.getShopBuyList().clear();
//		// 更新数据
//		setInfoDB(getInfoDB().toBuilder().putMaps(tMapb.getMapId(), tMapb.build()).build());
	}

	/**
	 * 处理副本过期
	 * 
	 * @param curTime
	 */
	public void onTick(long curTime) {
		if (infoDB.getCurrMap() > 0) {
			// 判断副本是否开启，兵是否有新的副本
			for (Builder mapData : getInfoDB().getMapsBuilderList()) {
				if(infoDB.getEndMapMap().containsKey(mapData.getMapId())) {
					continue;
				}
				// 当前副本结束
				if (mapData.getCloseTime() < curTime) {
					getInfoDB().putEndMap(mapData.getMapId(), mapData.getCloseTime());
					refresh();
					// 调用结束发送排行榜奖励
					Event event = Event.valueOf(EventType.ET_TRAIN_RANK_SETTLE, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
					event.pushParam(playeridx, mapData.getMapId());
					EventManager.getInstance().dispatchEvent(event);
				}
			}
		} else {
			int currMapId = 0;
//			long endTime = 0;
			for (TrainDBMap ent : infoDB.getMapsList()) {
				if (curTime > ent.getOpenTime() && curTime < ent.getCloseTime()) {
					currMapId = ent.getMapId();
//					endTime = ent.getCloseTime();
				}
			}
			if (currMapId > 0) {
				getInfoDB().setCurrMap(currMapId);
				refresh();
//				TrainingManager.getInstance().sendTrainOpen(playeridx);
				// 重置成功路劲点后，刷新玩法队伍
				TeamsUtil.updateTeamInfoTrain(playeridx);
			}
		}

	}

}