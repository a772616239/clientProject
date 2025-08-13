package petrobot.robotConst;

public class IndexConst {
    /** 玩家相关 1-10 **/
    /**
     * 登陆流程放在玩家的初始化阶段
     **/
//    public final static int LOG_IN = 1;
    public final static int ALTER_NAME = 2;
    public final static int CHANGE_AVATAR = 3;
    public final static int GOLD_EXCHANGE = 4;
    public final static int CLAIM_VIP_GIFT = 5;

    /**
     * 邮件 11-20
     **/
    public final static int CLAIM_MAIL_BOX = 11;
    public final static int READ_MAIL = 12;
    public final static int CLAIM_ATTACHMENT = 13;
    public final static int CLAIM_ALL_ATTACHMENT = 14;
    public final static int DELETE_MAIL = 15;
    public final static int DELETE_ALL_MAIL = 16;

    /**
     * 道具背包 21-30
     **/
    public final static int SELL_ITEM = 21;
    public final static int USE_ITEM = 22;

    /**
     * 好友 31-40
     **/
    public final static int CLAIM_FRIEND_INFO = 31;
    public final static int CLAIM_RECOMMEND_FRIEND = 32;
    public final static int CLAIM_ALL_POINT_AND_SEND = 33;
    public final static int DELETE_FRIEND = 34;
    public final static int APPLY_ADD_FRIEND = 35;
    public final static int RESPOND_ADD_FRIEND = 36;
    /**
     * 宠物 41-50
     */
    public final static int PET_BAG_INIT = 41;
    public final static int PET_BAG_ENLARGE = 42;
    public final static int PET_DISCHARGE = 43;
    public final static int PET_LVL_UP = 44;
    public final static int PET_Rarity_Up = 45;
    /**
     * 远古召唤 46-50
     **/
    public final static int CALL_ANCIENT = 46;
    public final static int PET_TRANSFER = 47;

    /**
     * 抽卡 51-60
     **/
    public final static int CLAIM_DRAW_CARD_INFO = 51;
    public final static int DRAW_COMMON_CARD = 52;
    public final static int DRAW_HIGH_CARD = 53;
    public final static int RESET_HIGH_CARD = 54;
   // public final static int EXCHANGE_LOW_CALL_BOOK = 55;
    public final static int DRAW_FRIEND_SHIP_CARD = 56;
    public final static int ABANDON_HIGH_POOL = 57;
    public final static int ENSURE_HIGH_DRAW_CARD= 59;
    public final static int CLAIM_SELECTED_PET = 60;

    /**
     * 编队 61-70
     **/
    public final static int CLAIM_TEAM_INFO = 61;
    public final static int BUY_TEAM = 62;
    public final static int CHANGE_TEAM_NAME = 63;
    public final static int UPDATE_TEAM = 64;
    public final static int CHANGE_USED_TEAM = 65;

    /**
     * 主线 71-80
     **/
    public final static int CLAIM_MAINLINE_INFO = 71;
    public final static int CLAIM_ON_HOOK_INFO = 72;
    public final static int CLAIM_PASSED_RANKING = 73;
    public final static int CLAIM_RECENT_PASSED_INFO = 74;
    public final static int QUICK_ON_HOOK = 75;
    public final static int SETTLE_ON_HOOK_REWARD = 76;

    /**
     * 资源副本 81-90
     **/
    public final static int CLAIM_RES_COPY_INFO = 81;
    public final static int BUY_TIMES = 82;
    public final static int SWEEP_RES_COPY = 83;

    /**
     * 商店 90-100
     **/
    public final static int CLAIM_SHOP_INFO = 91;
    public final static int REFRESH_SHOP = 92;
    public final static int BUY_GOODS = 93;

    /**
     * 目标系统 101-110
     **/
    public final static int CLAIM_TARGET_INFO = 101;
    public final static int CLAIM_DAILY_REWARD = 102;
    public final static int CLAIM_ACHIEVEMENT_REWARD = 103;

    /**
     * 宠物碎片 111-120
     */
    public final static int PET_FRAGMENT_INIT = 111;
    public final static int PET_FRAGMENT_USE = 112;
    public final static int PET_FRAGMENT_USE_ALL = 113;

    /**
     * 宠物符文 141-160
     */
    public final static int PET_RUNE_BAG_INIT = 141;
    public final static int PET_RUNE_UN_EQUIP = 142;
    public final static int PET_RUNE_LVL_UP = 143;
    public final static int PET_RUNE_EQUIP = 144;

    /**
     * 宠物委托 161-180
     */
    public static final int PET_MISSION_INIT = 161;
    public static final int PET_MISSION_COMPLETE = 162;
    public static final int PET_MISSION_ACCEPT = 163;

