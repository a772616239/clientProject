/*CREATED BY TOOL*/

package model.comment;

import util.GameUtil;

public class commentConstant{
    //TODO  原100
    public static int MaxCommentContSize = 100;

    public static int MaxQueryCommentCount = 20;

    public static int UpdateCommentInterval = 120000;

    public static String buildIdx(int type, int linkId) {
        return GameUtil.longToString((((long) type) << 32) | linkId, "");
    }
}
