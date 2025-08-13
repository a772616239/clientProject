/*CREATED BY TOOL*/

package model.training.cache;

import model.base.cache.baseUpdateCache;

public class trainingUpdateCache extends baseUpdateCache<trainingUpdateCache> {

    private static trainingUpdateCache instance = null;

    public static trainingUpdateCache getInstance() {

        if (instance == null) {
            instance = new trainingUpdateCache();
        }
        return instance;

    }


}
