package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.common.Configuration;
import codes.showme.mavenrepocrawler.common.PropertiesConfig;
import codes.showme.mavenrepocrawler.domain.Link;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import javax.management.JMException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/2/17.
 */
public class Crawler implements PageProcessor {

    private static JedisPool jedisPool;
    private static final Configuration config = new PropertiesConfig();

    private Site site = Site.me().setSleepTime(1).setTimeOut(10000).setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36").setRetryTimes(3);

    private static final String ROOT_LINK = config.getRootLink();
    private static final int threadNum = 8;
    private static final String SAVE_LINKS_KEY = "saved_links";

    public static void main(String[] args) throws JMException, InterruptedException {

        Crawler crawler = new Crawler();
        crawler.initEbeanServer();
        crawler.jedisPool = createJedisPool();
        Spider spider = Spider.create(crawler);
        spider.setScheduler(new RedisScheduler(config.getRedisIP()));

        spider.addUrl(ROOT_LINK).thread(threadNum).start();

        SpiderMonitor.instance().register(spider);

    }

    @Override
    public void process(Page page) {
        try {

            List<String> allLinks = page.getHtml().links().all();

            String currentUrl = page.getRequest().getUrl();

            page.addTargetRequests(getNextTargetRequest(allLinks, currentUrl));

            // filter non persist links
            List<Link> linkList = allLinks.stream()
                    .filter(l -> !l.equals(currentUrl))
                    .filter(l -> l.trim().length() > 0)
                    .map(l -> Link.convert(ROOT_LINK, l))
                    .filter(ll -> {
                        Jedis jedis = jedisPool.getResource();
                        boolean result = !jedis.hexists(SAVE_LINKS_KEY, ll.getKey());
                        jedis.close();
                        return result;
                    })
                    .collect(Collectors.toList());


            // save links to db
            Link.saveAll(linkList);

            // persist links to redis
            persistSavedLinks(linkList);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private List<String> getNextTargetRequest(List<String> alllinks, String currentUrl){
        return alllinks.stream()
                .filter(l -> l.endsWith("/"))
                .filter(l -> !l.equals(currentUrl))
                .collect(Collectors.toList());

    }

    private void persistSavedLinks(List<Link> linkList) throws IOException {
        Jedis jedis = jedisPool.getResource();
        Transaction multi = jedis.multi();

        linkList.forEach(link -> {
            multi.hset(SAVE_LINKS_KEY, link.getKey(), 0+"");
        });
        multi.exec();
        multi.close();
        jedis.close();
    }

    @Override
    public Site getSite() {
        return site;
    }

    private void initEbeanServer() {
        ServerConfig config = new ServerConfig();
        config.setName("mysql");
        config.setCurrentUserProvider(() -> "mysql");
        config.setDatabasePlatformName("mysql");
        config.loadFromProperties();

        config.setDefaultServer(true);
        config.setRegister(true);
        config.addPackage("codes.showme.mavenrepocrawler.domain");

        EbeanServer ebeanServer = EbeanServerFactory.create(config);

        Ebean.register(ebeanServer, true);

    }

    public static JedisPool createJedisPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        String ip = config.getRedisIP();
        int port = config.getRedisPort();
        int timeout = 2000;
        jedisPoolConfig.setMaxTotal(2048);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setMaxWaitMillis(100);
        jedisPoolConfig.setTestOnBorrow(false);
        jedisPoolConfig.setTestOnReturn(true);
        // 初始化JedisPool
        return new JedisPool(jedisPoolConfig, ip, port, timeout);
    }
}
