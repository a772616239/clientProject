package server.http.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020.02.27
 */
public class PlatformRetCode {
    /**
     * http请求返回码
     */
    public static class RetCode {
        public static final int success = 0;
        public static final int failed = 1;
    }


    @Setter
    @Getter
    @NoArgsConstructor
    public static class MailAddRet extends PlatformBaseRet {
        private long templateId;

        public MailAddRet(int retCode, String msg) {
            super(retCode, msg);
        }

        public MailAddRet(int retCode, String msg, long templateId) {
            super(retCode, msg);
            this.templateId = templateId;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class PlatformBaseRet {
        private int retCode;
        private String msg;

        public PlatformBaseRet(int retCode) {
            this.retCode = retCode;
        }

        public String toJsonString() {
            return JSONObject.toJSONString(this);
        }
    }

    @Getter
    @Setter
    public static class PlatformCommonResult extends PlatformBaseRet {
        private Object data;

        public PlatformCommonResult(int retCode) {
            super(retCode);
        }

        public PlatformCommonResult(int retCode, String msg) {
            super(retCode, msg);
        }

        public PlatformCommonResult setData(Object data) {
            this.data = data;
            return this;
        }
    }

    @Setter
    @Getter
    public static class PlatformChatResult {
        private int authority;
        private String extInfo;
        private String roleId;
        private String msg;
    }

}
