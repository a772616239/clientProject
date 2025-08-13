package common.load;

import db.config.AppContext;
import db.core.BaseDAO;
import util.LogUtil;

import java.util.Map;

public class DbCheck {
    private static final String idx = "0";

    public static boolean check() {
        try {
            BaseDAO dao;
            String daoName;
            for (Map.Entry<String, BaseDAO> entry : AppContext.daos.entrySet()) {
                daoName = entry.getKey();
                dao = entry.getValue();
                LogUtil.info("check DB>>>>>" + daoName);
                dao.test();
                dao.delete(idx);
            }
            return true;

        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}
