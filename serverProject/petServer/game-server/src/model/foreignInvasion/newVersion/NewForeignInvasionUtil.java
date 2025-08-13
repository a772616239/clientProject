package model.foreignInvasion.newVersion;

import java.util.ArrayList;
import java.util.List;
import util.ArrayUtil;

/**
 * @author huhan
 * @date 2020.11.09
 */
public class NewForeignInvasionUtil {
    private NewForeignInvasionUtil(){}

    /**
     * 获取下一个开放日（1-7）
     *
     * @param openDay
     * @param toDayInWeek
     * @return
     */
    public static int getNextOpenDay(int[] openDay, int toDayInWeek) {
        if (openDay == null || toDayInWeek < 1 || toDayInWeek > 7) {
            return 0;
        }

        List<Integer> list = excludeElement(openDay, new int[]{1, 2, 3, 4, 5, 6, 7});
        list.sort(Integer::compareTo);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > toDayInWeek) {
                return list.get(i);
            }
        }
        return list.get(0);
    }

    /**
     //     * 剔除除指定元素之外的元素
     //     *
     //     * @param checkIntArr  需要筛选的数组
     //     * @param containArr  给定元素数组
     //     * @return
     //     */
    public static List<Integer> excludeElement(int[] checkIntArr, int[] containArr) {
        List<Integer> array = new ArrayList<>();

        if (checkIntArr == null || containArr == null) {
            return array;
        }

        for (int i = 0; i < checkIntArr.length; i++) {
            if (ArrayUtil.intArrayContain(containArr, checkIntArr[i])) {
                array.add(checkIntArr[i]);
            }
        }

        return array;
    }
}
