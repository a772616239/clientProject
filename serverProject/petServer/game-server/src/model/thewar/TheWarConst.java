package model.thewar;

import cfg.TheWarConstConfig;
import cfg.TheWarConstConfigObject;
import common.GameConst;
import model.consume.ConsumeUtil;
import protocol.Common.Consume;

public class TheWarConst {
    public static int getDailyLimitBuyBackTimes() {
        TheWarConstConfigObject cfg = TheWarConstConfig.getById(GameConst.CONFIG_ID);
        if (cfg == null || cfg.getBuybackcost() == null) {
            return 0;
        }
        return cfg.getBuybackcost().length;
    }

    public static Consume getBuyBackConsume(int dailyTimes) {
        if (dailyTimes < 0) {
            return null;
        }
        TheWarConstConfigObject cfg = TheWarConstConfig.getById(GameConst.CONFIG_ID);
        if (cfg == null || cfg.getBuybackcost() == null || dailyTimes >= cfg.getBuybackcost().length) {
            return null;
        }
        return ConsumeUtil.parseConsume(cfg.getBuybackcost()[dailyTimes]);
    }

    public static int getDailyLimitBuyStaminaTimes() {
        TheWarConstConfigObject cfg = TheWarConstConfig.getById(GameConst.CONFIG_ID);
        if (cfg == null || cfg.getBuystamiacost() == null) {
            return 0;
        }
        return cfg.getBuystamiacost().length;
    }

    public static Consume getBuyStaminaConsume(int dailyTimes) {
        if (dailyTimes < 0) {
            return null;
        }
        TheWarConstConfigObject cfg = TheWarConstConfig.getById(GameConst.CONFIG_ID);
        if (cfg == null || cfg.getBuystamiacost() == null || dailyTimes >= cfg.getBuystamiacost().length) {
            return null;
        }
        return ConsumeUtil.parseConsume(cfg.getBuystamiacost()[dailyTimes]);
    }
}
