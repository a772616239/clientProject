package model.wordFilter;

import cfg.BadWord;
import cfg.BadWordObject;
import com.hyz.platform.sdk.utils.sensi.SensiWordsUtils;
import common.load.ServerConfig;
import io.netty.util.internal.ConcurrentSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import util.LogUtil;

public class WordFilterManager {
    private static WordFilterManager instance = new WordFilterManager();

    private static Set<String> badWordSet = new ConcurrentSet<>();
    private static Map<Character, Integer> fastCheckMap = new ConcurrentHashMap<>();
    private static Map<Character, Integer> beginCheckMap = new ConcurrentHashMap<>();
    private static Map<Character, Integer> endCheckMap = new ConcurrentHashMap<>();

    private static int maxWordLength = 0;


    public static WordFilterManager getInstance() {
        if (instance == null) {
            synchronized (WordFilterManager.class) {
                if (instance == null) {
                    instance = new WordFilterManager();
                }
            }
        }
        return instance;
    }


    public boolean init() {
        Map<String, BadWordObject> ix_badword = BadWord._ix_badword;
        if (ix_badword == null || ix_badword.size() <= 0) {
            LogUtil.error("badwordCfg is null");
            return false;
        }

        for (BadWordObject value : ix_badword.values()) {
            String badWord = value.getBadword();
            addWord(badWord);
        }
        LogUtil.info(String.format("badWrodSet.size=%d,fastCheckMap.size=%d,beginCheckMap.size=%d,endCheckMap.size=%d",
                badWordSet.size(), fastCheckMap.size(), beginCheckMap.size(), endCheckMap.size()));

        initSensiWordsUtil();
        return true;
    }

    private void initSensiWordsUtil() {
        String sensiWordsLanguage = ServerConfig.getInstance().getSensiWordsLanguage();
        LogUtil.info("SensiWordsUtils init language:{}", sensiWordsLanguage);
        String[] wordsLanguage = null;
        if (!StringUtils.isEmpty(sensiWordsLanguage)) {
            wordsLanguage = sensiWordsLanguage.replace(" ", "").split(",");
        }
        SensiWordsUtils.init(wordsLanguage);
    }

    private void addWord(String words) {
        maxWordLength = Math.max(maxWordLength, words.length());

        for (int i = 0; i < words.length(); ++i) {
            if (fastCheckMap.containsKey(words.charAt(i))) {
                int val = fastCheckMap.get(words.charAt(i));
                fastCheckMap.put(words.charAt(i), (val | (1 << i)));
            } else {
                fastCheckMap.put(words.charAt(i), 1 << i);
            }
        }
        int mark = 1 << (words.length() - 1);
        if (beginCheckMap.containsKey(words.charAt(0))) {
            beginCheckMap.put(words.charAt(0), (mark | beginCheckMap.get(words.charAt(0))));
        } else {
            beginCheckMap.put(words.charAt(0), mark);
        }

        if (endCheckMap.containsKey(words.charAt(words.length() - 1))) {
            endCheckMap.put(words.charAt(words.length() - 1),
                    (mark | endCheckMap.get(words.charAt(words.length() - 1))));
        } else {
            endCheckMap.put(words.charAt(words.length() - 1), mark);
        }
        badWordSet.add(words);
    }

    /**
     * @param words
     * @param checkSpace 检查空格
     * @return true为没有屏蔽字,
     */
    public boolean checkBadWord(String words, boolean checkSpace) {
        for (int i = 0; i < words.length(); ++i) {
            int count = 0;
            int maxIndex = Math.min(maxWordLength + i, words.length());
            char beginChar = words.charAt(i);
            if (checkSpace && beginChar == ' ') {
                return false;
            }
            for (int j = i; j < maxIndex; j++) {
                char curChar = words.charAt(j);
                int mark = 1 << count;
                if (!fastCheckMap.containsKey(curChar) || (fastCheckMap.get(curChar) & mark) == 0) {
                    break;
                }
                ++count;
                if (endCheckMap.containsKey(curChar) && (endCheckMap.get(curChar) & mark) != 0
                        && beginCheckMap.containsKey(beginChar) && (beginCheckMap.get(beginChar) & mark) != 0) {
                    if (badWordSet.contains(words.substring(i, j + 1))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String replaceBadWord(String words, char replaceChar) {
        StringBuilder strBuff = new StringBuilder();
        for (int i = 0; i < words.length(); ++i) {
            int count = 0;
            int maxIndex = Math.min(maxWordLength + i, words.length());
            char beginChar = words.charAt(i);
            for (int j = i; j < maxIndex; j++) {
                char curChar = words.charAt(j);
                int mark = 1 << count;
                if (!fastCheckMap.containsKey(curChar) || (fastCheckMap.get(curChar) & mark) == 0) {
                    break;
                }
                ++count;
                if (endCheckMap.containsKey(curChar) && (endCheckMap.get(curChar) & mark) != 0
                        && beginCheckMap.containsKey(beginChar) && (beginCheckMap.get(beginChar) & mark) != 0) {
                    if (badWordSet.contains(words.substring(i, j + 1))) {
                        strBuff.append(words, strBuff.length(), i);
                        for (int tmp = i; tmp <= j; ++tmp) {
                            strBuff.append(replaceChar);
                        }
                        i += count - 1;
                        break;
                    }
                }
            }
        }
        if (strBuff.length() < words.length()) {
            strBuff.append(words.substring(strBuff.length()));
        }
        return strBuff.toString();
    }


    public boolean checkPlatformSensitiveWords(String content) {
        return SensiWordsUtils.isLegal(content);
    }

    public String filterSensitiveWords(String content) {
        return SensiWordsUtils.filter(content);
    }
}
