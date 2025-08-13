package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author huhan
 * @date 2020.03.06
 */
public class ArrayUtil {

    public static final int[][] emptyIntList2 = new int[0][0];

    public static boolean intArrayContain(int[] array, int param) {
        if (array == null) {
            return false;
        }
        for (int i : array) {
            if (i == param) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前一个数组是否包含后一个数组的所有元素
     *
     * @param array
     * @param param
     * @return
     */
    public static boolean intArrayContainArray(int[] array, int[] param) {
        if (array == null || param == null) {
            return false;
        }

        for (int i : param) {
            if (!intArrayContain(array, i)) {
                return false;
            }
        }

        return true;
    }


    /**
     * 判断两个int[] 是否有具有完全相同(array 长度相同， 包含的元素相同）的元素,但不考虑数组中元素的顺序
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean haveSameElementExclusionOrder(int[] first, int[] second) {
        if (first == null || second == null) {
            return false;
        }

        if (first.length != second.length) {
            return false;
        }

        for (int i : first) {
            if (!intArrayContain(second, i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否含有重复的nodeId,该方法只适合检测长度较短的array
     *
     * @param nodeList
     * @return
     */
    public static boolean isHaveRepeatedElement(int[] nodeList) {
        if (nodeList == null) {
            return false;
        }

        for (int i = 0; i < nodeList.length; i++) {
            for (int j = i + 1; j < nodeList.length; j++) {
                if (nodeList[i] == nodeList[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断两个数组类是否含有相同的元素
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean haveDuplicateElement(int[] first, int[] second) {
        if (first == null || second == null) {
            return false;
        }
        for (int i : first) {
            if (intArrayContain(second, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断两个数组类是否含有相同的元素
     *
     * @param intArray
     * @return
     */
    public static boolean haveDuplicateElement(int[] intArray) {
        if (intArray == null) {
            return false;
        }

        Set<Integer> set = new HashSet<>();
        for (int i : intArray) {
            if (set.contains(i)) {
                return true;
            }

            set.add(i);
        }

        return false;
    }


    public static class ArrayOperation {
        public static final int AO_MAX = 1;
        public static final int AO_MIN = 2;
        public static final int AO_SUM = 3;
    }

    /**
     * 对intArray的操作
     *
     * @param array
     * @param operation 1：极大值，2：极小值，3：求和
     * @return
     * @see ArrayOperation
     */
    public static Integer intArrayOperation(int[] array, int operation) {
        if (array == null || array.length <= 0) {
            return null;
        }

        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];

            if (array[i] > max) {
                max = array[i];
            }

            if (array[i] < min) {
                min = array[i];
            }
        }

        if (operation == ArrayOperation.AO_MAX) {
            return max;
        } else if (operation == ArrayOperation.AO_MIN) {
            return min;
        } else if (operation == ArrayOperation.AO_SUM) {
            return sum;
        }
        return null;
    }

    public static int getMaxInt(int[] intList, int defaultValue) {
        Integer result = intArrayOperation(intList, ArrayOperation.AO_MAX);
        return result == null ? defaultValue : result;
    }

    public static int getMinInt(int[] intList, int defaultValue) {
        Integer result = intArrayOperation(intList, ArrayOperation.AO_MIN);
        return result == null ? defaultValue : result;
    }

    public static boolean intArrayIsEmpty(int[] array) {
        return array == null || array.length <= 0;
    }

    /**
     * 检查数组的大小,
     *
     * @param target
     * @param x
     * @param y
     * @return
     */
    public static boolean checkArraySize(int[][] target, int x, int y) {
        if (target == null) {
            return false;
        }
        if (target.length < x) {
            return false;
        }

        for (int[] ints : target) {
            if (ints.length < y) {
                return false;
            }
        }

        return true;
    }

    public static List<Integer> intArrayToList(int[] ints) {
        List<Integer> result = new ArrayList<>();
        if (ints == null) {
            return result;
        }

        for (int anInt : ints) {
            result.add(anInt);
        }
        return result;
    }

    public static boolean contain(Object[] array, Object target) {
        if (array == null || target == null) {
            return false;
        }

        for (Object obj : array) {
            if (target.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static String toString(int[] ints) {
        StringBuilder builder = new StringBuilder();
        if (ints == null || ints.length <= 0) {
            return builder.toString();
        }
        builder.append("{");
        for (int anInt : ints) {
            builder.append(anInt);
            builder.append(",");
        }
        //删除最后一个逗号
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    public static String toString(int[][] ints) {
        StringBuilder result = new StringBuilder();
        if (ints != null) {
            for (int[] anInt : ints) {
                result.append("[");
                for (int i : anInt) {
                    result.append("[");
                    result.append(i);
                    result.append("]");
                }
                result.append(",]");
            }
        }
        return result.toString();
    }


    public static int getValueFromKeyValueIntArray(int[][] data, int key) {
        for (int[] ints : data) {
            if (ints.length < 2) {
                continue;
            }
            if (ints[0] == key) {
                return ints[1];
            }
        }
        return 0;
    }
}
