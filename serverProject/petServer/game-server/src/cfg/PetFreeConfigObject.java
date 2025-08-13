package cfg;

import model.base.baseConfigObject;

public class PetFreeConfigObject implements baseConfigObject {


    private int id;

    private int freetype;

    private int lvl;

    private int[][] rewardbylvl;

    private int[][] reorderbylvl;

    private int[][] petcorereward;

    private int[][] petcorereorder;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setFreetype(int freetype) {

        this.freetype = freetype;

    }

    public int getFreetype() {

        return this.freetype;

    }


    public void setLvl(int lvl) {

        this.lvl = lvl;

    }

    public int getLvl() {

        return this.lvl;

    }


    public void setRewardbylvl(int[][] rewardbylvl) {

        this.rewardbylvl = rewardbylvl;

    }

    public int[][] getRewardbylvl() {

        return this.rewardbylvl;

    }


    public void setReorderbylvl(int[][] reorderbylvl) {

        this.reorderbylvl = reorderbylvl;

    }

    public int[][] getReorderbylvl() {

        return this.reorderbylvl;

    }


    public void setPetcorereward(int[][] petcorereward) {

        this.petcorereward = petcorereward;

    }

    public int[][] getPetcorereward() {

        return this.petcorereward;

    }


    public void setPetcorereorder(int[][] petcorereorder) {

        this.petcorereorder = petcorereorder;

    }

    public int[][] getPetcorereorder() {

        return this.petcorereorder;

    }


}
