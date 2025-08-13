package cfg;

import java.util.HashMap;
import java.util.Map;
import model.base.baseConfigObject;

public class MistStrangeGrassConfigObject implements baseConfigObject {


    private int id;

    private int generateobjtype;

    private Map<Integer, Long> generateobjprop;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setGenerateobjtype(int generateobjtype) {

        this.generateobjtype = generateobjtype;

    }

    public int getGenerateobjtype() {

        return this.generateobjtype;

    }


    public void setGenerateobjprop(int[][] generateobjprop) {
        if (this.generateobjprop == null) {
            this.generateobjprop = new HashMap<>();
        }
        for (int i = 0; i < generateobjprop.length; ++i) {
            if (generateobjprop[i].length < 2) {
                throw new IndexOutOfBoundsException();
            } else {
                this.generateobjprop.put(generateobjprop[i][0], (long) generateobjprop[i][1]);
            }
        }
    }

    public Map<Integer, Long> getGenerateobjprop() {

        return this.generateobjprop;

    }


}
