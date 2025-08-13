package cfg;

import model.base.baseConfigObject;

import java.util.HashMap;

public class MistObjDefaultPropObject implements baseConfigObject {


    private int id;

    private int objtype;

    private int[] initpos;

    private int[] inittoward;

    private HashMap<Integer, Long> defaultprop;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setObjtype(int objtype) {

        this.objtype = objtype;

    }

    public int getObjtype() {

        return this.objtype;

    }


    public void setInitpos(int[] initpos) {

        if (initpos.length < 2) {
            throw new IndexOutOfBoundsException();
        }
        this.initpos = initpos;

    }

    public int[] getInitpos() {

        return this.initpos;

    }


    public void setInittoward(int[] inittoward) {

        if (initpos.length < 2) {
            throw new IndexOutOfBoundsException();
        }
        this.inittoward = inittoward;

    }

    public int[] getInittoward() {

        return this.inittoward;

    }


    public void setDefaultprop(int[][] defaultprop) {

        if (this.defaultprop == null) {
            this.defaultprop = new HashMap<>();
        }
        for (int i = 0; i < defaultprop.length; ++i) {
            if (defaultprop[i].length < 2) {
                throw new IndexOutOfBoundsException();
            } else {
                this.defaultprop.put(defaultprop[i][0], (long) defaultprop[i][1]);
            }
        }

    }

    public HashMap<Integer, Long> getDefaultprop() {

        return this.defaultprop;

    }


}
