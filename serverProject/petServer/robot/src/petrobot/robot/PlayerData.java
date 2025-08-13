package petrobot.robot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import petrobot.system.mine.RobotMine;
import petrobot.system.mistForest.RobotMistForest;
import petrobot.system.thewar.RobotTheWarData;
import protocol.Activity.ClientActivity;
import protocol.Activity.ClientSubMission;
import protocol.Activity.RefreshActivity;
import protocol.Activity.SC_ClaimNovice;
import protocol.Arena.SC_ClaimArenaInfo;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.FriendInfo;
import protocol.Mail.MailInfo;
import protocol.MainLine.MainLineProgress;
import protocol.MonthCard.MonthCardInfo;
import protocol.Patrol.PatrolMap;
import protocol.Patrol.PatrolStatus;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.PetFragment;
import protocol.PetMessage.PetMission;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetBagInit;
import protocol.PlayerInfo.SC_GlobalAddition;
import protocol.PlayerInfo.SC_PlayerBaseInfo;
import protocol.PrepareWar.SC_TeamsInfo;
import protocol.PrepareWar.TeamInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.ResourceCopy.ResCopy;
import protocol.Shop.ShopInfo;
import protocol.Shop.ShopTypeEnum;
import protocol.TargetSystem.AchievmentPro;
import protocol.TargetSystem.SC_GetFeatsInfo;
import protocol.TargetSystem.TargetMission;

/**
 * 缓存一些机器人需要用的缓存数据，比如坐标，任务数据等
 * @author Administrator
 *
 */
@Getter
@Setter
public class PlayerData {
	private SC_PlayerBaseInfo.Builder baseInfo;
	private SC_GlobalAddition.Builder globalAddition;
	private Map<String, MailInfo> mailInfoMap = new ConcurrentHashMap<>();
	private Map<Integer, Long> itemMap = new ConcurrentHashMap<>();
	private SC_PetBagInit petBag;
	private List<Rune> petRuneList = new ArrayList<>();
	private List<PetFragment> petFragmentList = new ArrayList<>();
	private List<Integer> petCollectionIdList = new ArrayList<>();
	private long battleId;

	private boolean patrolFinish = true;

	/** 好友 **/
	private Map<String, FriendInfo> ownedFriend = new ConcurrentHashMap<>();
	private Map<String, FriendBaseInfo> applyMap = new ConcurrentHashMap<>();
	private Map<String, FriendBaseInfo> recommendMap = new ConcurrentHashMap<>();

	private int curSpireLv;

	/**主线**/
	private MainLineProgress.Builder mainLinePro;
	private int curOnHookNode;
	private long startOnHookTime;
	private int todayQuickTimes;

	/**资源副本**/
	private List<ResCopy> resCopies;

	/**商店**/
	private Map<ShopTypeEnum, ShopInfo> shopInfo = new HashMap<>();

	/**目标系统**/
	private List<TargetMission> dailyMission = new ArrayList<>();
	private List<AchievmentPro> achievementMission = new ArrayList<>();

	/**
	 * 宠物委托
	 */
	private List<PetMission> petMissionList = new ArrayList<>();
	private List<AcceptedPetMission> acceptedPetMissionList = new ArrayList<>();

	/**
	 * 勇气试炼
	 */
	private ChallengeProgress braveChallengeProgress;

	/**
	 * 秘境探索
	 */
	private PatrolMap patrolMap;
	private PatrolStatus patrolStatus;

	/**编队**/
	private int unlockPosition;
	private List<TeamInfo> teamsInfo = new ArrayList<>();

	/**
	 * 活动
	 */

	Map<Long, ClientActivity> clientActivities = new HashMap<>();

	/**
	 * 竞技场
	 * @param clientActivities
	 */
	private SC_ClaimArenaInfo.Builder arenaInfo;

	private List<MonthCardInfo> monthCardInfo;

	private SC_GetFeatsInfo featsInfos;

	public void addAllClientActivity(List<ClientActivity> clientActivities) {
		if (clientActivities == null) {
			return;
		}
		for (ClientActivity activity : clientActivities) {
			this.clientActivities.put(activity.getActicityId(), activity);
		}
	}

	public void updateActivity(ClientActivity newActivity) {
		clientActivities.merge(newActivity.getActicityId(), newActivity, (oldVal, newVal) -> newVal);
	}

	public void refreshActivity(RefreshActivity refreshActivity) {
		ClientActivity activity = clientActivities.get(refreshActivity.getActivityId());
		if (activity == null) {
			return;
		}
		ClientSubMission subMission = activity.getMissionLists(refreshActivity.getIndex());
		ClientSubMission.Builder subMissionBuilder;
		if (subMission == null) {
			subMissionBuilder = ClientSubMission.newBuilder();
		} else {
			subMissionBuilder = subMission.toBuilder();
		}
		subMissionBuilder.setIndex(refreshActivity.getIndex());
		subMissionBuilder.setPlayerPro(refreshActivity.getNewPro());
		subMissionBuilder.setStatus(refreshActivity.getNewStatus());
		ClientActivity.Builder newActivity = activity.toBuilder().setMissionLists(refreshActivity.getIndex(), subMissionBuilder);
		clientActivities.put(newActivity.getActicityId(), newActivity.build());
	}

	public void addMail(MailInfo mailInfo) {
		if (mailInfo == null) {
			return;
		}
		mailInfoMap.put(mailInfo.getMailIdx(), mailInfo);
	}

