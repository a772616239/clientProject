using ExcelDataReader;
using ExcelModel;
using ExcelModelBase.Scripts;
using Newtonsoft.Json;
using ProtoBuf;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace ExcelToUnity
{
    class Program
    {
        enum ExportType
        {
            AllCsAndJson = 1,
            AllJson,
            SingleJson,
        }
        static void Main(string[] args)
        {
            //Test.Run();
            //return;
            Console.WriteLine("开始...");
            try
            {
                int exportType = int.Parse(args[0]);
                ExportType exportEnum = (ExportType)exportType;
                switch (exportEnum)
                {
                    case ExportType.AllCsAndJson:
                        {
                            var excelPath = args[1] as string;
                            var outputPath = args[2] as string;
                            var codeFileOutputPath = args[3] as string;
                            var hotfixCodeOutputPath = args[4] as string;
                            var logicConfigManagerPath = args[5] as string;
                            var isOnlyExportHotfix = args[6] == "true";
                            Console.WriteLine($"导出cs和json");
                            Console.WriteLine($"Excel表目录:{excelPath}");
                            Console.WriteLine($"Json输出目录:{outputPath}");
                            Console.WriteLine($"Code输出目录:{codeFileOutputPath}");
                            Console.WriteLine($"HotfixCode输出目录:{hotfixCodeOutputPath}");
                            Console.WriteLine($"是否只导出热更层:{isOnlyExportHotfix}");
                            ExportExcelFolder(excelPath, outputPath, codeFileOutputPath, hotfixCodeOutputPath, logicConfigManagerPath, isOnlyExportHotfix, true);
                        }
                        break;
                    case ExportType.AllJson:
                        {
                            var excelPath = args[1] as string;
                            var outputPath = args[2] as string;
                            Console.WriteLine($"只导出json");
                            Console.WriteLine($"Excel表目录:{excelPath}");
                            Console.WriteLine($"Json输出目录:{outputPath}");
                            ExportExcelFolder(excelPath, outputPath, null, null, null, false, false);
                        }
                        break;
                    case ExportType.SingleJson:
                        {
                            var outputPath = args[1] as string;
                            var excelName = args[2] as string;
                            List<string> list = new List<string>();
                            list.Add(excelName);
                            ExportAllConfigs(list, outputPath, null, null, null, false, false);
                        }
                        break;
                    default:
                        throw new Exception("不支持的导出类型：" + args[0]);
                }
            }
            catch (Exception ex)
            {
                Console.Write(ex.ToString());
                Console.ReadLine();
            }
        }

        static void ExportExcelFolder(string excelPath, string outputPath, string codeFileOutputPath, string hotfixCodeOutputPath, string logicConfigManagerPath, bool isOnlyExportHotfix, bool isExportCs)
        {
            if (Directory.Exists(excelPath))
            {
                var excelFiles = Directory.GetFiles(excelPath).Where(f => IsExcelFile(f)).ToList();
                ExportAllConfigs(excelFiles, outputPath, codeFileOutputPath, hotfixCodeOutputPath, logicConfigManagerPath, isOnlyExportHotfix, isExportCs);
            }
            else
            {
                throw new Exception("excel表目录不存在!");
            }
        }

        static void ExportAllConfigs(List<string> excelFiles, string outputPath, string codeFileOutputPath, string hotfixCodeOutputPath, string logicConfigManagerPath, bool isOnlyExportHotfix, bool isExportCs)
        {
            SynchronizationContext.SetSynchronizationContext(new SynchronizationContext());
            var mainThreadContext = SynchronizationContext.Current;
            StringBuilder errorSb = new StringBuilder();

            if (excelFiles != null && excelFiles.Count > 0)
            {
                int taskCount = excelFiles.Count;
                Dictionary<string, ExcelMetaData> dic = new Dictionary<string, ExcelMetaData>();
                foreach (var excelFile in excelFiles)
                {
                    if (!IsExcelFile(excelFile))
                    {
                        taskCount--;
                        continue;
                    }
                    //Task.Run(() =>
                    //{
                    using (FileStream stream = new FileStream(excelFile, FileMode.Open, FileAccess.Read, FileShare.ReadWrite))
                    {
                        using (var reader = ExcelReaderFactory.CreateReader(stream))
                        {
                            var excelName = Path.GetFileNameWithoutExtension(stream.Name);
                            try
                            {
                                do
                                {
                                    while (reader.Read()) { }
                                } while (reader.NextResult());

                                var result = reader.AsDataSet().Tables[0];
                                List<List<string>> rawData = new List<List<string>>();

                                for (int row = 0; row < result.Rows.Count; row++)
                                {
                                    if (result.Rows[row].ItemArray.Length > 1000)
                                    {
                                        throw new Exception($"{excelFile}列数过多：{result.Rows[row].ItemArray.Length}");
                                    }
                                    rawData.Add(new List<string>
                                        (
                                        result.Rows[row].ItemArray
                                        .Select(i => i.ToString())
                                        )
                                        );
                                }

                                ExcelExportData exportData = ExcelExportDataParser.Parse(excelName, rawData);
                                if (exportData.FieldCommentList.Count == 0)
                                {
                                    Console.WriteLine($"跳过配置表{excelFile}的生成,不包含客户端所需字段");
                                    //mainThreadContext.Post(new SendOrPostCallback((obj) =>
                                    //{
                                    taskCount--;
                                    //}), null);
                                    //return;
                                    continue;
                                }


                                //exportData.CheckDataValid(excelName);

                                if (!Directory.Exists(outputPath))
                                {
                                    Directory.CreateDirectory(outputPath);
                                }

                                using (var file = System.IO.File.Create($"{outputPath}/{excelName}.json"))
                                {
                                    Serializer.Serialize(file, exportData.ExcelRawData);
                                }

                                //if (isExportCs)
                                //{
                                //    if (!isOnlyExportHotfix)
                                //    {
                                //        if (!Directory.Exists(codeFileOutputPath))
                                //        {
                                //            Directory.CreateDirectory(codeFileOutputPath);
                                //        }
                                //        var codeStr = ExcelObjectGenerator.GeneratCodeString(exportData, excelName);
                                //        File.WriteAllBytes($"{codeFileOutputPath}/{excelName}.cs", Encoding.UTF8.GetBytes(codeStr));
                                //    }

                                //    var hotfixCodeStr = ExcelObjectGenerator.GenerateHotfixCodeString(exportData, excelName);
                                //    File.WriteAllBytes($"{hotfixCodeOutputPath}/Hotfix_{excelName}.cs", Encoding.UTF8.GetBytes(hotfixCodeStr));
                                //}

                                Console.WriteLine($"{excelFile}已生成");

                                //mainThreadContext.Post(new SendOrPostCallback((obj) =>
                                //{
                                //dic[excelName] = exportData;
                                taskCount--;
                                //}), null);
                            }
                            catch (Exception ex)
                            {
                                //mainThreadContext.Post(new SendOrPostCallback((obj) =>
                                //{
                                taskCount--;
                                errorSb.AppendLine();
                                errorSb.AppendLine($"{excelName}报错:{ex.Message}");
                                //}), null);
                            }
                        }
                    }
                    //});
                }

                while (taskCount > 0)
                {
                    Thread.Sleep(100);
                }
                if (errorSb.Length == 0)
                {
                    //if (!isOnlyExportHotfix && isExportCs)
                    //{
                    //    var logicConfigManagerStr = ExcelObjectGenerator.GenerateLogicConfigManagerString(dic);
                    //    File.WriteAllBytes(logicConfigManagerPath, Encoding.UTF8.GetBytes(logicConfigManagerStr));
                    //}

                    Console.WriteLine("OK");
                    Thread.Sleep(1000);
                }
                else
                {
                    string error = errorSb.ToString();
                    string errorMsg =
                        "\n\n<======================配置导出失败======================>\n"
                        + error
                        + "\n<======================配置导出失败======================>";
                    Console.WriteLine(errorMsg);
                    File.WriteAllText("Error.txt", error);
                    Console.ReadLine();
                }
            }
            else
            {
                throw new Exception("请输入正确的excel路径");
            }
        }

        private static bool IsExcelFile(string filePath)
        {
            if (filePath.Contains("~$"))
                return false;
            string extension = Path.GetExtension(filePath);
            return extension == ".xlsx" || extension == ".xlsm";
        }
    }
}


