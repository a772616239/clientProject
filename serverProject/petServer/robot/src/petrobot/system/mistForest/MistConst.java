package petrobot.system.mistForest;

import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.PropertyDict;
import protocol.MistForest.ProtoVector;

public class MistConst {
    public static long parsePropertyLongValue(PropertyDict propertyDict, MistUnitPropTypeEnum propType) {
        try {
            if (propertyDict == null) {
                return 0;
            }
            int index = -1;
            for (int i = 0; i < propertyDict.getKeysCount(); i++) {
                MistUnitPropTypeEnum key = propertyDict.getKeys(i);
                if (key == propType) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                return 0;
            }
            return propertyDict.getValues(index);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int parsePropertyIntValue(PropertyDict propertyDictList, MistUnitPropTypeEnum propType) {
        return (int) parsePropertyLongValue(propertyDictList, propType);
    }

    public static String parsePropertyStringValue(PropertyDict propertyDictList, MistUnitPropTypeEnum propType) {
        long value = parsePropertyLongValue(propertyDictList, propType);
        return String.valueOf(value);
    }

    public static boolean isSamePos(ProtoVector.Builder pos1, ProtoVector.Builder pos2) {
        if (pos1 == null || pos2 == null) {
            return false;
        }
        return isSamePos(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
    }

    public static boolean isSamePos(int x1, int y1, int x2, int y2) {
        return x1 == x2 && y1 == y2;
    }

    public static ProtoVector.Builder calcStanderCoordVector(int sourceX, int sourceY, int targetX, int targetY) {
        // (根号2)分之1 约为0.7
        ProtoVector.Builder builder = ProtoVector.newBuilder();
        if (targetX - sourceX > 0) {
            if (targetY - sourceY > 0) {
                builder.setX(7);
                builder.setY(7);
            } else if (targetY - sourceY < 0) {
                builder.setX(7);
                builder.setY(-7);
            } else {
                builder.setX(10);
                builder.setY(0);
            }
        } else if (targetX - sourceX < 0) {
            if (targetY - sourceY > 0) {
                builder.setX(-7);
                builder.setY(7);
            } else if (targetY - sourceY < 0) {
                builder.setX(-7);
                builder.setY(-7);
            } else {
                builder.setX(-10);
                builder.setY(0);
            }
        } else {
            if (targetY - sourceY > 0) {
                builder.setX(0);
                builder.setY(10);
            } else if (targetY - sourceY < 0) {
                builder.setX(0);
                builder.setY(-10);
            } else {
                builder.setX(0);
                builder.setY(0);
            }
        }
        return builder;
    }
}
