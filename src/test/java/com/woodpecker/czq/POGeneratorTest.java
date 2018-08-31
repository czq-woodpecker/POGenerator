package com.woodpecker.czq;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: woodpecker
 * @Date: 2018/8/31 10:34
 */
public class POGeneratorTest {
    POGenerator poGenerator;

    @Before
    public void setUp() throws Exception {
        String url = "jdbc:mysql://localhost:3306/miaosha?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false";
        String username = "root";
        String password = "root";
        poGenerator = new POGenerator(url, username, password);
        poGenerator.generatePO("F:\\IDEA\\POGenerator\\src\\main\\java\\com\\woodpecker\\czq");
    }

    @Test
    public void getComment() {
        String comment = poGenerator.getComment("goods", "goods_name");
        assertTrue("商品名称".equals(comment));
        assertTrue("商品单价".equals(poGenerator.getComment("goods_name", "goods")));

    }

    @Test
    public void mapUnderscoreToCamelCase() {
        assertTrue("goodsName".equals(poGenerator.mapUnderscoreToCamelCase("Goods_Name")));
        assertTrue("goodsName".equals(poGenerator.mapUnderscoreToCamelCase("goods_name")));
        assertTrue(null == poGenerator.mapUnderscoreToCamelCase(null));
    }

    @Test
    public void mapCamelCaseToUnderscore() {
        assertTrue("goods_name".equals(POGenerator.mapCamelCaseToUnderscore("goodsName")));
        assertTrue("order_info".equals(POGenerator.mapCamelCaseToUnderscore("OrderInfo")));
        assertTrue("s".equals(POGenerator.mapCamelCaseToUnderscore("s")));
        assertTrue(null == POGenerator.mapCamelCaseToUnderscore(null));
    }
}