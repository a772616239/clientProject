package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class MistLootPackCarryConfigObject implements baseConfigObject {


    private int id;

    private int rewardtype;

    private int rewardid;

    private Map<Integer, Integer> carrylimit;

    private int[] needalchemynum;

    private int alchemyrewardcount;

    private int[][] alchemyexhangereward;

    private boolean onlyuseinmist;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setRewardtype(int rewardtype) {

        this.rewardtype = rewardtype;

    }

    public int getRewardtype() {

        return this.rewardtype;

    }


    public void setRewardid(int rewardid) {

        this.rewardid = rewardid;

    }

    public int getRewardid() {

        return this.rewardid;

    }


    public void setCarrylimit(int[][] carrylimit) {
        if (carrylimit == null || carrylimit.length <= 0) {
            return;
        }
        this.carrylimit = new HashMap<>();
        for (int i = 0; i < carrylimit.length; i++) {
            if (carrylimit[i] == null || carrylimit[i].length < 2) {
                continue;
            }
            this.carrylimit.put(carrylimit[i][0], carrylimit[i][1]);
        }
    }

    public Map<Integer, Integer> getCarrylimit() {

        return this.carrylimit;

    }


    public void setNeedalchemynum(int[] needalchemynum) {

        this.needalchemynum = needalchemynum;

    }

    public int[] getNeedalchemynum() {

        return this.needalchemynum;

    }


    public void setAlchemyrewardcount(int alchemyrewardcount) {

        this.alchemyrewardcount = alchemyrewardcount;

    }

    public int getAlchemyrewardcount() {

        return this.alchemyrewardcount;

    }


    public void setAlchemyexhangereward(int[][] alchemyexhangereward) {

        this.alchemyexhangereward = alchemyexhangereward;

    }

    public int[][] getAlchemyexhangereward() {

        return this.alchemyexhangereward;

    }


    public void setOnlyuseinmist(boolean onlyuseinmist) {

        this.onlyuseinmist = onlyuseinmist;

    }

    public boolean getOnlyuseinmist() {

        return this.onlyuseinmist;

    }

    public int getLimitByRule(int rule) {
        Integer limit = this.carrylimit.get(rule);
        return limit != null ? limit : 0;
    }
}
