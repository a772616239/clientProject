package model.battle.pvp;

import java.util.ArrayList;
import java.util.List;

import model.crossarena.CrossArenaManager;
import org.apache.commons.lang.math.NumberUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import cfg.CrossArenaPvp;
import common.GameConst;
import common.GameConst.RedisKey;
import common.JedisUtil;
import model.battle.AbstractPvpBattleController;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaUtil;
import model.crossarenapvp.CrossArenaPvpManager;
import model.crossarenapvp.CrossArenaPvpUpdateType;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArena;
import protocol.CrossArenaPvp.CrossArenaPvpRoom;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.TargetSystem;
import util.EventUtil;

public class CrossArenaPvpController extends AbstractPvpBattleController {

	public static final String COSTINDEX = "COSTID";
	public static final String ATTERPOWER = "ATTERPOWER";
	public static final String OWNERPOWER = "OWNPOWER";
	public static final String ROOMID = "ROOMID";

	@Override
	public String getLogExInfo() {
		return null;
	}

	@Override
	protected void battleLog(int winnerCamp, List<Reward> rewardListList) {

	}

	@Override
	protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
		EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetSystem.TargetTypeEnum.TTE_CrossArena_PVP, 1, 0);
		EventUtil.triggerUpdateCrossArenaWeeklyTask(getPlayerIdx(), CrossArena.CrossArenaGradeType.GRADE_QC_Join, 1);
		CrossArenaHonorManager.getInstance().honorVueByKeyAdd(getPlayerIdx(), CrossArenaUtil.HR_PVP_JION, 1);
		if (realResult.getWinnerCamp() == getCamp()) {
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(getPlayerIdx(), CrossArenaUtil.HR_PVP_WIN, 1);
		}
		CrossArenaManager.getInstance().savePlayerDBInfo(getPlayerIdx(), CrossArena.CrossArenaDBKey.QC_JoinTime,1, CrossArenaUtil.DbChangeAdd);
		LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), Common.EnumFunction.QIECUO));
		String roomId = getEnterParam(ROOMID);
		double tarPower = 0;
		double myPower = 0;
		if (getCamp() == 2) {// 房主
			tarPower = getLongEnterParam(ATTERPOWER);
			myPower = getLongEnterParam(OWNERPOWER);
			byte[] hget = JedisUtil.jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
			if (hget != null) {
				CrossArenaPvpRoom room;
				try {
					room = CrossArenaPvpRoom.parseFrom(hget);
					CrossArenaPvpManager.getInstance().noticeRoomPlayer(room, 1);
					CrossArenaPvpManager.getInstance().sendInfoOne(room, "", CrossArenaPvpUpdateType.DEL);
					CrossArenaPvpManager.getInstance().noticeOtherServer(room, "", CrossArenaPvpUpdateType.DEL);
					
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
			JedisUtil.jedis.hdel(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		} else {
			tarPower = getLongEnterParam(OWNERPOWER);
			myPower = getLongEnterParam(ATTERPOWER);
		}
		double per = 1.5;
		if (tarPower > (myPower * per)) {
			CrossArenaHonorManager.getInstance().honorVueFirst(getPlayerIdx(), CrossArenaUtil.HR_FIRST_150WIN);
		}
	}

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_CrossArenaPvp;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_CrossAreanPvp_GET;
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_QIECUO;
	}

	@Override
	public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
		if (battleResult.getWinnerCamp() == getCamp()) {
			int costIndex = NumberUtils.toInt(getEnterParam(COSTINDEX));
			if (costIndex == -1) {
				return new ArrayList<>();
			}
			if (costIndex >= CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume().length || costIndex < 0) {
				return new ArrayList<>();
			}
			Reward reward = RewardUtil.parseReward(CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume()[costIndex]);
			reward = reward.toBuilder().setCount(reward.getCount() * 2).build();
			ArrayList<Reward> rewardList = new ArrayList<>();
			rewardList.add(reward);

			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(getPlayerIdx(), CrossArenaUtil.HR_PVP_GOLD, reward.getCount());
			ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrossAreanPvp_GET);
			RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewardList, reason, false);
			return rewardList;
		} else {
			return new ArrayList<>();
		}
	}

}
