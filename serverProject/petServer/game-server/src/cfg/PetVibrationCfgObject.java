package cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import model.base.baseConfigObject;

public class PetVibrationCfgObject implements baseConfigObject {


    private int id;

    private int[][] needpet;

    private int[] bufflist;

    private List<Integer> needPetList = new ArrayList<>();


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setNeedpet(int[][] needpet) {

        this.needpet = needpet;
        this.needPetList = Arrays.stream(needpet).map(ints -> ints[0]).collect(Collectors.toList());
    }

    public int[][] getNeedpet() {

        return this.needpet;

    }


    public void setBufflist(int[] bufflist) {

        this.bufflist = bufflist;

    }

    public int[] getBufflist() {

        return this.bufflist;

    }

    public List<Integer> getNeedPetList() {
        return needPetList;
    }
}
