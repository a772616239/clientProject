package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

/**
 * 字符串解析成对象
 * 
 * @author Autumn
 *
 */
public class StrObjUtil {

	/**
	 * 1_10:33,11:20=2_10:33,11:20=3_10:33,11:20=将Str通过解析并存入Map<Integer, Map<Integer, Integer>>
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, Map<Integer, Integer>> toMapMapInt(String str) {
		Map<Integer, Map<Integer, Integer>> result = new LinkedHashMap<Integer, Map<Integer, Integer>>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DENGHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.XIAHUAXIAN);
				if (pair != null && pair.length > 1) {
					int id = NumberUtils.toInt(pair[0]);
					Map<Integer, Integer> twoMap = toMapInt(pair[1]);
					result.put(id, twoMap);
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}
	
	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, List<Integer>> toMapListInt(String str) {
		Map<Integer, List<Integer>> result = new LinkedHashMap<Integer, List<Integer>>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					int id = NumberUtils.toInt(pair[0]);
					List<Integer> tempList = new ArrayList<Integer>();
					for (int i = 1; i < pair.length; i++) {
						tempList.add(NumberUtils.toInt(pair[i]));
					}
					result.put(id, tempList);
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, Integer> toMapInt(String str) {
		Map<Integer, Integer> result = new LinkedHashMap<Integer, Integer>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					int id = NumberUtils.toInt(pair[0]);
					int num = NumberUtils.toInt(pair[1]);
					if (result.containsKey(id)) {
						num += result.get(id);
					}
					result.put(id, num);
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, Float> toMapFloat(String str) {
		Map<Integer, Float> result = new LinkedHashMap<Integer, Float>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					int id = NumberUtils.toInt(pair[0]);
					float num = NumberUtils.toFloat(pair[1]);
					if (result.containsKey(id)) {
						num += result.get(id);
					}
					result.put(id, num);
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Float, Integer> toFloatMap(String str) {
		Map<Float, Integer> result = new LinkedHashMap<Float, Integer>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					result.put(NumberUtils.toFloat(pair[0]), NumberUtils.toInt(pair[1]));
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, Long> toMapLong(String str) {
		Map<Integer, Long> result = new LinkedHashMap<Integer, Long>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					result.put(NumberUtils.toInt(pair[0]), NumberUtils.toLong(pair[1]));
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Long, Integer> toMapLongInt(String str) {
		Map<Long, Integer> result = new LinkedHashMap<Long, Integer>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					result.put(NumberUtils.toLong(pair[0]), NumberUtils.toInt(pair[1]));
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, Byte> toMapByte(String str) {
		Map<Integer, Byte> result = new LinkedHashMap<Integer, Byte>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					result.put(NumberUtils.toInt(pair[0]), NumberUtils.toByte(pair[1]));
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}
	/**
	 * 1:2,3:4----->{[1,2],[3,4]}将Str通过“，”，“：”解析并存入map
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Integer, String> toMapString(String str) {
		Map<Integer, String> result = new LinkedHashMap<Integer, String>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		try {
			String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
			if (pairs == null) {
				return result;
			}
			for (String str1 : pairs) {
				String[] pair = StringUtils.split(str1, Symbol.MAOHAO);
				if (pair != null && pair.length > 1) {
					result.put(NumberUtils.toInt(pair[0]), pair[1]);
				}
			}
		} catch (Exception e) {
			LogUtil.error("工具类，字符串解析异常。", e);
		}
		return result;
	}

	/**
	 * 逗号隔开转换成list
	 * 
	 * @param str
	 * @return
	 */
	public static List<Byte> toListByte(String str) {
		List<Byte> result = new ArrayList<Byte>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			result.add(NumberUtils.toByte(str1));
		}
		return result;
	}

	/**
	 * 逗号隔开转换成list,去重
	 * 
	 * @param str
	 * @return
	 */
	public static List<Integer> toListInt(String str) {
		List<Integer> result = new ArrayList<Integer>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			int i = NumberUtils.toInt(str1);
			if (!result.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * 逗号隔开转换成list,去重
	 * 
	 * @param str
	 * @return
	 */
	public static List<Long> toListLong(String str) {
		List<Long> result = new ArrayList<Long>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			long i = NumberUtils.toLong(str1);
			if (!result.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * 逗号隔开转换成list,元素值可以相同
	 * 
	 * @param str
	 * @return
	 */
	public static List<Integer> toListInt2(String str) {
		List<Integer> result = new ArrayList<Integer>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			int i = NumberUtils.toInt(str1);
			result.add(i);
		}
		return result;
	}
	/**
	 * 逗号隔开转换成list
	 * 
	 * @param str
	 * @return
	 */
	public static List<Float> toListFloat(String str) {
		List<Float> result = new ArrayList<Float>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			result.add(NumberUtils.toFloat(str1));
		}
		return result;
	}

	/**
	 * 逗号隔开转换成list
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> toListString(String str, String fuhao) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, fuhao);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			result.add(str1);
		}
		return result;
	}

	/**
	 * 逗号隔开转换成list
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> toListString(String str) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.DOUHAO);
		if (pairs == null) {
			return result;
		}
		for (String str1 : pairs) {
			result.add(str1);
		}
		return result;
	}
	/**
	 * 分号隔开逗号隔开转换成list矩阵
	 * 
	 * @param str
	 * @return
	 */
	public static List<List<Integer>> toListListInt(String str) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.FENHAO);
		for (String s1 : pairs) {
			List<Integer> result1 = new ArrayList<Integer>();
			String[] ss1 = StringUtils.split(s1, Symbol.DOUHAO);
			if (ss1 == null) {
				continue;
			}
			for (String s2 : ss1) {
				result1.add(NumberUtils.toInt(s2));
			}
			result.add(result1);
		}
		return result;
	}

	/**
	 * 分号隔开逗号隔开转换成list矩阵
	 * 
	 * @param str
	 * @return
	 */
	public static List<List<Integer>> toListListIntMaoHao(String str) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		if (StringUtils.isBlank(str)) {
			return result;
		}
		String[] pairs = StringUtils.split(str, Symbol.MAOHAO);
		for (String s1 : pairs) {
			List<Integer> result1 = new ArrayList<Integer>();
			String[] ss1 = StringUtils.split(s1, Symbol.DOUHAO);
			if (ss1 == null) {
				continue;
			}
			for (String s2 : ss1) {
				result1.add(NumberUtils.toInt(s2));
			}
			result.add(result1);
		}
		return result;
	}

	/**
	 * 将List数据转化为string
	 */
	public static String listToString(List<?> list) {
		String result = "";
		for (Object value : list) {
			result += value + Symbol.DOUHAO;
		}
		if (!list.isEmpty()) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/**
	 * 将map数据转化为string，格式：X:X,X:X,X:X,......
	 */
	public static String mapToString2(Map<Integer, Integer> map) {
		String result = "";
		for (int propKey : map.keySet()) {
			result += propKey + Symbol.MAOHAO + map.get(propKey) + Symbol.DOUHAO;
		}
		if (!map.isEmpty()) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	/**
	 * 将map数据转化为string，格式：X:X,X:X,X:X,......
	 */
	public static String mapToString3(Map<Byte, Integer> map) {
		String result = "";
		for (byte propKey : map.keySet()) {
			result += propKey + Symbol.MAOHAO + map.get(propKey) + Symbol.DOUHAO;
		}
		if (!map.isEmpty()) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	/**
	 * 将map数据转化为string，格式：X:X,X:X,X:X,......
	 */
	public static String mapToString(Map<?, ?> map) {
		StringBuffer sb = new StringBuffer();
		if (null != map && !map.isEmpty()) {
			for (Entry<?, ?> ent : map.entrySet()) {
				sb.append(ent.getKey()).append(Symbol.MAOHAO).append(ent.getValue()).append(Symbol.DOUHAO);
			}
		}
		return sb.toString();
	}

	/**
	 * 返回对象值,如果为空则返回0
	 * 
	 * @param value
	 * @return
	 */
	public static long getLong(Object value) {
		if (value == null) {
			return 0;
		}
		return (long) value;
	}

	/**
	 * 返回对象值,如果为空则返回0
	 * 
	 * @param value
	 * @return
	 */
	public static long getLong(Object value, long defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return (long) value;
	}

	/**
	 * 返回对象值,如果为空则返回0
	 * 
	 * @param value
	 * @return
	 */
	public static int getInt(Object value) {
		if (value == null) {
			return 0;
		}
		return (int) value;
	}

	/**
	 * 返回对象值,如果为空则返回0
	 * 
	 * @param value
	 * @return
	 */
	public static int getInt(Object value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return (int) value;
	}

	/**
	 * 返回对象值,如果为空则返回0
	 * 
	 * @param value
	 * @return
	 */
	public static float getFloat(Object value) {
		if (value == null) {
			return 0;
		}
		return Float.parseFloat(value.toString());
	}

	public static boolean containsHTMLTag(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		String pattern = "<\\s*(\\S+)(\\s*[^>]*)?>[\\s\\S]*<\\s*\\/\\1\\s*>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		return m.find();
	}
	
}
