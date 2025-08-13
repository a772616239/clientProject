package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class TheWarConstConfigObject implements baseConfigObject {


    private int id;

    private int preendtime;

    private int rechargepettime;

    private int delaycleargridtime;

    private Map<Integer, Integer> mosterfightstarrecoverenergy;

    private Map<Integer, Integer> playerfightstarrecoverenergy;

    private int[][] buybackcost;

    private int[][] buystamiacost;

    private int bustamiavalue;

    private int petrecoverinterval;

    private int petrecoverrate;

    private int campseasonrankmailid;

    private int attackenemygridmarqueeid;

    private int occupyenemygridmarqueeid;

    private int minpetremainhprate;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setPreendtime(int preendtime) {

        this.preendtime = preendtime;

    }

    public int getPreendtime() {

        return this.preendtime;

    }


    public void setRechargepettime(int rechargepettime) {

        this.rechargepettime = rechargepettime;

    }

    public int getRechargepettime() {

        return this.rechargepettime;

    }


    public void setDelaycleargridtime(int delaycleargridtime) {

        this.delaycleargridtime = delaycleargridtime;

    }

    public int getDelaycleargridtime() {

        return this.delaycleargridtime;

    }


    public void setMosterfightstarrecoverenergy(int[][] mosterfightstarrecoverenergy) {

        if (mosterfightstarrecoverenergy == null || mosterfightstarrecoverenergy.length <= 0) {
            return;
        }
        this.mosterfightstarrecoverenergy = new HashMap<>();
        for (int i = 0; i < mosterfightstarrecoverenergy.length; i++) {
            if (mosterfightstarrecoverenergy[i].length < 2) {
                continue;
            }
            this.mosterfightstarrecoverenergy.put(mosterfightstarrecoverenergy[i][0], mosterfightstarrecoverenergy[i][1]);
        }

    }

    public int getMosterFightRecoverEnergyByFightStar(int fightStar) {
        if (mosterfightstarrecoverenergy == null) {
            return 0;
        }
        return mosterfightstarrecoverenergy.containsKey(fightStar) ? mosterfightstarrecoverenergy.get(fightStar) : 0;
    }


    public void setPlayerfightstarrecoverenergy(int[][] playerfightstarrecoverenergy) {
        if (playerfightstarrecoverenergy == null || playerfightstarrecoverenergy.length <= 0) {
            return;
        }
        this.playerfightstarrecoverenergy = new HashMap<>();
        for (int i = 0; i < playerfightstarrecoverenergy.length; i++) {
            if (playerfightstarrecoverenergy[i].length < 2) {
                continue;
            }
            this.playerfightstarrecoverenergy.put(playerfightstarrecoverenergy[i][0], playerfightstarrecoverenergy[i][1]);
        }
    }

    public int getPlayerFightRecoverEnergyByFightStar(int fightStar) {
        if (playerfightstarrecoverenergy == null) {
            return 0;
        }
        return playerfightstarrecoverenergy.containsKey(fightStar) ? playerfightstarrecoverenergy.get(fightStar) : 0;
    }


    public void setBuybackcost(int[][] buybackcost) {

        this.buybackcost = buybackcost;

    }

    public int[][] getBuybackcost() {

        return this.buybackcost;

    }


    public void setBuystamiacost(int[][] buystamiacost) {

        this.buystamiacost = buystamiacost;

    }

    public int[][] getBuystamiacost() {

        return this.buystamiacost;

    }


    public void setBustamiavalue(int bustamiavalue) {

        this.bustamiavalue = bustamiavalue;

    }

    public int getBustamiavalue() {

        return this.bustamiavalue;

    }


    public void setPetrecoverinterval(int petrecoverinterval) {

        this.petrecoverinterval = petrecoverinterval;

    }

    public int getPetrecoverinterval() {

        return this.petrecoverinterval;

    }


    public void setPetrecoverrate(int petrecoverrate) {

        this.petrecoverrate = petrecoverrate;

    }

    public int getPetrecoverrate() {

        return this.petrecoverrate;

    }


    public void setCampseasonrankmailid(int campseasonrankmailid) {

        this.campseasonrankmailid = campseasonrankmailid;

    }

    public int getCampseasonrankmailid() {

        return this.campseasonrankmailid;

    }


    public void setAttackenemygridmarqueeid(int attackenemygridmarqueeid) {

        this.attackenemygridmarqueeid = attackenemygridmarqueeid;

    }

    public int getAttackenemygridmarqueeid() {

        return this.attackenemygridmarqueeid;

    }


    public void setOccupyenemygridmarqueeid(int occupyenemygridmarqueeid) {

        this.occupyenemygridmarqueeid = occupyenemygridmarqueeid;

    }

    public int getOccupyenemygridmarqueeid() {

        return this.occupyenemygridmarqueeid;

    }


    public void setMinpetremainhprate(int minpetremainhprate) {

        this.minpetremainhprate = minpetremainhprate;

    }

    public int getMinpetremainhprate() {

        return this.minpetremainhprate;

    }


}
