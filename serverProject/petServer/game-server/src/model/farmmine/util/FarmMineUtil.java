package model.farmmine.util;

public class FarmMineUtil {
    /**
     * 竞拍未开启
     */
    public static final int STATE_NOT_OPEN = 0; // 竞拍未开启
    /**
     * 竞拍开启出价阶段
     */
    public static final int STATE_OFFERPRICE = 1;// 竞拍开启出价阶段
    /**
     * 拍开启展示阶段
     */
    public static final int STATE_VIEW = 2;// 竞拍开启展示阶段
    /**
     * 拍拍开启休息阶段
     */
    public static final int STATE_REST = 3;

    public static final long HOURTOSECOND = 3600000L;

    public static final String KEY_PUB = "pub";
}
