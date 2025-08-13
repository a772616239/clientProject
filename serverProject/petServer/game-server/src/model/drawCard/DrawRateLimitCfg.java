package model.drawCard;

import java.util.HashMap;
import java.util.Map;

public class DrawRateLimitCfg {
    private Map<Integer, Map<Integer, DrawPetRateBean>> coreKeyCommon = new HashMap<Integer, Map<Integer, DrawPetRateBean>>();
    private Map<Integer, DrawPetRateBean> petspKeyCommon = new HashMap<Integer, DrawPetRateBean>();

    private Map<Integer, Map<Integer, DrawPetRateBean>> coreKeyHigh = new HashMap<Integer, Map<Integer, DrawPetRateBean>>();
    private Map<Integer, DrawPetRateBean> petspKeyhigh = new HashMap<Integer, DrawPetRateBean>();

    private Map<Integer, DrawPetRateBean> petspKeyAncient = new HashMap<Integer, DrawPetRateBean>();

    public Map<Integer, Map<Integer, DrawPetRateBean>> getCoreKeyCommon() {
        return coreKeyCommon;
    }

    public void setCoreKeyCommon(Map<Integer, Map<Integer, DrawPetRateBean>> coreKeyCommon) {
        this.coreKeyCommon = coreKeyCommon;
    }

    public Map<Integer, DrawPetRateBean> getPetspKeyCommon() {
        return petspKeyCommon;
    }

    public void setPetspKeyCommon(Map<Integer, DrawPetRateBean> petspKeyCommon) {
        this.petspKeyCommon = petspKeyCommon;
    }

    public Map<Integer, Map<Integer, DrawPetRateBean>> getCoreKeyHigh() {
        return coreKeyHigh;
    }

    public void setCoreKeyHigh(Map<Integer, Map<Integer, DrawPetRateBean>> coreKeyHigh) {
        this.coreKeyHigh = coreKeyHigh;
    }

    public Map<Integer, DrawPetRateBean> getPetspKeyhigh() {
        return petspKeyhigh;
    }

    public void setPetspKeyhigh(Map<Integer, DrawPetRateBean> petspKeyhigh) {
        this.petspKeyhigh = petspKeyhigh;
    }

    public Map<Integer, DrawPetRateBean> getPetspKeyAncient() {
        return petspKeyAncient;
    }

    public void setPetspKeyAncient(Map<Integer, DrawPetRateBean> petspKeyAncient) {
        this.petspKeyAncient = petspKeyAncient;
    }
}
