package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class PlayerLevelConfigObject implements baseConfigObject {


    private int level;

    private int experience;

    private int[][] petmissioncfg;

    private int arenafightmake;

    private int arenabattlerewardfactor;

    private Map<Integer, Integer> mistdailyrewardlimit;

    private int mistbattlerewarditemid;

    private int mistbattlerewarcount;

    private int mistrbagrewarcount;

    private int[][] bravefightreward;

    private int[] gloryroadquizconsume;

    private int[][] matcharenagift;

    private int[][] matcharenafullgift;

    private int[][] matcharenamatchwinreward;

    private int[][] matcharenamatchfailreward;

    private int[][] matcharenamatchfullreward;

    private int[][] redbagrandomrewards;


    public void setLevel(int level) {

        this.level = level;

    }

    public int getLevel() {

        return this.level;

    }


    public void setExperience(int experience) {

        this.experience = experience;

    }

    public int getExperience() {

        return this.experience;

    }


    public void setPetmissioncfg(int[][] petmissioncfg) {

        this.petmissioncfg = petmissioncfg;

    }

    public int[][] getPetmissioncfg() {

        return this.petmissioncfg;

    }


    public void setArenafightmake(int arenafightmake) {

        this.arenafightmake = arenafightmake;

    }

    public int getArenafightmake() {

        return this.arenafightmake;

    }


    public void setArenabattlerewardfactor(int arenabattlerewardfactor) {

        this.arenabattlerewardfactor = arenabattlerewardfactor;

    }

    public int getArenabattlerewardfactor() {

        return this.arenabattlerewardfactor;

    }


    public void setMistdailyrewardlimit(int[][] mistdailyrewardlimit) {
        if (mistdailyrewardlimit == null || mistdailyrewardlimit.length <= 0) {
            return;
        }
        this.mistdailyrewardlimit = new HashMap<>();
        for (int i = 0; i < mistdailyrewardlimit.length; i++) {
            if (mistdailyrewardlimit[i] == null || mistdailyrewardlimit[i].length < 2) {
                continue;
            }
            this.mistdailyrewardlimit.put(mistdailyrewardlimit[i][0], mistdailyrewardlimit[i][1]);
        }
    }

    public Map<Integer, Integer> getMistdailyrewardlimit() {

        return this.mistdailyrewardlimit;

    }

    public int getLimitByRule(int rule) {
        Integer limit = this.mistdailyrewardlimit.get(rule);
        return limit != null ? limit : 0;
    }


    public void setMistbattlerewarditemid(int mistbattlerewarditemid) {

        this.mistbattlerewarditemid = mistbattlerewarditemid;

    }

    public int getMistbattlerewarditemid() {

        return this.mistbattlerewarditemid;

    }


    public void setMistbattlerewarcount(int mistbattlerewarcount) {

        this.mistbattlerewarcount = mistbattlerewarcount;

    }

    public int getMistbattlerewarcount() {

        return this.mistbattlerewarcount;

    }


    public void setMistrbagrewarcount(int mistrbagrewarcount) {

        this.mistrbagrewarcount = mistrbagrewarcount;

    }

    public int getMistrbagrewarcount() {

        return this.mistrbagrewarcount;

    }


    public void setBravefightreward(int[][] bravefightreward) {

        this.bravefightreward = bravefightreward;

    }

    public int[][] getBravefightreward() {

        return this.bravefightreward;

    }


    public void setGloryroadquizconsume(int[] gloryroadquizconsume) {

        this.gloryroadquizconsume = gloryroadquizconsume;

    }

    public int[] getGloryroadquizconsume() {

        return this.gloryroadquizconsume;

    }


    public void setMatcharenagift(int[][] matcharenagift) {

        this.matcharenagift = matcharenagift;

    }

    public int[][] getMatcharenagift() {

        return this.matcharenagift;

    }


    public void setMatcharenafullgift(int[][] matcharenafullgift) {

        this.matcharenafullgift = matcharenafullgift;

    }

    public int[][] getMatcharenafullgift() {

        return this.matcharenafullgift;

    }


    public void setMatcharenamatchwinreward(int[][] matcharenamatchwinreward) {

        this.matcharenamatchwinreward = matcharenamatchwinreward;

    }

    public int[][] getMatcharenamatchwinreward() {

        return this.matcharenamatchwinreward;

    }


    public void setMatcharenamatchfailreward(int[][] matcharenamatchfailreward) {

        this.matcharenamatchfailreward = matcharenamatchfailreward;

    }

    public int[][] getMatcharenamatchfailreward() {

        return this.matcharenamatchfailreward;

    }


    public void setMatcharenamatchfullreward(int[][] matcharenamatchfullreward) {

        this.matcharenamatchfullreward = matcharenamatchfullreward;

    }

    public int[][] getMatcharenamatchfullreward() {

        return this.matcharenamatchfullreward;

    }


    public void setRedbagrandomrewards(int[][] redbagrandomrewards) {

        this.redbagrandomrewards = redbagrandomrewards;

    }

    public int[][] getRedbagrandomrewards() {

        return this.redbagrandomrewards;

    }


}
