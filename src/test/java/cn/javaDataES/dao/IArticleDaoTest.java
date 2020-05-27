package cn.javaDataES.dao;

import cn.javaDataES.domain.Article;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class IArticleDaoTest {

    @Autowired
    private IArticleDao iArticleDao;

    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void createTest() throws Exception{
        //创建索引并配置映射
        template.createIndex(Article.class);
        //配置映射
        //Template.putMapping(Article.class);
    }


}
