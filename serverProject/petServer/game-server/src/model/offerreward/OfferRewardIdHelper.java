package model.offerreward;

import org.apache.commons.lang.math.NumberUtils;

import common.load.ServerConfig;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;

/*
*@author Hammer
*2021年11月29日
*/
public class OfferRewardIdHelper {
	private final static String STRLIMIT = "_";
	private final static int STRLENGTH = 4;


	public static String createOfferId(String playerId, int playerSaveNum, int levelLimit) {

		int serverIndex = ServerConfig.getInstance().getServer();
		StringBuilder sb = new StringBuilder();
		sb.append(serverIndex + STRLIMIT);
		sb.append(playerId + STRLIMIT);
		sb.append(playerSaveNum + STRLIMIT);
		sb.append(levelLimit);
		// 服务器ID_玩家ID_玩家发布悬赏缓存ID_等级区间
		return sb.toString();
	}

	public static int getPlayerIdByOfferId(String offerId) {
		String[] split = strSplit(offerId);
		if (!strLengthCheck(split)) {
			return 0;
		}
		return NumberUtils.toInt(split[1]);
	}

	public static String getServerIndexByOfferId(String offerId) {
		String[] split = strSplit(offerId);
		if (!strLengthCheck(split)) {
			return "";
		}
		return split[0];
	}

	public static String getLevelLimitByOfferId(String offerId) {
		String[] split = strSplit(offerId);
		if (!strLengthCheck(split)) {
			return "";
		}
		return split[3];
	}

	public static int getPlayerShortId(String playerId) {
		playerEntity entity = playerCache.getByIdx(playerId);
		if (entity == null) {
			return 0;
		}
		return entity.getShortid();
	}

	private static boolean strLengthCheck(String[] split) {
		if (split == null) {
			return false;
		}
		if (split.length < STRLENGTH) {
			return false;
		}
		return true;
	}

	private static String[] strSplit(String str) {
		String[] split = str.split(STRLIMIT);
		if (split == null) {
			return null;
		}
		return split;
	}

}
