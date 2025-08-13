package util;

import cfg.RandomName;
import cfg.ServerStringRes;
import common.GameConst;
import common.IdGenerator;
import common.tick.GlobalTick;
import datatool.StringHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Common.LanguageEnum;

import java.util.Date;
import java.util.List;

public class ObjUtil {

    public static playerEntity createPlayer(String userId, LanguageEnum language) {
        if (StringHelper.isNull(userId)) {
            LogUtil.error("ObjUtil.createPlayer error, userId = " + userId);
            return null;
        }

        String idx = IdGenerator.getInstance().generateId();
        playerEntity player = new playerEntity();
        player.setIdx(idx);
        player.setUserid(userId);
        player.setCreatetime(new Date(GlobalTick.getInstance().getCurrentTime()));
        player.setName(createRandomName(language));
        player.setSex(GameConst.PlayerSex.Male.getCode());
        LogUtil.info("create new player id=" + idx + ",userId=" + userId + ",name=" + player.getName());
        return player;
    }

    public static String createRandomName(LanguageEnum language) {
        String name = "";
        List<Integer> name0List = RandomName.getInstance().getNameList(0);
        List<Integer> name1List = RandomName.getInstance().getNameList(1);
        List<Integer> name2List = RandomName.getInstance().getNameList(2);
        if (CollectionUtils.isEmpty(name0List) || CollectionUtils.isEmpty(name1List) || CollectionUtils.isEmpty(name2List)) {
            return name;
        }
        int length;
        int time = 0;
        do {
            if (time >= 20) {
                break; // 避免死循环
            }
            name = "";
            int name0Index = RandomUtils.nextInt(name0List.size());
            int name1Index = RandomUtils.nextInt(name1List.size());
            int name2Index = RandomUtils.nextInt(name2List.size());
            String rand0Name = ServerStringRes.getContentByLanguage(name0List.get(name0Index), language);
            String rand1Name = ServerStringRes.getContentByLanguage(name1List.get(name1Index), language);
            String rand2Name = ServerStringRes.getContentByLanguage(name2List.get(name2Index), language);
            if (!isEmptyRandName(rand0Name)) {
                name += rand0Name;
            }
            if (!isEmptyRandName(rand1Name)) {
                name += rand1Name;
            }
            if (!isEmptyRandName(rand2Name)) {
                name += rand2Name;
            }
            length = getStringWeight(name);
            time++;
        } while (length == 0 || length > GameConst.ROLE_NAME_MAX_LENGTH || playerCache.getInstance().isNameDuplicated(name));

        if (playerCache.getInstance().isNameDuplicated(name)) {
            for (int i = 0; i < 100; i++) {
                if (!playerCache.getInstance().isNameDuplicated(name + i)) {
                    name += i;
                    break;
                }
            }
            LogUtil.warn("random name duplicate more than 20 times,name =" + name);
        }
        return name;
    }

    public static int getStringWeight(String content) {
        try {
            int weight = 0;
            for (int i = 0; i < content.length(); ++i) {
                int charVal = content.charAt(i);
                if (charVal > 255) {
                    weight += 2;
                } else {
                    weight += 1;
                }
            }
            return weight;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return 0;
        }
    }

    /**
     * 判断指定对象都不为空
     *
     * @param objects
     * @return true:没有一个对象为空，false:一个或者多个对象为空
     */
    public static boolean requireNotNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }

    public static int requireIntOrDefault(Integer value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static boolean isEmptyRandName(String randNameStr) {
        return StringHelper.isNull(randNameStr) || randNameStr.equals("null") || randNameStr.equals("0");
    }


    /**
     * 对象转byte
     *
     * @param obj
     * @return
     */
    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            LogUtil.error("translation" + e.getMessage());
            LogUtil.printStackTrace(e);
        }
        return bytes;
    }


    /**
     * byte转对象
     * @param bytes
     * @return
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

}
