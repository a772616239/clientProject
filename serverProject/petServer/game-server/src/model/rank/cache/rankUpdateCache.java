/*CREATED BY TOOL*/

package model.rank.cache;

import model.base.cache.baseUpdateCache;

public class rankUpdateCache extends baseUpdateCache<rankUpdateCache> {

    private static rankUpdateCache instance = null;

    public static rankUpdateCache getInstance() {

        if (instance == null) {
            instance = new rankUpdateCache();
        }
        return instance;

    }


}
