/*CREATED BY TOOL*/

package model.magicthron.cache;

import model.base.cache.baseUpdateCache;

public class magicUpdateCache extends baseUpdateCache<magicUpdateCache> {

    private static magicUpdateCache instance = null;

    public static magicUpdateCache getInstance() {

        if (instance == null) {
            instance = new magicUpdateCache();
        }
        return instance;

    }


}
