package cfg;

import java.util.Map;
import java.util.Random;
import model.base.baseConfigObject;
import util.TimeUtil;

public class TheWarDropItemConfigObject implements baseConfigObject {


    private int id;

    private int baseafktime;

    private int[][] itemdropodds;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setBaseafktime(int baseafktime) {

        this.baseafktime = baseafktime;

    }

    public int getBaseafktime() {

        return this.baseafktime;

    }


    public void setItemdropodds(int[][] itemdropodds) {
        if (itemdropodds != null && itemdropodds.length > 0) {
            for (int i = 0; i < itemdropodds.length; i++) {
                if (itemdropodds[i].length < 2) {
                    continue;
                }
                totalDropOdds += itemdropodds[i][1];
            }
        }
        this.itemdropodds = itemdropodds;

    }

    public int[][] getItemdropodds() {

        return this.itemdropodds;

    }

    private int totalDropOdds;

    public void calcDropItems(long afkTime, Map<Integer, Integer> itemDataMap) {
        if (itemDataMap == null || itemdropodds == null || totalDropOdds <= 0 || baseafktime <= 0) {
            return;
        }
        int odds;
        int sumOdds;
        Random random = new Random();
        for (int i = 0; i < afkTime / TimeUtil.MS_IN_A_MIN / baseafktime; i++) {
            sumOdds = 0;
            odds = random.nextInt(totalDropOdds);
            for (int j = 0; j < itemdropodds.length; j++) {
                if (itemdropodds[j].length < 2) {
                    continue;
                }
                sumOdds += itemdropodds[j][1];
                if (odds <= sumOdds) {
                    itemDataMap.merge(itemdropodds[j][0], 1, (oldVal, newVal) -> oldVal + newVal);
                    break;
                }
            }
        }
    }

}
