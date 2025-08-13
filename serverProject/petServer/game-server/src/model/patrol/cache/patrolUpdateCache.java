/*CREATED BY TOOL*/

package model.patrol.cache;

import model.base.cache.baseUpdateCache;

public class patrolUpdateCache extends baseUpdateCache<patrolUpdateCache> {

    private static patrolUpdateCache instance = null;

    public static patrolUpdateCache getInstance() {

        if (instance == null) {
            instance = new patrolUpdateCache();
        }
        return instance;

    }


}
