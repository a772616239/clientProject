package common;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.activity.petAvoidance.PetAvoidanceGameManager;
import model.battle.BattleManager;
import model.crossarena.CrossArenaManager;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;

/*
*@author Hammer
*功能互斥
*/
public class FunctionExclusion {

	private static FunctionExclusion instance = new FunctionExclusion();

	private Map<Integer, List<Integer>> typeMap = new HashMap<>();
	private Set<Integer> typeSet = new HashSet<>();

	public static FunctionExclusion getInstance() {
		return instance;
	}

	private FunctionExclusion() {
		// 参数1为被检查功能,可变参数为冲突功能
		addSubTypeMap(ExclusionType.CROSSARENA_PVP, ExclusionType.CROSSARENA_LEITAI_UP, ExclusionType.CRAZY_FIGHT);

		// 全冲突
		Set<Integer> temSet = new HashSet<>();
		Class<ExclusionType> clazz = ExclusionType.class;
		try {
			for (Field field : ExclusionType.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					int num = field.getInt(clazz);
					if (num >= 10000 && num <= 20000) {
						temSet.add(num);
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		addSubTypeSet(temSet);
	}

	/**
	 * 部分冲突检查
	 * 
	 * @param playerId
	 * @param exclusionEnumType
	 * @return
	 */
	public boolean checkExclusionMap(String playerId, int exclusionEnumType) {
		if (!typeMap.containsKey(exclusionEnumType)) {
			return true;
		}
		List<Integer> checkList = typeMap.get(exclusionEnumType);
		for (Integer subType : checkList) {
			if (checkSubType(playerId, subType)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 全冲突检查
	 * 
	 * @param playerId
	 * @return true 没有冲突，false 有冲突
	 */
	public boolean checkExclusionList(String playerId) {
		for (Integer subType : typeSet) {
			if (checkSubType(playerId, subType)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 全冲突检查
	 *
	 * @param playerId
	 * @return 0 没有冲突，>0 有冲突
	 */
	public int checkExclusionAll(String playerId) {
		for (int subType : typeSet) {
			if (checkSubType(playerId, subType)) {
				return subType;
			}
		}
		return 0;
	}

	private boolean checkSubType(String playerId, int exclusionEnumType) {
		switch (exclusionEnumType) {
		case ExclusionType.CROSSARENA_PVP:
			return CrossArenaPvpManager.getInstance().isHaveCrossArenaPvpRoom(playerId);
		case ExclusionType.CROSSARENA_LEITAI_UP:
			return CrossArenaManager.getInstance().hasJionArena(playerId) > 0;
		case ExclusionType.CRAZY_FIGHT:
			return false;
		case ExclusionType.FIGHT:
			return BattleManager.getInstance().isInBattle(playerId);
		case ExclusionType.MIST_FIGHT:
			return false;
		case ExclusionType.PET_AVOIDANCE_GAME:
			return PetAvoidanceGameManager.getInstance().isInGame(playerId);
		default:
			break;
		}
		return true;
	}

	private void addSubTypeMap(int mainType, Integer... subType) {
		typeMap.put(mainType, Arrays.asList(subType));
	}

	private void addSubTypeSet(Set<Integer> subTypeList) {
		typeSet.addAll(subTypeList);
	}

	public RetCodeId.RetCodeEnum getRetCodeByType(int type) {
		switch (type) {
			case ExclusionType.CROSSARENA_PVP:
				return RetCodeId.RetCodeEnum.RCE_PLAY_LTPVP;
			case ExclusionType.CROSSARENA_LEITAI_UP:
				return RetCodeId.RetCodeEnum.RCE_PLAY_LT;
			case ExclusionType.CRAZY_FIGHT:
				return RetCodeId.RetCodeEnum.RCE_PLAY_CRAZY;
			case ExclusionType.FIGHT:
				return RetCodeId.RetCodeEnum.RCE_PLAY_FIGHT;
			case ExclusionType.PET_AVOIDANCE_GAME:
				return RetCodeEnum.REC_IN_PET_AVOIDANCE_GAME;
			default:
				return RetCodeId.RetCodeEnum.RCE_ConfigError;
		}
	}

}
