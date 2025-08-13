/*CREATED BY TOOL*/

package model.comment.cache;

import model.base.cache.baseUpdateCache;

public class commentUpdateCache extends baseUpdateCache<commentUpdateCache> {

    private static commentUpdateCache instance = null;

    public static commentUpdateCache getInstance() {

        if (instance == null) {
            instance = new commentUpdateCache();
        }
        return instance;
    }


}
