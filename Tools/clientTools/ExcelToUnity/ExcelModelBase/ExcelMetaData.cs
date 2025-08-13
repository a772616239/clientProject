using ExcelModelBase;
using System;
using System.Collections.Generic;
using System.Linq;

namespace ExcelModel
{
    [Serializable]
    public class ExcelMetaData
    {
        private const int CONTENT_START_ROW = 5;
        public List<string> m_fieldComment = new List<string>();
        //public List<string> m_reservedAttribute = new List<string>();
        public List<string> m_fieldName = new List<string>();
        public List<EnumSupportFieldType> m_fieldType = new List<EnumSupportFieldType>();
        public List<Dictionary<string, object>> m_parsedFieldContent = new List<Dictionary<string, object>>();
        public List<string> m_keyFieldName = new List<string>();

        private ExcelMetaData() { }

        public static ExcelMetaData Parse(List<List<string>> rawData)
        {
            if (rawData.Count < 5)
            {
                throw new Exception("配置表格式错误,预留行数不足(5)");
            }
            else
            {
                ExcelMetaData result = new ExcelMetaData();

                int maxColumn = rawData[0].Count;

                List<int> ignoreForClientColumnIndex = new List<int>();

                for (int i = 0; i < rawData[0].Count; i++)
                {
                    //获取解析选项字段
                    string optional = rawData[3][i];
                    if (optional.Contains('c'))
                    {
                        ////解析注释
                        result.m_fieldComment.Add(rawData[0][i]);
                        ////解析预留字段
                        //result.m_reservedAttribute.Add(rawData[1][i]);
                        //解析字段名称
                        string fieldName = rawData[2][i].ToLower();
                        result.m_fieldName.Add(fieldName);
                        if (optional.Contains('k'))
                        {
                            result.m_keyFieldName.Add(fieldName);
                        }
                    }
                    else
                    {
                        ignoreForClientColumnIndex.Add(i);
                    }
                }
                //解析字段类型
                for (int i = 0; i < maxColumn; i++)
                {
                    if (ignoreForClientColumnIndex.Contains(i))//忽略没有标记为c的行
                        continue;

                    var fieldType = rawData[4][i];

                    switch (fieldType.ToLower())
                    {
                        case "int":
                            result.m_fieldType.Add(EnumSupportFieldType.Integer); break;
                        case "string":
                            result.m_fieldType.Add(EnumSupportFieldType.String); break;
                        case "float":
                            result.m_fieldType.Add(EnumSupportFieldType.Float); break;
                        case "boolean":
                            result.m_fieldType.Add(EnumSupportFieldType.Boolean); break;
                        case "intlist":
                            result.m_fieldType.Add(EnumSupportFieldType.IntegerArray); break;
                        case "intlist2":
                            result.m_fieldType.Add(EnumSupportFieldType.IntegerArray_2D); break;
                        case "strlist":
                            result.m_fieldType.Add(EnumSupportFieldType.StringArray); break;
                        case "strlist2":
                            result.m_fieldType.Add(EnumSupportFieldType.StringArray_2D); break;
                        case "floatlist":
                            result.m_fieldType.Add(EnumSupportFieldType.FloatArray); break;
                        case "floatlist2":
                            result.m_fieldType.Add(EnumSupportFieldType.FloatArry_2D); break;
                        case "booleanlist":
                            result.m_fieldType.Add(EnumSupportFieldType.BooleanArray); break;
                        case "booleanlist2":
                            result.m_fieldType.Add(EnumSupportFieldType.BooleanArray_2D); break;
                        case "long":
                            result.m_fieldType.Add(EnumSupportFieldType.LongInterger); break;
                        case "longlist":
                            result.m_fieldType.Add(EnumSupportFieldType.LongIntegerArray); break;
                        case "longlist2":
                            result.m_fieldType.Add(EnumSupportFieldType.LongIntegerArray_2D); break;
                        case "fix64":
                            result.m_fieldType.Add(EnumSupportFieldType.Fix64); break;
                        default:
                            throw new Exception($"不支持的数据类型{fieldType}");
                    }
                }

                //从第五行开始,解析字段值
                for (int i = CONTENT_START_ROW; i < rawData.Count; i++)
                {
                    Dictionary<string, object> fieldContent = new Dictionary<string, object>();
                    result.m_parsedFieldContent.Add(fieldContent);
                    int index = 0;
                    for (int fieldIndex = 0; fieldIndex < maxColumn; fieldIndex++)
                    {
                        if (ignoreForClientColumnIndex.Contains(fieldIndex))//忽略没有标记为c的行
                            continue;

                        var rawString = rawData[i][fieldIndex];
                        var obj = result.ConvertFieldString(rawString, index);
                        var fieldName = result.m_fieldName[index];
                        fieldContent[fieldName] = obj;
                        index++;
                    }
                }

                return result;
            }
        }

