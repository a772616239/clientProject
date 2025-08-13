package cfg;

import java.util.ArrayList;
import java.util.List;
import model.base.baseConfigObject;

public class RandomNameObject implements baseConfigObject {


    private int id;

    private int pos;

    private List<Integer> svrnamestrid;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setPos(int pos) {

        this.pos = pos;

    }

    public int getPos() {

        return this.pos;

    }


    public void setSvrnamestrid(int[] svrnamestrid) {
        if (svrnamestrid == null) {
            return;
        }
        this.svrnamestrid = new ArrayList<>();
        for (int i = 0; i < svrnamestrid.length; i++) {
            this.svrnamestrid.add(svrnamestrid[i]);
        }

    }

    public List<Integer> getSvrnamestrid() {

        return this.svrnamestrid;

    }


}