    /**
     * 宠物收集 181-190
     */
//    public static final int PET_COLLECTION_INIT = 181;
//    public static final int PET_COLLECTION_REWARD = 182;

    /**
     * 勇气试炼 191-200
     */
    public static final int BRAVE_CHALLENGE_INIT = 191;
    public static final int BRAVE_UPDATE_TEAM = 192;
    public static final int BRAVE_CHALLENGE_BATTLE = 193;
    public static final int BRAVE_REBORN= 194;

    /**
     * 秘境探索 201-220
     */
    public static final int PATROL_FINISH_CHECK = 201;
    public static final int PATROL_INIT = 202;
    public static final int PATROL_PLAY = 203;

    /**
     * 活动 221-230
     */
    public static final int CLAIM_ACTIVITY = 221;
    public static final int CLAIM_ACTIVITY_REWARD = 222;
    public static final int SIGN_IN = 223;
    public static final int CLAIM_NOVICE = 224;
    public static final int CLAIM_NOVICE_Reward = 225;
    public static final int Claim_DayDayRecharge_Reward = 226;


    /**
     * 矿区 231-250
     */
    public static final int MINE_JoinMineFight = 231;
    public static final int MINE_ClaimMineReward = 232;
    public static final int MINE_UpdateMineTeam = 233;
    public static final int MINE_BuyExploitScroll = 234;
    public static final int MINE_OccupyMine = 235;
    public static final int MINE_ChooseRewardType = 236;
    public static final int MINE_QueryMineForm = 237;
    public static final int MINE_QueryMineRecord = 238;
    public static final int MINE_QueryFriendHelpList = 239;
    public static final int MINE_ApplyFriendHelp = 240;
 //   public static final int MINE_RespondFriendHelp = 241;
    public static final int MINE_ClaimMineGift = 242;
    public static final int MINE_ExitMineFight = 243;

    /**
     * 远征 260-280
     */
    public final static int TheWar_EnterWarRoom = 260;
    public final static int TheWar_QueryAllWarPet = 261;
    public final static int TheWar_AddPetToWar = 262;
    public final static int TheWar_QueryWarTeam = 263;
    public final static int TheWar_UpdatePetTeam = 264;
    public final static int TheWar_QueryGridList = 265;
    public final static int TheWar_AttackGrid = 266;
    public final static int TheWar_StationTroopsGrid = 267;
    public final static int TheWar_BuyStamina = 268;
    public final static int TheWar_PromoteJobTile = 269;
    public final static int TheWar_ClearOwnedGrid = 270;
    public final static int TheWar_ComposeWarItem = 271;
    public final static int TheWar_EquipOnWarItem = 272;
    public final static int TheWar_LevelUpWarTech = 273;
    public final static int TheWar_GainAfkReward = 274;

    /**
     * 竞技场 301 - 350
     */
    public static final int ARENA_UPDATE_TEAM = 301;
    public static final int ARENA_CLAIM_ARENA_INFO = 302;
    public static final int ARENA_CLAIM_RANKING = 303;
    public static final int ARENA_CLAIM_RECORDS = 304;
    public static final int ARENA_TOTAL_INFO = 305;
    public static final int ARENA_BUY_ITEM = 306;

//    //竞技场gm测试
//    public static final int ARENA_GM_DAN = 307;
//    public static final int ARENA_GM_SCORE = 308;

    /**
     * 月卡351-360
     */
    public static final int MonthCard_Info = 351;
    public static final int BUY_MONTH_CARD = 352;


    /**
     * 功勋361-370
     */
    public static final int Feats_Info = 361;
    public static final int ClaimFeatsReward = 362;

    /**
     * 宝石 371-380
     */
 //   public final static int Gem_Lv_UP = 371;


    /**
     * 神器 381-390
     */

    //神器升星/激活
    public final static int Artifact_Star_Up =383;

    //神器升级
    public final static int Artifact_Lv_Up = 384;



    /**
     * 无尽尖塔(放迷雾森林前) 400-420
     **/
    public final static int CLAIM_ENDLESS_INFO = 401;
    public final static int CLAIM_ENDLESS_RANKING = 402;
    public final static int CLAIM_SPIRE_ACHIEVEMENT_REWARDS = 403;

    /**
     * boss塔 421-440
     */
    public final static int CLAIM_BOSS_TOWER_INFO = 421;
    public final static int SWEEP_BOSS_TOWER = 422;

    /**
     * 迷雾森林(放战斗前) 901-950
     */
    public static final int MIST_JoinMistForest = 901;
    public static final int MIST_MistForestMove = 902; // 在迷雾森林里跑

    /**
     * 战斗(放最后)
     */
    public static final int BATTLE_LAST_STEP = 1000;
}
