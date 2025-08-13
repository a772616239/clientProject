package cfg;

import model.base.baseConfigObject;

public class MineGiftBaseCfgObject implements baseConfigObject {


    private int cfgid;

    private int existtime;

    private int effecttime;

    private int effecttype;

    private int effectvalue;


    public void setCfgid(int cfgid) {

        this.cfgid = cfgid;

    }

    public int getCfgid() {

        return this.cfgid;

    }


    public void setExisttime(int existtime) {

        this.existtime = existtime;

    }

    public int getExisttime() {

        return this.existtime;

    }


    public void setEffecttime(int effecttime) {

        this.effecttime = effecttime;

    }

    public int getEffecttime() {

        return this.effecttime;

    }


    public void setEffecttype(int effecttype) {

        this.effecttype = effecttype;

    }

    public int getEffecttype() {

        return this.effecttype;

    }


    public void setEffectvalue(int effectvalue) {

        this.effectvalue = effectvalue;

    }

    public int getEffectvalue() {

        return this.effectvalue;

    }


}
