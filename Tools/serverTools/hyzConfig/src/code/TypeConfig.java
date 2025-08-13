package code;

import java.util.HashMap;
import java.util.Map;

/**
 * excel 字段行下标从0开始
 * @author Administrator
 */
public class TypeConfig {

	public static final int SET_JAVA_STR = 1;
	public static final int SET_JSON_STR = 2;

	/**
	 * 是否需要跳过字段,
	 */
	public static final String FIELD_SKIP = "1";
	public static final String FIELD_NOT_SKIP = "0";

	/**
	 * 主键字段
	 */
	public static final String FIELD_PRIMARY_KEY = "1";
	public static final String FIELD_NOT_PRIMARY_KEY = "0";

	/**
	 * 服务器字段标记
	 */
	public static final String FIELD_SERVER_MARK = "s";
	/**
	 * 主键字段标记
	 */
	public static final String FILED_PRIMARY_KEY_MARK = "k";



	public static final int KEY_FIELD = 1;
	public static final int FIELD_NAME_ROW_NUM = 2;
	/**
	 * 该字段为csk字段
	 */
	public static final int FIELD_CSK_ROW_NUM = 3;
	public static final int FIELD_TYPE_ROW_NUM = 4;
	public static final int FIELD_VALUE_START_ROW_NUM = 5;

	public static final int KEY_CACHE_STR = 1;
	public static final int KEY_GET_STR = 2;
	public static final int KEY_PUT_TO_CACHE_STR = 3;

	public static final Map<String, String[]> EXCEL_TYPE_MAPPING_JAVA_TYPE = new HashMap<>();

	static {
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("int".toLowerCase(), new String[] { "int", "1"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("intList".toLowerCase(), new String[] { "int[]", "2"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("intList2".toLowerCase(), new String[] { "int[][]", "3"});

		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("long".toLowerCase(), new String[] { "long", "4"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("longList".toLowerCase(), new String[] { "long[]", "5"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("longList2".toLowerCase(), new String[] { "long[][]", "6"});



		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("string".toLowerCase(),new String[] { "String", "7" });
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("strList".toLowerCase(), new String[] { "String[]", "8"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("strList2".toLowerCase(), new String[] { "String[][]","9"});

		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("boolean".toLowerCase(), new String[] { "boolean", "10"});
		EXCEL_TYPE_MAPPING_JAVA_TYPE.put("float".toLowerCase(),new String[] { "float", "11"});

	}

	public static String getPreFix(String type) {
		type = type.toLowerCase();
		String[] ret = EXCEL_TYPE_MAPPING_JAVA_TYPE.get(type);
		if (ret == null || ret.length < 2) {
			System.out.println("getJavaType is error>>>>" + ret + ">>>type>>>>>" + type);
			System.exit(0);
			return "";
		}

		try {
			return ret[0];
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return "";
		}
	}

	
	public static int getJavaType(String type) {
		type = type.toLowerCase();
		String[] ret = EXCEL_TYPE_MAPPING_JAVA_TYPE.get(type);
		if (ret == null || ret.length < 2) {
			System.out.println("getJavaIntType is error>>>>" + ret + ">>>type>>>>>" + type);
			System.exit(0);
			return 0;
		}
		try {
			return Integer.parseInt(ret[1]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return 0;
		}

	}

	public static void main(String[] args) {
		String aa = "{{\"\"}}";
		System.out.println("aa>>>>" + aa);
	}
}
