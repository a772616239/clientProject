package bean;

import code.TypeConfig;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Administrator
 * @date
 */
@Getter
@Setter
public class ConfigData implements Cloneable{
	private String fieldName;
	private String fieldType;
	/**
	 * primary key
	 */
	private String fieldKey;
	private String fieldValue;
	private String fieldSkip;

	public boolean isSkip() {
		return fieldSkip != null && Objects.equals(fieldSkip, TypeConfig.FIELD_SKIP);
	}

	@Override
	public ConfigData clone() {
		ConfigData data = new ConfigData();
		data.setFieldName(getFieldName());
		data.setFieldType(getFieldType());
		data.setFieldKey(getFieldKey());
		data.setFieldValue(getFieldValue());
		data.setFieldSkip(getFieldSkip());
		return data;
	}

}