        public void CheckDataValid(string excelName)
        {
            foreach (var keyFieldName in m_keyFieldName)
            {
                Dictionary<object, int> keyDic = new Dictionary<object, int>();
                for (var i = 0; i < m_parsedFieldContent.Count; i++)
                {
                    object key = m_parsedFieldContent[i][keyFieldName];
                    int curRow = i + CONTENT_START_ROW + 1;
                    if (keyDic.TryGetValue(key, out int row))
                    {
                        throw new Exception($"[{excelName}]作为key的字段{keyFieldName}中, 第{row}行和第{curRow}行包含重复的值:{key}");
                    }
                    keyDic[key] = curRow;
                }
            }
        }

        /// <summary> 每次调用CreateExcelObject方法后,这个属性会返回错误报告(如果有错误) </summary>
        public string CreateExcelObjectDescribe { get; private set; }

        public MultiKeyDictionary<ExcelObject> CreateExcelObject<ExcelObject>()
        {
            return null;
            //CreateExcelObjectDescribe = string.Empty;

            //List<ExcelObject> result = new List<ExcelObject>();
            //var excelObjType = typeof(ExcelObject);
            //var excelObjFields = excelObjType.GetFields();
            //var fieldNameDict = new Dictionary<string, FieldInfo>();

            //foreach (var fieldInfo in excelObjFields)
            //{
            //    fieldNameDict.Add(fieldInfo.Name.ToLower(), fieldInfo);
            //}

            //foreach (var fieldValues in m_fieldContent)
            //{
            //    var excelObj = Activator.CreateInstance<ExcelObject>();
            //    for (int i = 0; i < fieldValues.Count; i++)
            //    {
            //        var fieldName = m_fieldName[i].ToLower();
            //        if (fieldNameDict.ContainsKey(fieldName))
            //        {
            //            var field = fieldNameDict[fieldName];
            //            var jValue = fieldValues[i];
            //            var fieldValue = ConvertFieldString(jValue, i);
            //            field.SetValue(excelObj, fieldValue);
            //        }
            //        else
            //        {
            //            CreateExcelObjectDescribe += $"在创建[{excelObjType.FullName}]实例时,没有找到名为[{fieldName}]的字段";
            //            continue;
            //        }
            //    }
            //    result.Add(excelObj);
            //}
            //var dataList = new List<ExcelObject>();
            //foreach (var excelObj in result)
            //{
            //    dataList.Add(excelObj);
            //}
            //return new MultiKeyDictionary<ExcelObject>(m_keyFieldName, dataList, excelObjType);
        }

        public MultiKeyDictionary<object> CreateExcelObject(Type type, Func<object> instancer)
        {
            return null;
            //CreateExcelObjectDescribe = string.Empty;
            //List<object> result = new List<object>();

            //Type excelObjType = type;
            //var excelObjFields = excelObjType.GetFields();

            //var fieldNameDict = new Dictionary<string, FieldInfo>();

            //foreach (var fieldInfo in excelObjFields)
            //{
            //    fieldNameDict.Add(fieldInfo.Name.ToLower(), fieldInfo);
            //}

            //foreach (var fieldValues in m_fieldContent)
            //{
            //    var excelObj = instancer.Invoke();
            //    for (int i = 0; i < fieldValues.Count; i++)
            //    {
            //        var fieldName = m_fieldName[i].ToLower();
            //        if (fieldNameDict.ContainsKey(fieldName))
            //        {
            //            var field = fieldNameDict[fieldName];
            //            var jValue = fieldValues[i];
            //            var fieldValue = ConvertFieldString(jValue, i);
            //            field.SetValue(excelObj, fieldValue);
            //        }
            //        else
            //        {
            //            CreateExcelObjectDescribe += $"在创建[{excelObjType.FullName}]实例时,没有找到名为[{fieldName}]的字段";
            //            continue;
            //        }

            //    }
            //    result.Add(excelObj);
            //}

            //return new MultiKeyDictionary<object>(m_keyFieldName, result, excelObjType);
        }

        public object ConvertFieldString(string rawString, int fieldIndex)
        {
            rawString = rawString.Replace("\"", string.Empty);

