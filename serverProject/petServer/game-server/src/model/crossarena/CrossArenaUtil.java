package model.crossarena;

public class CrossArenaUtil {
	
	public static final int SCREEN_TABLENUM = 3; // 一屏N个擂台
	
    public static final int AT_NOT = 0; // 不在擂台
    public static final int AT_DEF = 10; // 是擂主
    public static final int AT_ATT = 20; // 是攻擂者
    public static final int AT_QUE = 30; // 是队列

    public static final String SYNCACHE = "99999999";

    // 1等级2连胜3日挑战次数4周挑战次数
    public static final int TRIGGER_LV = 1; // 等级
    public static final int TRIGGER_COU = 2; // 连胜
    public static final int TRIGGER_DAY = 3; // 日挑战次数
    public static final int TRIGGER_WEEK = 4; //周挑战次数
    public static final int TRIGGER_WEEKRATE = 5; //每周X场胜率
    public static final int TRIGGER_COUINS = 6; // 是队列

    public static final int DbChangeAdd = 1; // 累加
    public static final int DbChangeRep= 2; // 替换
    public static final int DbChangeRepMax = 3; // 大于替换

    public static final int HR_LT_JION = 1001;// 擂台参与次数
    public static final int HR_LT_WIN = 1002;// 擂台胜利次数
    public static final int HR_LT_10WIN = 1003;// 10连胜次数
    public static final int HR_LT_ZAN = 1004;// 点赞次数
    public static final int HR_LT_HOT = 1005;// 累计获得热度
    public static final int HR_LT_DEF = 1006;// 守擂次数
    public static final int HR_LT_ATT = 1007;// 打雷次数
    public static final int HR_LT_TIME = 1008;// 参与擂台累计时常

    public static final int HR_PVP_JION = 1101;// 切磋次数
    public static final int HR_PVP_WIN = 1102;// 切磋胜利次数
    public static final int HR_PVP_GOLD = 1103;// 切磋获得金额
    public static final int HR_FKDJ_JION = 1104;// 疯狂参与对决次数
    public static final int HR_FKDJ_5PASS = 1105;// 疯狂对决五通过次数
    public static final int HR_TEAM_JION = 1106;// 组队次数
    public static final int HR_TEAM_PASS = 1107;// 组队通过次数
    public static final int HR_DFHZ_JION = 1108;// 巅峰混战次数
    public static final int HR_DFHZ_1NUM = 1109;// 巅峰第一次数

    public static final int HR_XS_NUM = 1110;// 悬赏次数
    public static final int HR_XS_NUM_H = 1202;// 红悬次数
    public static final int HR_XS_NUM_C = 1203;// 橙悬次数
    public static final int HR_XS_NUM_Z = 1204;// 紫悬次数
    public static final int HR_DAY = 1205;// 活跃天数
    public static final int HR_XS_NUM_F = 252;// 悬赏次数完成
    public static final int HR_XS_NUM_FH = 1207;// 红悬次数完成
    public static final int HR_XS_NUM_FC = 1208;// 橙悬次数完成
    public static final int HR_XS_NUM_FZ = 1209;// 紫悬次数完成

    public static final int HR_LT90DAY = 1301;// 混迹擂台90天
    public static final int HR_FIRST_10WIN = 1302;// 第一次获得十连胜
    public static final int HR_FIRST_150WIN = 1303;// 切磋——挑战自身战力150%玩家成功
    public static final int HR_FIRST_XS_FH = 1304;// 第一次完成红色悬赏
    public static final int HR_FIRST_XS_H = 1305;// 第一次发布红色悬赏
    public static final int HR_FIRST_FKDJ = 1306;// 第一次疯狂对决通关
    public static final int HR_FIRST_DFHZ = 1307;// 第一次巅峰混战第一名
    public static final int HR_FIRST_100RATE = 1308;// 百场擂台胜率80%

}
