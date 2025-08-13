package model.wordFilter;

public class WordFilterUtil {

    /**
     * 判断是否是系统信息,例如宠物分享信息,或者符文分享信息
     */
    public static boolean isSystemInfo(String info) {
        if (info == null) {
            return false;
        }

        int firstNum = getFirstNum(info);
        if (firstNum == 0 || firstNum == 1 || calculateCharCount(info, ',') == 3) {
            return true;
        }

        return false;
    }

    /**
     * 返回字符串的第一个数字
     *
     * @return  -1为没有数字
     */
    public static int getFirstNum(String str) {
        if (str == null) {
            return -1;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(0);
            if (Character.isDigit(c)) {
                return Integer.valueOf(c);
            }
        }

        return -1;
    }

    public static int calculateCharCount(String str, char c) {
        if (str == null) {
            return 0;
        }

        int charCount = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                charCount ++ ;
            }
        }

        return charCount;
    }
}
