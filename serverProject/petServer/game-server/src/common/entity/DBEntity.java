package common.entity;

import util.ObjUtil;

public class DBEntity {

    public byte[] toByteArray() {
        return ObjUtil.ObjectToByte(this);
    }

    public static Object parseFrom(byte[] arr) {
        return ObjUtil.byteToObject(arr);
    }
}
