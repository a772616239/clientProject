using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;

namespace ExcelModelBase
{
    public class MultiKeyDictionary<ExcelObj> : IEnumerable<ExcelObj>
    {
        private Dictionary<string, Dictionary<object, ExcelObj>> dictionaryList = new Dictionary<string, Dictionary<object, ExcelObj>>();
        private List<ExcelObj> m_dataList;

        public MultiKeyDictionary(Dictionary<string, Dictionary<object, ExcelObj>> dictionaryList, List<ExcelObj> dataList)
        {
            this.dictionaryList = dictionaryList;
            this.m_dataList = dataList;
        }

        public MultiKeyDictionary(List<ExcelObj> configList, List<string> keyList)
        {
            m_dataList = configList;
            foreach (var key in keyList)
            {
                Dictionary<object, ExcelObj> diction = new Dictionary<object, ExcelObj>();
                foreach (var config in configList)
                {
                    if(config is ConfigBase configBase)
                    {
                        diction.Add(configBase.GetKey(key), config);
                    }
                }
                dictionaryList.Add(key, diction);
            }
        }

        public MultiKeyDictionary(List<string> keyFieldNameList, List<ExcelObj> dataList, Type objType)
        {
            m_dataList = dataList;

            foreach (var key in keyFieldNameList)
            {
                Dictionary<object, ExcelObj> diction = new Dictionary<object, ExcelObj>();
                foreach (var obj in dataList)
                {
                    var key2 = objType.GetField(key.ToLower()).GetValue(obj);
                    diction.Add(key2, (ExcelObj)obj);
                }

                dictionaryList.Add(key.ToLower(), diction);
            }
        }

        public DictionGetter<ExcelObj> this[string key]
        {
            get
            {
                if (dictionaryList.TryGetValue(key.ToLower(), out Dictionary<object, ExcelObj> value))
                {
                    return new DictionGetter<ExcelObj>(value);
                }

                return default;
            }
        }

        public List<ExcelObj> ToList()
        {
            return m_dataList.ToList();
        }

        public IEnumerator GetEnumerator()
        {
            return m_dataList.GetEnumerator();
        }

        IEnumerator<ExcelObj> IEnumerable<ExcelObj>.GetEnumerator()
        {
            return m_dataList.GetEnumerator();
        }
    }

    public struct DictionGetter<Excel>
    {
        private Dictionary<object, Excel> m_data;

        public DictionGetter(Dictionary<object, Excel> data)
        {
            m_data = data;
        }

        public Excel this[object key]
        {
            get
            {
                if (m_data == null) return default;

                if (m_data.TryGetValue(key, out Excel value))
                {
                    return value;
                }

                return default;
            }
        }
    }
}
