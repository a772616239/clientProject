using ProtoBuf;
using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace ExcelModelBase.Scripts
{
    [ProtoContract]
    public class TestConfigRawData
    {
        [ProtoMember(1)]
        public string Name { get; set; }
        [ProtoMember(2)]
        public int Id { get; set; }
        [ProtoMember(3)]
        public List<int>[] LineIntList { get; set; }
    }


    public class Test
    {
        private static string ProtoFile = "testconfig";

        public static void Run()
        {
            SerializeProto();
            DeserializeProto();
        }

        private static TestConfigRawData WrapData1(int index)
        {
            TestConfigRawData data = new TestConfigRawData();
            data.Name = "ctctyy";
            data.Id = index;
            data.LineIntList = new List<int>[1];
            var list = new List<int>();
            list.Add(100);
            list.Add(200);
            data.LineIntList[0] = list;
            return data;
        }

        private static void SerializeProto()
        {
            List<TestConfigRawData> list = new List<TestConfigRawData>();
            for (var i = 0; i < 10; i++)
            {
                var data = WrapData1(i);
                list.Add(data);
            }

            using (var file = System.IO.File.Create(ProtoFile))
            {
                Serializer.Serialize(file, list);
            }
        }

        private static void DeserializeProto()
        {
            var bytes = File.ReadAllBytes(ProtoFile);
            MemoryStream ms = new MemoryStream();
            ms.Write(bytes, 0, bytes.Length);
            ms.Position = 0;
            var data = Serializer.Deserialize<List<TestConfigRawData>>(ms);
            Console.WriteLine(data);
        }
    }
}
