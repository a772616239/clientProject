package model.mistforest.formula;

import model.mistforest.mistobj.MistObject;

import java.util.List;

public class Formula {
    public static int calculate(int id, MistObject user, MistObject target, List<Integer> paramList) {
        switch (id) {
            case 1: // 直接返回值
                if (paramList != null && paramList.size() > 0) {
                    return paramList.get(0);
                }
                break;
            case 2:
                if (paramList != null && paramList.size() > 2) {
                    int param1 = paramList.get(0);
                    int param2 = paramList.get(1);
                    int param3 = paramList.get(2);
                    int attrVal = (int) user.getAttribute(param2);
                    return param1 + attrVal * param3 / 1000;
                }
                break;
            default:
                break;
        }
        return 0;
    }
}
