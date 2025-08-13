/*CREATED BY TOOL*/

package model.mainLine.cache;

import model.base.cache.baseUpdateCache;

public class mainlineUpdateCache extends baseUpdateCache<mainlineUpdateCache> {

    private static mainlineUpdateCache instance = null;

    public static mainlineUpdateCache getInstance() {

        if (instance == null) {
            instance = new mainlineUpdateCache();
        }
        return instance;

    }


}
