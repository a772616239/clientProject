package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfigObject;
import protocol.MistForest.ProtoVector;

public class MistMazeAreaConfigObject implements baseConfigObject {


    private int level;

    private int[] enterpos;

    private int[][] mazetranspos;

    private List<Integer> mazenum;

    private int taskid;


    public void setLevel(int level) {

        this.level = level;

    }

    public int getLevel() {

        return this.level;

    }


    public void setEnterpos(int[] enterpos) {

        this.enterpos = enterpos;

    }

    public int[] getEnterpos() {

        return this.enterpos;

    }


    public void setMazetranspos(int[][] mazetranspos) {
        this.mazetranspos = mazetranspos;
//        if (mazetranspos == null || mazetranspos.length <= 0) {
//            return;
//        }
//
//        this.mazetranspos = new ArrayList<>();
//        ProtoVector.Builder posBuilder = ProtoVector.newBuilder();
//        for (int i = 0; i < mazetranspos.length; i++) {
//            if (mazetranspos[i] == null || mazetranspos[i].length < 2) {
//                continue;
//            }
//            posBuilder.setX(mazetranspos[i][0]);
//            posBuilder.setY(mazetranspos[i][1]);
//            this.mazetranspos.add(posBuilder.build());
//        }
    }

    public int[][] getMazetranspos() {

        return this.mazetranspos;

    }


    public void setMazenum(int[] mazenum) {
        if (mazenum == null || mazenum.length <= 0) {
            return;
        }
        this.mazenum = new ArrayList<>();
        for (int i = 0; i < mazenum.length; i++) {
            this.mazenum.add(mazenum[i]);
        }
    }

    public List<Integer> getMazenum() {

        return this.mazenum;

    }


    public void setTaskid(int taskid) {

        this.taskid = taskid;

    }

    public int getTaskid() {

        return this.taskid;

    }

    public Map<ProtoVector, Integer> getMazeTransPosInfo() {
        Map<ProtoVector, Integer> transPosInfo = new HashMap<>();
        for (int i = 0; i < mazetranspos.length; i++) {
            if (mazetranspos[i] == null || mazetranspos[i].length < 2) {
                continue;
            }
            transPosInfo.put(ProtoVector.newBuilder().setX(mazetranspos[i][0]).setY(mazetranspos[i][1]).build(), mazetranspos[i][2]);
        }
        return transPosInfo;
    }

}
