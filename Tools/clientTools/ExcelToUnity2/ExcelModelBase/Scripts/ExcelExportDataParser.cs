using System;
using System.Collections.Generic;
using System.Text;

namespace ExcelModelBase.Scripts
{
    public class ExcelExportData
    {
        public ExcelRawData ExcelRawData = new ExcelRawData();
        public List<string> FieldCommentList = new List<string>();
        public List<string> KeyNameList = new List<string>();
    }

    public static class FieldTypeStr
    {
        public const string Integer = "int";
        public const string String = "string";
        public const string Float = "float";
        public const string Boolean = "boolean";
        public const string LongInterger = "long";
        public const string BooleanArray = "booleanlist";
        public const string BooleanArray_2D = "booleanlist2";
        public const string FloatArray = "floatlist";
        public const string FloatArry_2D = "floatlist2";
        public const string IntegerArray = "intlist";
        public const string IntegerArray_2D = "intlist2";
        public const string StringArray = "strlist";
        public const string StringArray_2D = "strlist2";
        public const string LongIntegerArray = "longlist";
        public const string LongIntegerArray_2D = "longlist2";
        public const string Fix64 = "fix64";

    }

    public static class ExcelExportDataParser
    {
        private const int CONTENT_START_ROW = 5;
        public static ExcelExportData Parse(string excelName, List<List<string>> rawData)
        {
            if (rawData.Count < CONTENT_START_ROW)
            {
                throw new Exception($"配置表{excelName}格式错误,预留行数不足(5)");
            }
            else
            {
                ExcelExportData result = new ExcelExportData();
                var excelRawData = result.ExcelRawData;
                ExcelHeaderRawData headerData = new ExcelHeaderRawData();
                headerData.FieldIndexDic = new Dictionary<string, int>();
                headerData.KeyIndexDic = new Dictionary<string, Dictionary<int, int>>();
                excelRawData.HeaderRawData = headerData;

                int maxColumn = rawData[0].Count;
                List<int> ignoreForClientColumnIndex = new List<int>();
                Dictionary<string, int> fieldTypeCountDic = new Dictionary<string, int>();
                fieldTypeCountDic.Add(FieldTypeStr.Integer, 0);
                fieldTypeCountDic.Add(FieldTypeStr.String, 0);
                fieldTypeCountDic.Add(FieldTypeStr.Float, 0);
                fieldTypeCountDic.Add(FieldTypeStr.Boolean, 0);
                fieldTypeCountDic.Add(FieldTypeStr.LongInterger, 0);
                fieldTypeCountDic.Add(FieldTypeStr.IntegerArray, 0);
                fieldTypeCountDic.Add(FieldTypeStr.IntegerArray_2D, 0);


                //解析Header
                for (int i = 0; i < maxColumn; i++)
                {
                    string optional = rawData[3][i];
                    if (optional.Contains("c"))
                    {
                        //解析注释
                        result.FieldCommentList.Add(rawData[0][i]);
                        //解析字段名
                        string fieldName = rawData[2][i].ToLower();
                        //解析key
                        if (optional.Contains("k"))
                        {
                            result.KeyNameList.Add(fieldName);
                            headerData.KeyIndexDic.Add(fieldName, new Dictionary<int, int>());
                        }
                        //解析字段类型
                        string fieldType = rawData[4][i].ToLower();
                        switch (fieldType)
                        {
                            case FieldTypeStr.Integer:
                            case FieldTypeStr.Float:
                            case FieldTypeStr.Boolean:
                            case FieldTypeStr.LongInterger:
                            case FieldTypeStr.IntegerArray:
                            case FieldTypeStr.IntegerArray_2D:
                                headerData.FieldIndexDic.Add(fieldName, fieldTypeCountDic[fieldType]);
                                fieldTypeCountDic[fieldType]++;
                                break;
                            default:
                                headerData.FieldIndexDic.Add(fieldName, fieldTypeCountDic[FieldTypeStr.String]);
                                fieldTypeCountDic[FieldTypeStr.String]++;
                                break;
                        }
                    }
                    else
                    {
                        ignoreForClientColumnIndex.Add(i);
                    }
                }

                if (result.FieldCommentList.Count == 0)
                    return result;

                //解析Content
                ConfigRawData[] configRawDatas = new ConfigRawData[rawData.Count - CONTENT_START_ROW];
                for (int row = CONTENT_START_ROW; row < rawData.Count; row++)
                {
                    ConfigRawData configRawData = new ConfigRawData();
                    configRawDatas[row - CONTENT_START_ROW] = configRawData;

                    configRawData.LineInt = new int[fieldTypeCountDic[FieldTypeStr.Integer]];
                    configRawData.LineFloat = new float[fieldTypeCountDic[FieldTypeStr.Float]];
                    configRawData.LineBool = new bool[fieldTypeCountDic[FieldTypeStr.Boolean]];
                    configRawData.LineLong = new long[fieldTypeCountDic[FieldTypeStr.LongInterger]];
                    configRawData.LineString = new string[fieldTypeCountDic[FieldTypeStr.String]];
                    configRawData.LineIntList = new ConfigIntList[fieldTypeCountDic[FieldTypeStr.IntegerArray]];
                    configRawData.LineIntList2 = new ConfigIntList2[fieldTypeCountDic[FieldTypeStr.IntegerArray_2D]];

                    for (int column = 0; column < maxColumn; column++)
                    {
                        if (ignoreForClientColumnIndex.Contains(column))
                            continue;
                        var rawString = rawData[row][column].Replace("\"", string.Empty);
                        string fieldName = rawData[2][column].ToLower();
                        var fieldType = rawData[4][column].ToLower();
                        int index = headerData.FieldIndexDic[fieldName];
                        var obj = ConvertFieldString(excelName, fieldName, fieldType, rawString, row);
                        switch (fieldType)
                        {
                            case FieldTypeStr.Integer:
                                configRawData.LineInt[index] = (int)obj;
                                break;
                            case FieldTypeStr.Float:
                                configRawData.LineFloat[index] = (float)obj;
                                break;
                            case FieldTypeStr.LongInterger:
                                configRawData.LineLong[index] = (long)obj;
                                break;
                            case FieldTypeStr.Boolean:
                                configRawData.LineBool[index] = (bool)obj;
                                break;
                            case FieldTypeStr.IntegerArray:
                                configRawData.LineIntList[index] = (ConfigIntList)obj;
                                break;
                            case FieldTypeStr.IntegerArray_2D:
                                configRawData.LineIntList2[index] = (ConfigIntList2)obj;
                                break;
                            default:
                                configRawData.LineString[index] = obj.ToString();
                                break;
                        }
                    }
                }

                excelRawData.ConfigRawDatas = configRawDatas;
                return result;
            }
        }

