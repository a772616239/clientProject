package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class MistMapObjConfigObject implements baseConfigObject {


    private int id;

    private int maprule;

    private int maplevel;

    private int objtype;

    private int delayborntime;

    private int[] initpos;

    private int[] inittoward;

    private Map<Integer, Long> initprop;

    private int[] rebornpropchangeinfo;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setMaprule(int maprule) {

        this.maprule = maprule;

    }

    public int getMaprule() {

        return this.maprule;

    }


    public void setMaplevel(int maplevel) {

        this.maplevel = maplevel;

    }

    public int getMaplevel() {

        return this.maplevel;

    }


    public void setObjtype(int objtype) {

        this.objtype = objtype;

    }

    public int getObjtype() {

        return this.objtype;

    }


    public void setDelayborntime(int delayborntime) {

        this.delayborntime = delayborntime;

    }

    public int getDelayborntime() {

        return this.delayborntime;

    }


    public void setInitpos(int[] initpos) {

        if (initpos.length < 2) {
            this.initpos = null;
        }
        this.initpos = initpos;

    }

    public int[] getInitpos() {

        return this.initpos;

    }


    public void setInittoward(int[] inittoward) {

        if (initpos.length < 2) {
            this.inittoward = null;
        }
        this.inittoward = inittoward;

    }

    public int[] getInittoward() {

        return this.inittoward;

    }


    public void setInitprop(int[][] initprop) {

        if (this.initprop == null) {
            this.initprop = new HashMap<>();
        }
        for (int i = 0; i < initprop.length; ++i) {
            if (initprop[i].length < 2) {
                throw new IndexOutOfBoundsException();
            } else {
                this.initprop.put(initprop[i][0], (long) initprop[i][1]);
            }
        }

    }

    public Map<Integer, Long> getInitprop() {

        return this.initprop;

    }

    public void setRebornpropchangeinfo(int[] rebornpropchangeinfo) {
        this.rebornpropchangeinfo = rebornpropchangeinfo;
    }

    public int[] getRebornpropchangeinfo() {

        return this.rebornpropchangeinfo;

    }


}
