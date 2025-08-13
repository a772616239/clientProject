package cfg;

import lombok.ToString;
import model.base.baseConfigObject;

@ToString
public class AncientCallObject implements baseConfigObject {


    private int id;

    private int type;

    private int rate;

    private int quality;

    private int[] contant;

    private int selectedodds;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setType(int type) {

        this.type = type;

    }

    public int getType() {

        return this.type;

    }


    public void setRate(int rate) {

        this.rate = rate;

    }

    public int getRate() {

        return this.rate;

    }


    public void setQuality(int quality) {

        this.quality = quality;

    }

    public int getQuality() {

        return this.quality;

    }


    public void setContant(int[] contant) {

        this.contant = contant;

    }

    public int[] getContant() {

        return this.contant;

    }


    public void setSelectedodds(int selectedodds) {

        this.selectedodds = selectedodds;

    }

    public int getSelectedodds() {

        return this.selectedodds;

    }


}
