/*CREATED BY TOOL*/

package model.gameplay.cache;

import model.base.cache.baseUpdateCache;

public class gameplayUpdateCache extends baseUpdateCache<gameplayUpdateCache> {

    private static gameplayUpdateCache instance = null;

    public static gameplayUpdateCache getInstance() {

        if (instance == null) {
            instance = new gameplayUpdateCache();
        }
        return instance;

    }


}