            try
            {
                switch (m_fieldType[fieldIndex])
                {
                    case EnumSupportFieldType.Boolean:
                        return bool.Parse(rawString);
                    case EnumSupportFieldType.BooleanArray:
                        return _parseArray(rawString, bool.Parse);
                    case EnumSupportFieldType.BooleanArray_2D:
                        return _parseArray2D(rawString, bool.Parse);
                    case EnumSupportFieldType.Float:
                        return float.Parse(rawString);
                    case EnumSupportFieldType.FloatArray:
                        return _parseArray(rawString, float.Parse);
                    case EnumSupportFieldType.FloatArry_2D:
                        return _parseArray2D(rawString, float.Parse);
                    case EnumSupportFieldType.Integer:
                        return int.Parse(rawString);
                    case EnumSupportFieldType.IntegerArray:
                        return _parseArray(rawString, int.Parse);
                    case EnumSupportFieldType.IntegerArray_2D:
                        return _parseArray2D(rawString, int.Parse);
                    case EnumSupportFieldType.String:
                        return rawString;
                    case EnumSupportFieldType.StringArray:
                        return _parseArray(rawString, (str) => { return str; });
                    case EnumSupportFieldType.StringArray_2D:
                        return _parseArray2D(rawString, (str) => { return str; });
                    case EnumSupportFieldType.LongInterger:
                        return long.Parse(rawString);
                    case EnumSupportFieldType.LongIntegerArray:
                        return _parseArray(rawString, long.Parse);
                    case EnumSupportFieldType.LongIntegerArray_2D:
                        return _parseArray2D(rawString, long.Parse);
                    case EnumSupportFieldType.Fix64:
                        return (Fix64)float.Parse(rawString);
                    default:
                        throw new Exception("未知的类型转换");
                }
            }
            catch
            {
                return GetDefaultTypeValue(m_fieldType[fieldIndex]);
            }
        }

        private object GetDefaultTypeValue(EnumSupportFieldType type)
        {
            switch (type)
            {
                case EnumSupportFieldType.Boolean:
                    return default(bool);
                case EnumSupportFieldType.BooleanArray:
                    return new List<bool>();
                case EnumSupportFieldType.BooleanArray_2D:
                    return new List<List<bool>>();
                case EnumSupportFieldType.Float:
                    return default(float);
                case EnumSupportFieldType.FloatArray:
                    return new List<float>();
                case EnumSupportFieldType.FloatArry_2D:
                    return new List<List<float>>();
                case EnumSupportFieldType.Integer:
                    return default(int);
                case EnumSupportFieldType.IntegerArray:
                    return new List<int>();
                case EnumSupportFieldType.IntegerArray_2D:
                    return new List<List<int>>();
                case EnumSupportFieldType.String:
                    return string.Empty;
                case EnumSupportFieldType.StringArray:
                    return new List<string>();
                case EnumSupportFieldType.StringArray_2D:
                    return new List<List<string>>();
                case EnumSupportFieldType.LongInterger:
                    return default(long);
                case EnumSupportFieldType.LongIntegerArray:
                    return new List<long>();
                case EnumSupportFieldType.LongIntegerArray_2D:
                    return new List<List<long>>();
                case EnumSupportFieldType.Fix64:
                    return default(Fix64);
                default:
                    throw new Exception("未知的类型转换");
            }
        }

        private static object _parseArray<T>(string rawString, Func<string, T> parseFunc)
        {
            var result = new List<T>();
            foreach (var itemString in rawString.TrimStart('{').TrimEnd('}').Split(','))
            {
                result.Add(parseFunc.Invoke(itemString));
            }
            return result;
        }

        private static object _parseArray2D<T>(string rawString, Func<string, T> parseFunc)
        {
            List<List<T>> result1 = new List<List<T>>();
            foreach (var arrayStr in rawString.TrimStart('{').TrimEnd('}').Split('|'))
            {
                result1.Add(new List<T>());
                foreach (var itemString in arrayStr.TrimStart('{').TrimEnd('}').Split(','))
                {
                    result1[result1.Count - 1].Add(parseFunc.Invoke(itemString));
                }
            }
            return result1;
        }

    }

    public enum EnumSupportFieldType
    {
        Integer,
        IntegerArray,
        IntegerArray_2D,
        Float,
        FloatArray,
        FloatArry_2D,
        String,
        StringArray,
        StringArray_2D,
        Boolean,
        BooleanArray,
        BooleanArray_2D,
        LongInterger,
        LongIntegerArray,
        LongIntegerArray_2D,
        Fix64,
    }
}
