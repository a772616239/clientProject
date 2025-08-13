第一行 字段的说明（程序不处理）
第二行 预留字段 暂未处理
第三行 字段名字
第四行 是否服务器或客户端所需字段，是否作为查询的索引（暂时只支持int string 类型作为查询的索引），c是客户端需要读的，s是服务器需要读的，k表示该字段为查询索引
第五行 字段的类型（暂时只支持 int intList,intList2,string,stringList,stringList2,boolean,float）住：intList 为int[] intList2 int[][] stringList string[] stringList2 String[][]
第六行开始 为字段的值

注：string类型不使用引号，strList类型的逗号和花括号前后都不要用空格，list里的值用,作为分隔符，list2里的值用|作为分隔符(例：{{1,2}|{3,4}})