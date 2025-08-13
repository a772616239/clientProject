/*CREATED BY TOOL*/

package model.crossarena.cache;

import model.base.cache.baseUpdateCache;

public class playercrossarenaUpdateCache extends baseUpdateCache<playercrossarenaUpdateCache> {

    private static playercrossarenaUpdateCache instance = null;

    public static playercrossarenaUpdateCache getInstance() {

        if (instance == null) {
            instance = new playercrossarenaUpdateCache();
        }
        return instance;
    }

}
