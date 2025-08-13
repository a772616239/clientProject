/*CREATED BY TOOL*/

package model.farmmine.cache;

import model.base.cache.baseUpdateCache;

public class farmmineUpdateCache extends baseUpdateCache<farmmineUpdateCache> {

    private static farmmineUpdateCache instance = null;

    public static farmmineUpdateCache getInstance() {

        if (instance == null) {
            instance = new farmmineUpdateCache();
        }
        return instance;

    }


}
