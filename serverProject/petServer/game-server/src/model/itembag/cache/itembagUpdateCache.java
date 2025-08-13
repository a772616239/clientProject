/*CREATED BY TOOL*/

package model.itembag.cache;

import model.base.cache.baseUpdateCache;

public class itembagUpdateCache extends baseUpdateCache<itembagUpdateCache> {

    private static itembagUpdateCache instance = null;

    public static itembagUpdateCache getInstance() {

        if (instance == null) {
            instance = new itembagUpdateCache();
        }
        return instance;

    }


}
