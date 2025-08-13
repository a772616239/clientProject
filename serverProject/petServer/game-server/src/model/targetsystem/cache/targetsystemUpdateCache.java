/*CREATED BY TOOL*/

package model.targetsystem.cache;

import model.base.cache.baseUpdateCache;

public class targetsystemUpdateCache extends baseUpdateCache<targetsystemUpdateCache> {

    private static targetsystemUpdateCache instance = null;

    public static targetsystemUpdateCache getInstance() {

        if (instance == null) {
            instance = new targetsystemUpdateCache();
        }
        return instance;

    }


}
