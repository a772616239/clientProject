using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ExcelModel
{
    public static class ExcelObjectGenerator
    {
        static readonly string ClassTemplate = @"using System.Collections.Generic;
using ExcelModelBase;
using FightLogic_Model.Scripts.Core.Math.FixedPoint;

namespace [NAME_SPACE]
{
    public class [EXCEL_CLASS_NAME] : ConfigBase
    {
[FIELD_Template]
    }
}
";

        static readonly string HotfixClassTemplate = @"using System.Collections.Generic;
using FightLogic_Model.Scripts.Core.Math.FixedPoint;

namespace [NAME_SPACE]
{
    public class [EXCEL_CLASS_NAME] : HotfixConfigBase
    {
[FIELD_Template]
    }
}
";
        static readonly string FieldTemplate = @"        public [FIELD_TYPE] [FIELD_NAME];";
        static readonly string HotfixFieldTemplate =
@"        private bool isInit_[FIELD_NAME];
        private [FIELD_TYPE] _[FIELD_NAME];
        public [FIELD_TYPE] [FIELD_NAME]
        {
            get
            {
                if (!isInit_[FIELD_NAME]) { isInit_[FIELD_NAME] = true; _[FIELD_NAME] = [FIELD_FUNC]([FIELD_NAME_STR]); }
                return _[FIELD_NAME];
            }
        }
";
        static readonly string LogicConfigManagerTemplate = @"
using Excel;
using ExcelModelBase;

namespace FightLogic_Hotfix.Scripts.Core.Config
{
    public partial class LogicConfigManager
    {
        private static readonly object _lock = new object();
[FIELD_Template]
    }
}";
        static readonly string LogicConfigFieldTemplate = @"
        private bool _isInit_[FIELD_NAME]_[KEY_NAME];
        private DictionGetter<[FIELD_NAME]> _[FIELD_NAME]_[KEY_NAME];
        public DictionGetter<[FIELD_NAME]> [FIELD_NAME]_[KEY_NAME]
        {
            get
            {
                if (!_isInit_[FIELD_NAME]_[KEY_NAME])
                {
                    object @lock = _lock;
                    lock (@lock)
                    {
                        if (!_isInit_[FIELD_NAME]_[KEY_NAME])
                        {
                            _[FIELD_NAME]_[KEY_NAME] = GetExcelData<[FIELD_NAME]>()[[KEY_NAME_STR]];
                            _isInit_[FIELD_NAME]_[KEY_NAME] = true;
                        }
                    }
                }
                return _[FIELD_NAME]_[KEY_NAME];
            }
        }
";
        public static string GeneratCodeString(ExcelMetaData excelMeta, string excelName)
        {
            if (excelMeta.m_fieldName.Count == 0)
            {
                return null;
            }

            var codeStr = ClassTemplate
                .Replace("[NAME_SPACE]", "Excel")
                .Replace("[EXCEL_CLASS_NAME]", excelName);


            var fieldBuilder = new StringBuilder();
            for (int i = 0; i < excelMeta.m_fieldType.Count; i++)
            {
                var typeString = excelMeta.m_fieldType[i].GeneratFieldTypeString();
                var commentList = excelMeta.m_fieldComment[i].Split('\n');
                bool isKey = excelMeta.m_keyFieldName.Contains(excelMeta.m_fieldName[i]);
                if (commentList.Length > 1)
                {
                    fieldBuilder.AppendLine("        /// <summary>");
                    foreach (var commentStr in commentList)
                    {
                        fieldBuilder.AppendLine($"        ///{commentStr}");
                    }
                    if (isKey)
                        fieldBuilder.Append("   *");

                    fieldBuilder.AppendLine("        /// </summary>");
                }
                else
                    fieldBuilder.AppendLine($"        /// <summary> {excelMeta.m_fieldComment[i]} {(isKey ? "*" : string.Empty)} </summary>");
                fieldBuilder.AppendLine(
                    FieldTemplate
                    .Replace("[FIELD_TYPE]", typeString)
                    .Replace("[FIELD_NAME]", excelMeta.m_fieldName[i].ToLower()));
            }

            GenerateKeyFunction(fieldBuilder, excelMeta);
            codeStr = codeStr.Replace("[FIELD_Template]", fieldBuilder.ToString());
            return codeStr;
        }

        public static string GenerateHotfixCodeString(ExcelMetaData excelMeta, string excelName)
        {
            if (excelMeta.m_fieldName.Count == 0)
            {
                return null;
            }
            var codeStr = HotfixClassTemplate
                .Replace("[NAME_SPACE]", "HotfixExcel")
                .Replace("[EXCEL_CLASS_NAME]", "Hotfix_" + excelName);

            var fieldBuilder = new StringBuilder();
            for (int i = 0; i < excelMeta.m_fieldType.Count; i++)
            {
                string fieldName = excelMeta.m_fieldName[i];
                var typeString = excelMeta.m_fieldType[i].GeneratFieldTypeString();
                var funcString = excelMeta.m_fieldType[i].GeneratFieldFuncString();
                var commentList = excelMeta.m_fieldComment[i].Split('\n');
                bool isKey = excelMeta.m_keyFieldName.Contains(fieldName);
                if (commentList.Length > 1)
                {
                    fieldBuilder.AppendLine("        /// <summary>");
                    foreach (var commentStr in commentList)
                    {
                        fieldBuilder.AppendLine($"        ///{commentStr}");
                    }
                    if (isKey)
                        fieldBuilder.Append("   *");

                    fieldBuilder.AppendLine("        /// </summary>");
                }
                else
                    fieldBuilder.AppendLine($"        /// <summary> {excelMeta.m_fieldComment[i]} {(isKey ? "*" : string.Empty)} </summary>");
                fieldBuilder.AppendLine(
                    HotfixFieldTemplate
                    .Replace("[FIELD_TYPE]", typeString)
                    .Replace("[FIELD_NAME_STR]", $"\"{fieldName.ToLower()}\"")
                    .Replace("[FIELD_NAME]", fieldName.ToLower())
                    .Replace("[FIELD_FUNC]", funcString)
                    );
            }

            GenerateKeyFunction(fieldBuilder, excelMeta);
            codeStr = codeStr.Replace("[FIELD_Template]", fieldBuilder.ToString());
            return codeStr;
        }

        private static void GenerateKeyFunction(StringBuilder fieldBuilder, ExcelMetaData excelMeta)
        {
            fieldBuilder.AppendLine();
            fieldBuilder.AppendLine("\t\tpublic override object GetKey(string keyName)");
            fieldBuilder.AppendLine("\t\t{");
            fieldBuilder.AppendLine("\t\t\tswitch(keyName)");
            fieldBuilder.AppendLine("\t\t\t{");
            for (var i = 0; i < excelMeta.m_keyFieldName.Count; i++)
            {
                string keyName = excelMeta.m_keyFieldName[i];
                fieldBuilder.AppendLine($"\t\t\t\tcase \"{keyName}\":");
                fieldBuilder.AppendLine($"\t\t\t\t\treturn {keyName};");
            }
            fieldBuilder.AppendLine($"\t\t\t\tdefault:");
            fieldBuilder.AppendLine($"\t\t\t\t\treturn null;");
            fieldBuilder.AppendLine("\t\t\t}");
            fieldBuilder.Append("\t\t}");
        }

        public static string GenerateLogicConfigManagerString(Dictionary<string, ExcelMetaData> excelDic)
        {
            var codeStr = LogicConfigManagerTemplate;
            StringBuilder fieldBuilder = new StringBuilder();
            List<string> list = excelDic.Keys.OrderBy((value) => value).ToList();
            foreach (var excelName in list)
            {
                List<string> keyList = excelDic[excelName].m_keyFieldName;
                foreach (var key in keyList)
                {
                    string keyField = LogicConfigFieldTemplate
                        .Replace("[FIELD_NAME]", excelName)
                        .Replace("[KEY_NAME]", key)
                        .Replace("[KEY_NAME_STR]", $"\"{key}\"");
                    fieldBuilder.AppendLine(keyField);
                }
            }
            codeStr = codeStr.Replace("[FIELD_Template]", fieldBuilder.ToString());
            return codeStr;
        }

        public static string GeneratFieldTypeString(this EnumSupportFieldType fieldType)
        {
            switch (fieldType)
            {
                case EnumSupportFieldType.Boolean: return "bool";
                case EnumSupportFieldType.BooleanArray: return "List<bool>";
                case EnumSupportFieldType.BooleanArray_2D: return "List<List<bool>>";
                case EnumSupportFieldType.Float: return "float";
                case EnumSupportFieldType.FloatArray: return "List<float>";
                case EnumSupportFieldType.FloatArry_2D: return "List<List<float>>";
                case EnumSupportFieldType.Integer: return "int";
                case EnumSupportFieldType.IntegerArray: return "List<int>";
                case EnumSupportFieldType.IntegerArray_2D: return "List<List<int>>";
                case EnumSupportFieldType.String: return "string";
                case EnumSupportFieldType.StringArray: return "List<string>";
                case EnumSupportFieldType.StringArray_2D: return "List<List<string>>";
                case EnumSupportFieldType.LongIntegerArray: return "List<long>";
                case EnumSupportFieldType.LongInterger: return "long";
                case EnumSupportFieldType.LongIntegerArray_2D: return "List<List<long>>";
                case EnumSupportFieldType.Fix64: return "Fix64";
                default:
                    throw new Exception("未知类型转换");
            }
        }

        public static string GeneratFieldFuncString(this EnumSupportFieldType fieldType)
        {
            switch (fieldType)
            {
                case EnumSupportFieldType.Boolean: return "GetBool";
                case EnumSupportFieldType.BooleanArray: return "GetBoolList";
                case EnumSupportFieldType.BooleanArray_2D: return "GetBoolList2";
                case EnumSupportFieldType.Float: return "GetFloat";
                case EnumSupportFieldType.FloatArray: return "GetFloatList";
                case EnumSupportFieldType.FloatArry_2D: return "GetFloatList2";
                case EnumSupportFieldType.Integer: return "GetInt";
                case EnumSupportFieldType.IntegerArray: return "GetIntList";
                case EnumSupportFieldType.IntegerArray_2D: return "GetIntList2";
                case EnumSupportFieldType.String: return "GetString";
                case EnumSupportFieldType.StringArray: return "GetStringList";
                case EnumSupportFieldType.StringArray_2D: return "GetStringList2";
                case EnumSupportFieldType.LongIntegerArray: return "GetLongList";
                case EnumSupportFieldType.LongInterger: return "GetLong";
                case EnumSupportFieldType.LongIntegerArray_2D: return "GetLongList2";
                case EnumSupportFieldType.Fix64: return "GetFix64";
                default:
                    throw new Exception("未知类型转换");
            }
        }
    }
}
