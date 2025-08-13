using ProtoBuf;
using System.Collections.Generic;

namespace ExcelModelBase.Scripts
{
    [ProtoContract]
    public class ExcelRawData
    {
        [ProtoMember(1)]
        public ExcelHeaderRawData HeaderRawData { get; set; }
        [ProtoMember(2)]
        public ConfigRawData[] ConfigRawDatas { get; set; }
    }

    [ProtoContract]
    public class ExcelHeaderRawData
    {
        [ProtoMember(1)]
        public Dictionary<string, int> FieldIndexDic { get; set; }
        [ProtoMember(2)]
        public Dictionary<string, Dictionary<int, int>> KeyIndexDic { get; set; }
    }

    [ProtoContract]
    public class ConfigRawData
    {
        [ProtoMember(1)]
        public int[] LineInt { get; set; }
        [ProtoMember(2)]
        public string[] LineString { get; set; }
        [ProtoMember(3)]
        public float[] LineFloat { get; set; }
        [ProtoMember(4)]
        public bool[] LineBool { get; set; }
        [ProtoMember(5)]
        public long[] LineLong { get; set; }
        [ProtoMember(6)]
        public ConfigIntList[] LineIntList { get; set; }
        [ProtoMember(7)]
        public ConfigIntList2[] LineIntList2 { get; set; }
    }

    [ProtoContract]
    public class ConfigIntList
    {
        [ProtoMember(1)]
        public List<int> List;
    }

    [ProtoContract]
    public class ConfigIntList2
    {
        [ProtoMember(1)]
        public List<ConfigIntList> List;
    }
}
