# POGenerator介绍

## 一、简介

PO:Persistant Object,持久化对象，在公司实习了解到他们一般都会用PO来映射数据库表，其实就是成员变量与数据库字段一一对应的Java Bean。所以，我突然就现写个根据数据库逆向生成PO的工具类。造造小轮子，学习一下。

## 二、思路

### 1.输出

1. Java源文件：文件名、路径。
2. 文件内容：package部分、import部分、public class +类名+"{"、private+属性类型+属性名、getter and setter方法

### 2.输入：url、username、password

### 3.处理：

1. 文件名：根据connection拿到数据库元数据，再拿到所有表名，处理下格式即可。
2. 路径：根据设置的绝对路径，稍作处理即可。
3. package部分:同上。
4. import部分：判断当前Java Bean的所有属性的类型，若不是java.lang包下的，导入相应的类。
5. 类名：同文件名
6. 属性类型：根据数据库表名拿到对应表的数据，取出所有列的类型，注意将数据库数据类型映射为Java数据类型。
7. 属性名称：根据数据库表名拿到对应表的数据，取出所有列的名称，注意需要将数据库列名改为驼峰命名法。
8. getter and setter方法：上面拿到了JavaBean的属性类型和属性名称，剩下的只是字符串拼接和处理之类的而已。

## 三、使用方法

找个地方运行下面的方法(替换成自己的数据库连接信息)

```java
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/miaosha";
        String username = "root";
        String password = "root";
        POGenerator poGenerator = new POGenerator(url, username, password);       	
        poGenerator.generatePO("F:\\IDEA\\POGenerator\\src\\main\\java\\com\\woodpecker\\czq");
    }
```

## 四、待改进问题

1. 类型不全
2. 注释未生成
3. toString()未生成
4. 外键问题
5. 基本类型与包装类型
6. 部分数据库类型到Java数据类型未转换