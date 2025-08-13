package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class TheWarItemConfigObject implements baseConfigObject {


    private int itemid;

    private int posdefine;

    private int prodefine;

    private int quality;

    private Map<Integer, Integer> composite; // <itemCfgId, itemCount>

    private int price;

    private int buffid;

    public void setItemid(int itemid) {

        this.itemid = itemid;

    }

    public int getItemid() {

        return this.itemid;

    }


    public void setPosdefine(int posdefine) {

        this.posdefine = posdefine;

    }

    public int getPosdefine() {

        return this.posdefine;

    }


    public void setProdefine(int prodefine) {

        this.prodefine = prodefine;

    }

    public int getProdefine() {

        return this.prodefine;

    }


    public void setQuality(int quality) {

        this.quality = quality;

    }

    public int getQuality() {

        return this.quality;

    }


    public void setComposite(int[][] composite) {
        if (this.composite == null) {
            this.composite = new HashMap<>();
        }
        if (composite == null || composite.length < 0) {
            return;
        }
        for (int i = 0; i < composite.length; i++) {
            this.composite.put(composite[i][0], composite[i][1]);
        }

    }

    public Map<Integer, Integer> getComposite() {

        return this.composite;

    }


    public void setPrice(int price) {

        this.price = price;

    }

    public int getPrice() {

        return this.price;

    }


    public void setBuffid(int buffid) {

        this.buffid = buffid;

    }

    public int getBuffid() {

        return this.buffid;

    }

    private int totalCost;

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }
}