	public void addMailList(List<MailInfo> mailList) {
		if (mailList == null) {
			return;
		}
		for (MailInfo mailInfo : mailList) {
			addMail(mailInfo);
		}
	}

	public void putItem(int cfgId, long count) {
		if (count <= 0) {
			itemMap.remove(cfgId);
		}
		itemMap.put(cfgId, count);
	}

	public void putAllItem(Map<Integer, Integer> items) {
		for (Entry<Integer, Integer> entry : items.entrySet()) {
			putItem(entry.getKey(), entry.getValue());
		}
	}

	public void addOwnedFriend(List<FriendInfo> ownedList) {
		if (ownedList == null) {
			return;
		}
		for (FriendInfo friendInfo : ownedList) {
			addOwnedFriend(friendInfo);
		}
	}

	public void addOwnedFriend(FriendInfo friendInfo) {
		if (friendInfo == null) {
			return;
		}
		ownedFriend.put(friendInfo.getBaseInfo().getPlayerIdx(), friendInfo);
	}

	public void addApplyFriend(List<FriendBaseInfo> applyList) {
		if (applyList == null) {
			return;
		}
		for (FriendBaseInfo friendInfo : applyList) {
			applyMap.put(friendInfo.getPlayerIdx(), friendInfo);
		}
	}

	public void addRecommend(List<FriendBaseInfo> recommendList) {
		if (recommendList == null) {
			return;
		}
		for (FriendBaseInfo friendInfo : recommendList) {
			recommendMap.put(friendInfo.getPlayerIdx(), friendInfo);
		}
	}

	public void removeOwned(String removePlayerIdx) {
		ownedFriend.remove(removePlayerIdx);
	}

	public void addShopInfo(ShopInfo shopInfo) {
		if (shopInfo == null) {
			return;
		}

		this.shopInfo.put(shopInfo.getShopType(), shopInfo);
	}

	public ShopInfo getShopInfoByType(int shopType) {
		return this.shopInfo.get(ShopTypeEnum.forNumber(shopType));
	}

	public void addAllDailyMission(List<TargetMission> missions) {
		if (missions == null) {
			return;
		}
		dailyMission.addAll(missions);
	}

	public void addAllAchievementPro(List<AchievmentPro> achievement) {
		if (achievement == null) {
			return;
		}

		achievementMission.addAll(achievement);
	}

	public void replaceDailyMission(List<TargetMission> missionProList) {
		if (missionProList == null || missionProList.isEmpty()) {
			return;
		}
		Set<Integer> removeId = new HashSet<>();
		for (TargetMission targetMission : missionProList) {
			removeId.add(targetMission.getCfgId());
		}

		dailyMission.removeIf(next -> removeId.contains(next.getCfgId()));
	}

	public void replaceAchievement(List<AchievmentPro> ProList) {
		if (ProList == null || ProList.isEmpty()) {
			return;
		}
		Set<Integer> removeId = new HashSet<>();
		for (AchievmentPro pro : ProList) {
			removeId.add(pro.getCfgId());
		}

		achievementMission.removeIf(next -> removeId.contains(next.getCfgId()));
	}

	public void setTeamsInfo(SC_TeamsInfo.Builder teamsInfo) {
		if (teamsInfo == null) {
			return;
		}
		this.teamsInfo.addAll(teamsInfo.getTeamsInfoList());
		this.teamsInfo.add(teamsInfo.getCourgeTeam());
		this.teamsInfo.addAll(teamsInfo.getMineTeamsList());
		this.teamsInfo.add(teamsInfo.getMistTeam());

		this.unlockPosition = teamsInfo.getUnlockPosition();
	}

	public void refreshTeam(TeamInfo teamInfo) {
		if (teamInfo == null) {
			return;
		}
		for (TeamInfo info : teamsInfo) {
			if (info.getTeamNum() == teamInfo.getTeamNum()) {
				teamsInfo.remove(info);
				break;
			}
		}
		teamsInfo.add(teamInfo);
	}

	public TeamInfo getTeamInfoByTeamNum(TeamNumEnum typeNum) {
		if (teamsInfo == null) {
			return null;
		}
		for (TeamInfo info : teamsInfo) {
			if (info.getTeamNum() == typeNum) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 新手积分======================================start
	 */
	private Map<Integer, TargetMission> noviceMission = new HashMap<>();

	public void setNovice(SC_ClaimNovice.Builder builder) {
		if (builder == null) {
			return;
		}
		List<TargetMission> missionProList = builder.getNovice().getMissionProList();
		if (!missionProList.isEmpty()){
			for (TargetMission mission : missionProList) {
				this.noviceMission.put(mission.getCfgId(), mission);
			}
		}
	}

	public void updateNoviceMissionPro(List<TargetMission> newPro) {
		if (newPro == null) {
			return;
		}

		for (TargetMission mission : newPro) {
			noviceMission.put(mission.getCfgId(), mission);
		}
	}

	/**
	 * 新手积分======================================end
	 */


	/**
	 * 迷雾森林======================================start
	 */
	RobotMistForest robotMistForest;

	/**
	 * 迷雾森林======================================end
	 */

	RobotMine mineInfo;


	/**
	 * 远征======================================start
	 */
	RobotTheWarData robotWarData;
	/**
	 * 远征======================================end
	 */
}

