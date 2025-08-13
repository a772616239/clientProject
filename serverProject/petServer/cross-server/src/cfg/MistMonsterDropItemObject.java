package cfg;

import model.base.baseConfigObject;

import java.util.ArrayList;
import java.util.List;

public class MistMonsterDropItemObject implements baseConfigObject {


    private int groupid;

    private int totalOdds;

    private List<List<Integer>> dropitemgroup;


    public void setGroupid(int groupid) {

        this.groupid = groupid;

    }

    public int getGroupid() {

        return this.groupid;

    }

    public int getTotalOdds() {
        return totalOdds;
    }

    public void setTotalOdds(int totalOdds) {
        this.totalOdds = totalOdds;
    }

    public void setDropitemgroup(int[][] dropitemgroup) {
        if (dropitemgroup == null) {
            return;
        }
        for (int i = 0; i < dropitemgroup.length; i++) {
            if (dropitemgroup[i] == null) {
                continue;
            }
            if (this.dropitemgroup == null) {
                this.dropitemgroup = new ArrayList<>();
            }
            totalOdds += dropitemgroup[i][0];
            List<Integer> itemTypeList = new ArrayList<>();
            itemTypeList.add(dropitemgroup[i][0]);
            itemTypeList.add(dropitemgroup[i][1]);
            this.dropitemgroup.add(itemTypeList);
        }

    }

    public List<List<Integer>> getDropitemgroup() {

        return this.dropitemgroup;

    }


}
