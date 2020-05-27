package cn.javaES.test;

import cn.javaES.domain.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

public class TestJavaES {

    TransportClient client;
    @Before
    public void init() throws Exception{
        // 创建Client连接对象
        Settings settings = Settings.builder().put("cluster.name", "my-elasticsearch").build();
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9303));
        //创建名称为javaestest的索引
    }
    @After
    public void destory() throws Exception{
        //释放资源
        client.close();
    }
    @Test
    //创建索引
    public void test1() throws Exception
    {
        client.admin().indices().prepareCreate("javaestest").get();
    }

    @Test
    //创建映射
    /**
     * 格式：
     * "mappings" : {
            "Article" : {
                "dynamic" : "false",
                "properties" : {
                     "id" : { "type" : "string" },
                     "content" : { "type" : "string" },
                     "author" : { "type" : "string" }
                }
            }
         }
     */
    public void test2() throws Exception{
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("Article")
                        .startObject("properties")//属性
                            .startObject("id")
                                .field("type","integer").field("store","yes")
                            .endObject()
                            .startObject("title")
                                .field("type","string").field("store", "yes").field("analyzer", "ik_smart")
                            .endObject()
                            .startObject("content")
                                .field("type","string").field("store","yes").field("analyzer","ik_smart")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();
        client.admin().indices()
                //设置做映射的索引
                .preparePutMapping("javaestest")
                //设置映射的type
                .setType("Article")
                //mapping信息可以是XContentBuilder对象也可以是Json串
                .setSource(builder)
                //执行操作
                .get();
    }

    @Test
    //创建文档1
    public void test3_1() throws Exception{
        //创建文档信息
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", 1)
                    .field("title", "ElasticSearch是一个基于Lucene的搜索服务器")
                    .field("content",
                        "它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful web接口。Elasticsearch是用Java开发的，并作为Apache许可条款下的开放源码发布，是当前流行的企业级搜索引擎。设计用于云计算中，能够达到实时搜索，稳定，可靠，快速，安装使用方便。")
                .endObject();

        client.prepareIndex()
                .setIndex("javaestest")
                .setType("Article")
                .setId("1")
                .setSource(builder)
                .get();
    }
    @Test
    //创建文档2
    public void test3_2() throws Exception{
        //创建文档信息
        Article article = new Article();
        article.setId(2l);
        article.setTitle("搜索工作其实很快乐");
        article.setContent("我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDocument = objectMapper.writeValueAsString(article);
        System.out.println(jsonDocument);
        client.prepareIndex("javaestest","article","2")
                .setSource(jsonDocument, XContentType.JSON)
                .get();

/*        for(int i=3;i<=30;i++)
        {
            Article article = new Article();
            article.setId(i);
            article.setTitle(i+"搜索工作其实很快乐");
            article.setContent(i+"我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(article);
            System.out.println(jsonDocument);
            client.prepareIndex("javaestest","article",i+" ")
                    .setSource(jsonDocument, XContentType.JSON)
                    .get();
        }*/
    }

    public void serach(QueryBuilder queryBuilder)throws Exception{

        //2、设置搜索条件
        SearchResponse searchResponse = client.prepareSearch("javaestest")
                .setTypes("article")
                .setQuery(queryBuilder)
                //setFrom()：从第几条开始检索，默认是0。
                //setSize():每页最多显示的记录数。
                .setFrom(0)
                .setSize(5)
                .get();


        //3、遍历搜索结果数据
        SearchHits hits = searchResponse.getHits(); // 获取命中次数，查询结果有多少对象
        System.out.println("查询结果有：" + hits.getTotalHits() + "条");
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next(); // 每个查询对象
            System.out.println(searchHit.getSourceAsString()); // 获取字符串格式打印
            System.out.println("title:" + searchHit.getSource().get("title"));
        }
    }

    public void serach(QueryBuilder queryBuilder,String hiBuilderfield )throws Exception{
        HighlightBuilder hiBuilder=new HighlightBuilder();
        //设置高亮参数
        hiBuilder.preTags("<font style='color:red'>");
        hiBuilder.postTags("</font>");
        hiBuilder.field(hiBuilderfield);
        //2、设置搜索条件
        SearchResponse searchResponse = client.prepareSearch("javaestest")
                .setTypes("article")
                .setQuery(queryBuilder)
                //setFrom()：从第几条开始检索，默认是0。
                //setSize():每页最多显示的记录数。
                .setFrom(0)
                .setSize(5)
                //高亮
                .highlighter(hiBuilder)
                .get();
        //3、遍历搜索结果数据
        SearchHits hits = searchResponse.getHits(); // 获取命中次数，查询结果有多少对象
        System.out.println("查询结果有：" + hits.getTotalHits() + "条");
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next(); // 每个查询对象
            System.out.println("---------------------------------------------");
            System.out.println(searchHit.getSourceAsString()); // 获取字符串格式打印
            System.out.println("title:" + searchHit.getSource().get("title"));
            //高亮结果
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            //map方式打印高亮内容
            //{title=[title],(key=value) fragments[[17搜索工作其实很<font style='color:red'>快乐</font>]](片段)}
            System.out.println(highlightFields);
            HighlightField field = highlightFields.get(hiBuilderfield);
            Text[] fragments = field.getFragments();
            if(fragments !=null){
                String str = fragments[0].toString();
                System.out.println(str);
            }
           /* for (Text str : fragments) {
                System.out.println(str);
            }*/
        }
    }
    @Test
    //关键词查询
    public void test4_1() throws Exception{

        //2、设置搜索条件
        SearchResponse searchResponse = client.prepareSearch("javaestest")
                .setTypes("article")
                //参数  搜索字段  搜索条件
                .setQuery(QueryBuilders.termQuery("content", "希望")).get();

        //3、遍历搜索结果数据
        SearchHits hits = searchResponse.getHits(); // 获取命中次数，查询结果有多少对象
        System.out.println("查询结果有：" + hits.getTotalHits() + "条");
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next(); // 每个查询对象
            System.out.println(searchHit.getSourceAsString()); // 获取字符串格式打印
            System.out.println("title:" + searchHit.getSource().get("title"));
        }
//        //2、设置搜索条件
//        QueryBuilder queryBuilder = QueryBuilders.termQuery("content", "希望");
//        //执行搜索
//        serach(queryBuilder);


    }

    @Test
    //字符串查询
    public void test4_2() throws Exception{
        //2、设置搜索条件 ，先分词在查询
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1","2");
        //执行搜索
        serach(queryBuilder);
    }
    @Test
    //字符串查询
    public void test4_3() throws Exception{
        //2、设置搜索条件 ，先分词在查询
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("希望")
                //默认搜索域，不指定就在所有域查询
                .defaultField("content");
        //执行搜索
        serach(queryBuilder);
    }

    @Test
    //分页查询
    public void test4_4() throws Exception{
        // 搜索数据条件
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        //执行搜索
        serach(queryBuilder);
    }

    @Test
    //高亮查询
    public void test4_5() throws Exception{
        //2、设置搜索条件 ，先分词在查询
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("快乐")
                //默认搜索域，不指定就在所有域查询
                .defaultField("title");
        //执行搜索
        serach(queryBuilder,"title");
    }




}
