package cfg;

import model.base.baseConfigObject;

public class MistBattleConfigObject implements baseConfigObject {


    private int id;

    private int level;

    private int battletype;

    private int[] fightmakeid;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setLevel(int level) {

        this.level = level;

    }

    public int getLevel() {

        return this.level;

    }


    public void setBattletype(int battletype) {

        this.battletype = battletype;

    }

    public int getBattletype() {

        return this.battletype;

    }


    public void setFightmakeid(int[] fightmakeid) {

        this.fightmakeid = fightmakeid;

    }

    public int[] getFightmakeid() {

        return this.fightmakeid;

    }
}
