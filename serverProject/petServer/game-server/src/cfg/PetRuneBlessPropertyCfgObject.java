package cfg;

import model.base.baseConfigObject;

public class PetRuneBlessPropertyCfgObject implements baseConfigObject {


    private int runerarity;

    private int[][] blessconsume;

    private int[][] ratingbase;

    private int[][] finalproperty;


    public void setRunerarity(int runerarity) {

        this.runerarity = runerarity;

    }

    public int getRunerarity() {

        return this.runerarity;

    }


    public void setBlessconsume(int[][] blessconsume) {

        this.blessconsume = blessconsume;

    }

    public int[][] getBlessconsume() {

        return this.blessconsume;

    }


    public void setRatingbase(int[][] ratingbase) {

        this.ratingbase = ratingbase;

    }

    public int[][] getRatingbase() {

        return this.ratingbase;

    }


    public void setFinalproperty(int[][] finalproperty) {

        this.finalproperty = finalproperty;

    }

    public int[][] getFinalproperty() {

        return this.finalproperty;

    }


    public int getblesslv() {

        return 0;
    }
}