        private static object ConvertFieldString(string excelName, string fieldName, string fieldType, string rawString, int row)
        {
            if (string.IsNullOrEmpty(rawString))
                return GetDefaultTypeValue(fieldType);
            try
            {
                switch (fieldType)
                {
                    case FieldTypeStr.Integer:
                        return int.Parse(rawString);
                    case FieldTypeStr.LongInterger:
                        return long.Parse(rawString);
                    case FieldTypeStr.Float:
                        return float.Parse(rawString);
                    case FieldTypeStr.Boolean:
                        return bool.Parse(rawString);
                    case FieldTypeStr.String:
                        return rawString;
                    case FieldTypeStr.IntegerArray:
                        return ParseIntList(rawString);
                    case FieldTypeStr.IntegerArray_2D:
                        return ParseIntList2(rawString);
                    case FieldTypeStr.BooleanArray:
                    case FieldTypeStr.FloatArray:
                    case FieldTypeStr.StringArray:
                    case FieldTypeStr.LongIntegerArray:
                    case FieldTypeStr.Fix64:
                        return rawString;
                    case FieldTypeStr.FloatArry_2D:
                    case FieldTypeStr.LongIntegerArray_2D:
                    case FieldTypeStr.BooleanArray_2D:
                    case FieldTypeStr.StringArray_2D:
                        return rawString.TrimStart('{').TrimEnd('}');
                    default:
                        throw new Exception($"配置{excelName}字段{fieldName}，类型配置错误：{fieldType}");
                }
            }
            catch (Exception ex)
            {
                throw new Exception($"配置表{excelName}字段{fieldName}第{row + 1}行配置错误：{ex.Message}");
            }
        }

        private static object GetDefaultTypeValue(string fieldType)
        {
            switch (fieldType)
            {
                case FieldTypeStr.Boolean:
                    return default(bool);
                case FieldTypeStr.BooleanArray:
                    return new List<bool>();
                case FieldTypeStr.BooleanArray_2D:
                    return new List<List<bool>>();
                case FieldTypeStr.Float:
                    return default(float);
                case FieldTypeStr.FloatArray:
                    return new List<float>();
                case FieldTypeStr.FloatArry_2D:
                    return new List<List<float>>();
                case FieldTypeStr.Integer:
                    return default(int);
                case FieldTypeStr.IntegerArray:
                    return new ConfigIntList();
                case FieldTypeStr.IntegerArray_2D:
                    return new ConfigIntList2();
                case FieldTypeStr.String:
                    return string.Empty;
                case FieldTypeStr.StringArray:
                    return new List<string>();
                case FieldTypeStr.StringArray_2D:
                    return new List<List<string>>();
                case FieldTypeStr.LongInterger:
                    return default(long);
                case FieldTypeStr.LongIntegerArray:
                    return new List<long>();
                case FieldTypeStr.LongIntegerArray_2D:
                    return new List<List<long>>();
                case FieldTypeStr.Fix64:
                    return default(Fix64);
                default:
                    throw new Exception("未知的类型转换");
            }
        }

        private static ConfigIntList ParseIntList(string rawString)
        {
            var result = new ConfigIntList();
            rawString = rawString.TrimStart('{').TrimEnd('}');
            if (string.IsNullOrEmpty(rawString))
                return result;
            result.List = new List<int>();
            foreach (var itemString in rawString.Split(','))
            {
                if (!string.IsNullOrEmpty(itemString))
                    result.List.Add(int.Parse(itemString));
            }
            return result;
        }

        private static ConfigIntList2 ParseIntList2(string rawString)
        {
            ConfigIntList2 result = new ConfigIntList2();
            rawString = rawString.TrimStart('{').TrimEnd('}');
            if (string.IsNullOrEmpty(rawString))
                return result;
            result.List = new List<ConfigIntList>();
            foreach (var arrayStr in rawString.Split('|'))
            {
                ConfigIntList intList = ParseIntList(arrayStr);
                if (intList.List != null && intList.List.Count > 0)
                    result.List.Add(intList);
            }
            return result;
        }
    }
}
