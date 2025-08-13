package common;

public class GameConst {
    public static final int ROLE_NAME_MAX_LENGTH = 14;
    public static final int CONFIG_ID = 1;

    public static final long UpdateServerTime = 3000;
    public static final long BattleTimeout = 300000;
    public static final long NewbeeBattleTimeout = 1800000;

    public static final int MineExploitScrollItemId = 1032; // 矿区开采劵道具id
    public static final int MineFriendHelpItemId = 1033; // 矿区助阵劵道具id

    public static class ServerState {
        public static final int ServerClosed = 0;
        public static final int ServerRunning = 1;
        public static final int ServerClosing = 2;
    }

    /**
     * 排行榜枚举
     */
    public static class RankingName {
        public static final String RN_MainLinePassed = "MainLinePassed";        //主线闯关通关排行榜
        public static final String RN_ForInv_bossDamage = "ForInv_BossDamage";  //外敌入侵boss伤害排行榜
        public static final String RN_EndlessSpire = "EndlessSpire";            //无尽尖塔排行榜
        public static final String RN_MistTransSvrRank = "MistTransSvrRank";    //迷雾森林跨服排行榜
        public static final String RN_MistLocalSvrRank = "MistLocalSvrRank";    //迷雾森林本服排行榜

        public static final String RN_Statistics_Ability = "Ability";           //统计_战力
        public static final String RN_Statistics_Diamond = "Diamond";           //统计_钻石
        public static final String RN_Statistics_Gold = "Gold";              //统计_金币
        public static final String RN_Statistics_Coupon = "Coupon";             //统计_点券
        public static final String RN_Statistics_StrayMarks = "StrayMarks";     //统计_流浪印记
        public static final String RN_Statistics_HighDrawCard = "HighDrawCard"; //统计_高级召唤卷
        public static final String RN_Statistics_Ancient = "AncientItem";       //统计_远古召唤卷
    }

    public static class EventType {
        public static final int ET_Login = 1;
        public static final int ET_Logout = 2;

        public static final int ET_AddCurrency = 3;            //增加货币  锁对象playerEntity  参数:货币类型（rewardTypeEnum中的货币）， 增加的数量
//        public static final int ET_ReduceCurrency = 4;         //减少货币  锁对象playerEntity  参数:货币类型， 减少的数量

        public static final int ET_RemoveItem = 5;              // 参数Map<Integer, integer>   itemCfgId,count
        public static final int ET_AddItem = 6;                 //参数同上

        public static final int ET_AddAvatar = 10;              //添加头像， 锁对象playerEntity，参数 List<Integer> avatarCfgIdList

        public static final int ET_UnlockTeamAndPosition = 11;  //解锁备战小队和位置 锁对象teamEntity， 参数：玩家等级

        public static final int ET_RemoveDisplayPet = 12;       //移除展示的宠物Idx   锁对象 playerEntity， 参数：宠物Idx List<String>

        //添加宠物，传入PetService.getPetByPlayer，参数Map：图鉴id-数量，参数2获得宠物原因：RewardSourceEnum.Value
        public static final int ET_AddPet = 13;
        //添加符文，传入PetRuneService.getRuneByPlayer，参数Map：图鉴id-数量
        public static final int ET_AddPetRune = 15;
        //添加宠物碎片，传入PetFragmentService.getFragmentByPlayer，参数Map：碎片图鉴id，添加数量
        public static final int ET_AddPetFragment = 17;

        public static final int ET_AddMail = 19;                //锁对象 mailboxEntity 参数: DB_Mail

        //清空符文状态，传入PetRuneService.getRuneByPlayer，参数List<String>宠物idList（符文被装备的宠物）
        public static final int ET_ResetRuneStatus = 21;
        //设置宠物委托状态，传入PetService.getPetByPlayer，参数List<String>宠物idList
        public static final int ET_SetPetMissionStatus = 22;
        //清空宠物委托状态，传入PetService.getPetByPlayer，参数List<String>宠物idList
        public static final int ET_ResetPetMissionStatus = 23;

        public static final int ET_AddVIPExp = 24;        //锁对象playerEntity，参数：添加的数量
        public static final int ET_AddExp = 25;           //锁对象playerEntity，参数：添加的数量

        public static final int ET_MainLineBattleSettle = 26;    //锁对象：mainlineEntity，参数1：nodeId，参数2：winnerCamp
        public static final int ET_UnlockMainLine = 27;           //玩家升级解锁新关卡，锁对象mainLineEntity, 参数1：玩家等级

        public static final int ET_VipLvUp = 30;          //Vip升级，锁对象：空BaseObj，参数一：playerIdx， 参数二：升级前等级，参数三，升级后等级

        public static final int ET_RemoveCourageTrialTeamDeadPet = 32;       //移除勇气试炼阵亡的小队

        public static final int ET_UpdateTargetProgress = 36;                  //更新目标系统进度

        // ----------迷雾森林Start(51-55)----------
        public static final int ET_MistForestSeasonEnd = 51;    //迷雾森林赛季结束清空玩家积分
        public static final int ET_ClearMistItem = 52;      // 每日清除迷雾森林宝箱
        public static final int ET_MistForestServerClosed = 53;      // 迷雾森林服务器关闭
        public static final int ET_AddMistIntegral = 54;      // 增加迷雾森林积分
        // ----------迷雾森林End(51-55)----------

        //--------矿区争夺Start(56-60)--------------
        public static final int ET_MineFightServerClosed = 56; // 矿区服务器关闭
        public static final int ET_LockPetTeam = 57; // 加锁解锁小队
        public static final int ET_UpdateApplyFriendHelpList = 58;       //更新申请好友助阵信息
        public static final int ET_UpdateBeAppliedFriendHelpList = 59;       //更新被好友申请助阵信息
        public static final int ET_UpdateHelpingFriendIdx = 60;       //更新玩家助阵的好友id
        //--------矿区争夺End(56-60)--------------

        public static final int ET_BattleServerClosed = 61; // 战斗服关闭

        public static final int ET_PointCopySettle = 62;

        public static final int ET_CalculateDropItem = 65;          //计算掉落活动道具
        public static final int ET_AddMainLineDropItem = 66;        //添加掉落到主线挂机

        public static final int ET_Statistics = 67;                 //统计事件
    }

    public static class RedisKey {
        public static final String BattleServerInfo = "BattleServerInfo"; // 战斗服信息

        public static final String CrossServerInfo = "CrossServerInfo"; // 跨服服务器地址信息

        public static final String MistForestInfo = "MistForestInfo-"; // 迷雾森林相关信息

        public static final String MineFightInfo = "MineFightInfo"; // 矿区争夺相关信息
    }

    /**
     * 平台聊天返回码
     */
    public static class ChatRetCode {
        public static final int UNKNOWN_ERROR = 0;
        public static final int OPEN = 1;
        public static final int LV_NOT_ENOUGH = 2;       //等级不足
        public static final int BANNED_TO_POST = 3;      //禁言
    }

    public static class ServerStringConst {
        public static final int LV_NOT_ENOUGH = 61;
    }

    public static class Ban {
        public static final int ROLE = 1;     //角色
        public static final int CHAT = 2;     //聊天
    }
}
