package petrobot.system.mistForest.obj;

import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.RobotMistForest;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MistObj {
    protected long id;
    protected RobotMistForest robotMistForest;
    protected MistUnitTypeEnum unitType;
    protected Map<MistUnitPropTypeEnum, Long> attributes;
    protected ProtoVector.Builder pos;

    public MistObj() {
        attributes = new HashMap<>();
        pos = ProtoVector.newBuilder();
    }

    public void init(UnitMetadata metadata) {
        setUnitType(metadata.getUnitType());

        for (int i = 0; i < metadata.getProperties().getKeysCount(); i++) {
            attributes.put(metadata.getProperties().getKeys(i), metadata.getProperties().getValues(i));
        }

        int x = metadata.getSnapShotData().getPos().getX() / 1000 * 1000 + 500;
        int y = metadata.getSnapShotData().getPos().getY() / 1000 * 1000 + 500;
        pos.setX(x).setY(y);
    }

    public void clear() {
        robotMistForest = null;
        attributes.clear();
        pos.clear();
    }

    public long getAttribute(MistUnitPropTypeEnum propType) {
        return attributes.containsKey(propType) ? attributes.get(propType) : 0;
    }

    public void setAttribute(MistUnitPropTypeEnum propType, long newValue) {
        attributes.put(propType, newValue);
    }

    public void onTick() {

    }
}