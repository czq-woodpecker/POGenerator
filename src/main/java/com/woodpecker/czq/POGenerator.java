package com.woodpecker.czq;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author: woodpecker
 * @Date: 2018/8/30 9:36
 */

/**
 * //TODO
 * 未解决问题：
 * 1.类型不全
 *
 * 3.toString()未生成
 * 4.外键问题
 * 5.基本类型与包装类型
 * 6.部分数据库类型到Java数据类型未转换
 */
public class POGenerator {
    private String url;
    private String username;
    private String password;
    private Connection connection;


    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/miaosha?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false";
        String username = "root";
        String password = "root";
        POGenerator poGenerator = new POGenerator(url, username, password);
        poGenerator.generatePO("F:\\IDEA\\POGenerator\\src\\main\\java\\com\\woodpecker\\czq");
    }

    public POGenerator() {
    }

    public POGenerator(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }


    public void generatePO(String path) {
        List tableList = getTableList();
        String tableName;
        String entityName;
        String comment = null;
        try {
            for (int i = 0; i < tableList.size(); i++) {
                tableName = (String) tableList.get(i);
                entityName = mapUnderscoreToBigCamelCase(tableName);
                Map<String, String> propertiesMap = getEntityProperties(tableName);

                File file = new File(path, entityName + ".java");
                file.createNewFile();
                PrintWriter printWriter = new PrintWriter(file);
                //package声明
                String packageDecl = getPackageDeclaration(path);
                printWriter.println(packageDecl);
                printWriter.println();


                //import
                Set<String> importSet = getImportDeclaration(propertiesMap.values());
                for (String str : importSet) {
                    printWriter.println(str);
                }
                if (importSet.size() > 0) {
                    printWriter.println();
                }

                //public class XXX{
                printWriter.println("public class " + entityName + "{");


                //成员变量声明

                for (String key : propertiesMap.keySet()) {
                    printWriter.print("\tprivate " + propertiesMap.get(key) + " " + key + ";");
                    //添加注释
                    comment = getComment(tableName, mapCamelCaseToUnderscore(key));
                    if (comment == null || "".equals(comment.trim())) {
                        printWriter.println();
                    } else {
                        printWriter.println("  //" + comment);
                    }
                }

                //getter and  setter
                for (String str : propertiesMap.keySet()) {
                    String getterAndSetter = generateGetterAndGetter(propertiesMap.get(str), str);
                    printWriter.println();
                    printWriter.println(getterAndSetter);
                }


                printWriter.println("}");
                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 生成getter and setter
     *
     * @param type
     * @param name
     * @return
     */
    public static String generateGetterAndGetter(String type, String name) {
        if (type == null || name == null || "".equals(type.trim()) || "".equals(name.trim())) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        String firstUpperName = name.substring(0, 1).toUpperCase() + name.substring(1);

        //getter
        stringBuffer.append("\tpublic " + type + " get" + firstUpperName + "(){\n")
                .append("\t\treturn " + name + ";\n")
                .append("\t}");

        stringBuffer.append("\n");
        //setter
        stringBuffer.append("\tpublic void set" + firstUpperName + "(" + type + " " + name + "){\n")
                .append("\t\tthis." + name + " = " + name + ";\n")
                .append("\t}");
        return stringBuffer.toString();

    }

    /**
     * 获取某张表中某个字段的注释
     *
     * @param tableName  数据库表名
     * @param columnName 数据库列名
     * @return
     */
    public String getComment(String tableName, String columnName) {
        String comment = null;
        try {
            ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, columnName);
            while (columns.next()) {
                comment = columns.getString("REMARKS");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comment;
    }

    /**
     * 生成import语句
     * //TODO 待完善
     *
     * @param values
     * @return
     */
    private Set getImportDeclaration(Collection<String> values) {
        Set<String> strSet = new HashSet<String>();
        for (String str : values) {
            if ("Date".equals(str)) {
                strSet.add("import java.util.Date;");
            }
        }
        return strSet;
    }

    /**
     * 生成package声明
     *
     * @param path 格式：F:\IDEA\POGenerator\src\main\java\com\woodpecker\czq
     * @return
     */
    private String getPackageDeclaration(String path) {
        int index = path.indexOf("java");
        String[] split = path.substring(index).split("\\\\");
        if (split.length <= 1) {
            return "";
        } else {
            String str = "package ";
            for (int i = 1; i < split.length; i++) {
                str += split[i] + ".";
            }
            return str.substring(0, str.length() - 1) + ";";
        }
    }


    /**
     * 将下划线命名法映射为大驼峰命名法
     *
     * @param underscoreName 格式：miaosha_user、MIAOSHA_USER
     * @return MiaoshaUser
     */
    public static String mapUnderscoreToBigCamelCase(String underscoreName) {
        String str = mapUnderscoreToCamelCase(underscoreName);
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 根据表名获取实现类属性（表名 --> 列名+列类型 --> 实体属性Map（key:属性名；value:属性类型））
     *
     * @param tableName
     * @return
     */
    public Map getEntityProperties(String tableName) {
        Map map = new HashMap<String, String>();
        String columnName;
        String propertyName;
        String dbType;
        String javaType;
        try {
            ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, null);
            while (columns.next()) {
                columnName = columns.getString("COLUMN_NAME");
                propertyName = mapUnderscoreToCamelCase(columnName);
                dbType = columns.getString("TYPE_NAME");
                javaType = dbTypeToJavaType(dbType);
                map.put(propertyName, javaType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 数据库类型转换为Java数据类型（）
     *
     * @param dbType
     * @return
     */
    public static String dbTypeToJavaType(String dbType) {
        if ("varchar".equals(dbType) || "tinytext".equals(dbType) || "text".equals(dbType)
                || "mediumtext".equals(dbType) || "longtext".equals(dbType)) {
            return "String";
        } else if ("bigint".equals(dbType)) {
            return "long";
        } else if ("datetime".equals(dbType)) {
            return "Date";
        } else if ("decimal".equals(dbType)) {
            return "double";
        } else if ("tinyint".equals(dbType)) {
            return "int";
        } else {
            return dbType;
        }
    }

    /**
     * 将下划线命名法映射为驼峰命名法
     *
     * @param columnName 格式：Goods_Name、good_name
     * @return 格式：goodName
     */
    public static String mapUnderscoreToCamelCase(String columnName) {
        if (columnName == null || "".equals(columnName.trim())) {
            return null;
        }
        String[] split = columnName.split("_");
        if (split.length < 0) {
            return null;
        } else if (split.length == 1) {
            return split[0];
        } else {
            StringBuffer stringBuffer = new StringBuffer(split[0].toLowerCase());
            for (int i = 1; i < split.length; i++) {
                stringBuffer.append(split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase());
            }
            return stringBuffer.toString();
        }
    }


    /**
     * 将驼峰命名法（此处包含大驼峰和小驼峰）映射为下划线命名法
     *
     * @param string 格式：goodsName、OrderInfo
     * @return 格式：goods_name、order_info
     */
    public static String mapCamelCaseToUnderscore(String string) {
        if (string == null || "".equals(string.trim())) {
            return null;
        } else if (string.length() == 1) {
            return string;
        } else {
            int start = 0;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 1; i < string.length(); i++) {
                if (Character.isUpperCase(string.charAt(i))) {
                    stringBuffer.append(string.substring(start, i).toLowerCase() + "_");
                    start = i;
                }

            }
            stringBuffer.append(string.substring(start).toLowerCase());
            return stringBuffer.toString();
        }


    }

    /**
     * 获取所有表名
     *
     * @return
     */
    public List getTableList() {
        List tables = new ArrayList<String>();
        connection = getConnection();
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tablesRS = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
            while (tablesRS.next()) {
                tables.add(tablesRS.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;

    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
