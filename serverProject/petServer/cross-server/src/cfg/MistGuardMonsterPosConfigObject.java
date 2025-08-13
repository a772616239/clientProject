package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;
import protocol.MistForest.ProtoVector;

public class MistGuardMonsterPosConfigObject implements baseConfigObject {


    private int id;

    private List<ProtoVector> patrolposlist;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setPatrolposlist(int[][] patrolposlist) {
        if (null == patrolposlist) {
            return;
        }
        this.patrolposlist = new ArrayList<>();
        ProtoVector.Builder posBuilder = ProtoVector.newBuilder();
        for (int i = 0; i < patrolposlist.length; i++) {
            if (null == patrolposlist[i] || patrolposlist[i].length < 2) {
                continue;
            }
            posBuilder.setX(patrolposlist[i][0]).setY(patrolposlist[i][1]);
            this.patrolposlist.add(posBuilder.build());
        }

    }

    public List<ProtoVector> getPatrolposlist() {

        return this.patrolposlist;

    }


}
