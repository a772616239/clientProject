package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;

public class MistMonsterDropBoxObject implements baseConfigObject {


    private int groupid;

    private List<List<Integer>> dropboxgroup;

    private int existtime;

    private int totalOdds;

    public void setGroupid(int groupid) {

        this.groupid = groupid;

    }

    public int getGroupid() {

        return this.groupid;

    }


    public void setDropboxgroup(int[][] dropboxgroup) {

        if (dropboxgroup == null) {
            return;
        }
        for (int i = 0; i < dropboxgroup.length; i++) {
            if (dropboxgroup[i] == null) {
                continue;
            }
            if (this.dropboxgroup == null) {
                this.dropboxgroup = new ArrayList<>();
            }
            totalOdds += dropboxgroup[i][0];
            List<Integer> itemTypeList = new ArrayList<>();
            itemTypeList.add(dropboxgroup[i][0]);
            itemTypeList.add(dropboxgroup[i][1]);
            this.dropboxgroup.add(itemTypeList);
        }

    }

    public List<List<Integer>> getDropboxgroup() {

        return this.dropboxgroup;

    }


    public void setExisttime(int existtime) {

        this.existtime = existtime;

    }

    public int getExisttime() {

        return this.existtime;

    }

    public int getTotalOdds() {
        return totalOdds;
    }
}
