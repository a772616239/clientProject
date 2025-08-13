/*CREATED BY TOOL*/

package model.foreignInvasion.cache;

import model.base.cache.baseUpdateCache;

public class foreigninvasionUpdateCache extends baseUpdateCache<foreigninvasionUpdateCache> {

    private static foreigninvasionUpdateCache instance = null;

    public static foreigninvasionUpdateCache getInstance() {

        if (instance == null) {
            instance = new foreigninvasionUpdateCache();
        }
        return instance;

    }


}
