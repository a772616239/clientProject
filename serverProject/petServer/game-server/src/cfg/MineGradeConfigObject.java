package cfg;

import model.base.baseConfigObject;
import protocol.MineFight.EnumMineQuality;

public class MineGradeConfigObject implements baseConfigObject {


    private int grade;

    private int needexp;

    private int plusfactor_type1;

    private int plusfactor_type2;

    private int plusfactor_type3;

    private int dailylimitplusfactor;


    public void setGrade(int grade) {

        this.grade = grade;

    }

    public int getGrade() {

        return this.grade;

    }


    public void setNeedexp(int needexp) {

        this.needexp = needexp;

    }

    public int getNeedexp() {

        return this.needexp;

    }


    public void setPlusfactor_type1(int plusfactor_type1) {

        this.plusfactor_type1 = plusfactor_type1;

    }

    public int getPlusfactor_type1() {

        return this.plusfactor_type1;

    }


    public void setPlusfactor_type2(int plusfactor_type2) {

        this.plusfactor_type2 = plusfactor_type2;

    }

    public int getPlusfactor_type2() {

        return this.plusfactor_type2;

    }


    public void setPlusfactor_type3(int plusfactor_type3) {

        this.plusfactor_type3 = plusfactor_type3;

    }

    public int getPlusfactor_type3() {

        return this.plusfactor_type3;

    }


    public void setDailylimitplusfactor(int dailylimitplusfactor) {

        this.dailylimitplusfactor = dailylimitplusfactor;

    }

    public int getDailylimitplusfactor() {

        return this.dailylimitplusfactor;

    }

    public int getEffectBonusByMineType(int mineQuality) {
        if (mineQuality == EnumMineQuality.EMQ_Rich_VALUE) {
            return getPlusfactor_type1();
        } else if (mineQuality == EnumMineQuality.EMQ_Normal_VALUE) {
            return getPlusfactor_type2();
        } else if (mineQuality == EnumMineQuality.EMQ_Poor_VALUE) {
            return getPlusfactor_type3();
        } else {
            return 0;
        }
    }
}
