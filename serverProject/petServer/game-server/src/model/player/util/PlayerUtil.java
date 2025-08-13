package model.player.util;

import common.GameConst;
import common.tick.GlobalTick;
import datatool.StringHelper;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.wordFilter.WordFilterManager;
import org.apache.commons.lang.StringUtils;
import protocol.Common;
import protocol.Common.LanguageEnum;
import protocol.Common.RewardTypeEnum;
import protocol.LoginProto.ClientData.Builder;
import protocol.PlayerInfo.Artifact;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.ObjUtil;
import util.TimeUtil;

public class PlayerUtil {

    public static int queryPlayerKeyNodeId(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        mainlineEntity mainline = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainline == null) {
            return 0;
        }

        return mainline.getDBBuilder().getKeyNodeId();
    }

    public static boolean queryFunctionUnlock(String playerIdx, Common.EnumFunction function) {
        if (playerIdx == null || function == null) {
            return false;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        return player.functionUnLock(function);
    }

    public static boolean queryFunctionLock(String playerIdx, Common.EnumFunction function) {
        return !queryFunctionUnlock(playerIdx,function);
    }




    public static int queryPlayerLv(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return 0;
        }

        return player.getLevel();
    }

    public static int queryPlayerVipLv(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return 0;
        }

        return player.getVip();
    }

    public static int queryPlayerVipExp(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return 0;
        }

        return player.getVipexperience();
    }

    public static String queryPlayerUserId(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }

        return player.getUserid();
    }

    public static LanguageEnum queryPlayerLanguage(String playerIdx) {
        LanguageEnum language = LanguageEnum.LE_SimpleChinese;
        if (playerIdx == null) {
            return language;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return language;
        }

        return player.getLanguage();
    }

    public static LanguageEnum queryLanguageByUid(String userId) {
        LanguageEnum language = LanguageEnum.LE_SimpleChinese;
        if (userId == null) {
            return language;
        }

        playerEntity player = playerCache.getInstance().getPlayerByUserId(userId);
        if (player == null) {
            return language;
        }

        return player.getLanguage();
    }

    public static String queryPlayerName(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return "";
        } else {
            return byIdx.getName();
        }
    }

    public static String queryPlayerPlantForm(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return "";
        }

        Builder clientData = byIdx.getClientData();
        if (clientData == null) {
            return "";
        }

        return clientData.getPlatform();
    }

    public static String queryPlayerNetType(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return "";
        }
        return byIdx.getNetType();
    }

    public static String queryPlayerChannel(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return "";
        }
        Builder clientData = byIdx.getClientData();
        if (clientData == null) {
            return "";
        }

        return clientData.getChannel();
    }

    public static String queryPlayerChannelByUserId(String userId) {
        playerEntity byUserId = playerCache.getInstance().getPlayerByUserId(userId);
        if (byUserId == null) {
            return "";
        }
        Builder clientData = byUserId.getClientData();
        if (clientData == null) {
            return "";
        }

        return clientData.getChannel();
    }

    public static String queryPlayerClientVersionNum(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return "";
        }
        Builder clientData = byIdx.getClientData();
        if (clientData == null) {
            return "";
        }

        return clientData.getClientVersion();
    }

    public static int queryPlayerSourceId(String playerIdx) {
        playerEntity byIdx = playerCache.getByIdx(playerIdx);
        if (byIdx == null) {
            return 0;
        }
        return byIdx.getClientSourceId();
    }

    public static RetCodeEnum checkName(String name) {
        if (StringHelper.isNull(name)) {
            return RetCodeEnum.RCE_AlterName_Empty;
        }

        if (playerCache.getInstance().isNameDuplicate(name)) {
            return RetCodeEnum.RCE_AlterName_Duplicate;
        }
        //长度判断
        if (ObjUtil.getStringWeight(name) > GameConst.ROLE_NAME_MAX_LENGTH) {
            return RetCodeEnum.RCE_AlterName_TooLong;
        }
        //姓名合法性检查
        if (!WordFilterManager.getInstance().checkPlatformSensitiveWords(name)) {
            return RetCodeEnum.RCE_AlterName_IllegalWords;
        }
        return RetCodeEnum.RCE_Success;
    }

    public static boolean playerIsExist(String idx) {
        return playerCache.getByIdx(idx) != null;
    }

    public static boolean playerNotExist(String idx) {
        return !playerIsExist(idx);
    }

    public static long queryPlayerLastLogOutTime(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null || entity.getLogouttime() == null) {
            return 0;
        }

        return entity.getLogouttime().getTime();
    }

    public static long queryPlayerCurrency(String playerIdx, RewardTypeEnum rewardType) {
        if (StringUtils.isEmpty(playerIdx) || !GameUtil.isCurrencyRewardType(rewardType)) {
            return 0;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        return player == null ? 0 : player.getCurrencyCount(rewardType);
    }

    public static int queryPlayerTitleId(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        return entity == null ? 0 : entity.getTitleId();
    }

    public static int queryPlayerSkillLv(String playerIdx, int skillId) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return 0;
        }

        for (Artifact value : entity.getDb_data().getArtifactList()) {
            if (value.getPlayerSkill().getSkillCfgId() == skillId) {
                return value.getPlayerSkill().getSkillLv();
            }
        }
        return 0;
    }

    public static int queryPlayerNewTitleId(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        return player == null ? 0 : player.getCurEquipNewTitleId();
    }

    public static int getPlayerOffLineDays(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return -1;
        }
        if (player.isOnline()) {
            return 0;
        }
        long logoutTime = player.getLogouttime() == null ? 0 : player.getLogouttime().getTime();
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        return (int) ((currentTime - logoutTime) / TimeUtil.MS_IN_A_DAY);
    }
}
