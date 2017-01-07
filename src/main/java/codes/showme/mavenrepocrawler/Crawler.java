package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.domain.Link;
import codes.showme.mavenrepocrawler.domain.NextLink;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import javax.management.JMException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/2/17.
 */
public class Crawler implements PageProcessor {

    private Site site = Site.me().setSleepTime(1).setTimeOut(10000).setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36").setRetryTimes(3);

    private static final String next_pages_object_path = "next_pages_.out";
    private static final String ROOT_LINK = "https://repo1.maven.org/maven2/";
    private static final int threadNum = 8;

    @Override
    public void process(Page page) {
        try {

            List<String> allLinks = page.getHtml().links().all();
            List<String> next_pages = allLinks.stream()
                    .filter(l -> l.endsWith("/"))
                    .filter(l -> !l.equals(page.getRequest().getUrl()))
                    .collect(Collectors.toList());

            page.addTargetRequests(next_pages);

            List<Link> linkList = allLinks.stream()
                    .filter(l -> !l.equals(page.getRequest().getUrl()))
                    .filter(l -> l.trim().length() > 0)
                    .map(l -> Link.convert(ROOT_LINK, l))
                    .collect(Collectors.toList());

            Link.saveAll(linkList);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }


    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws JMException {
        Crawler crawler = new Crawler();
        crawler.initEbeanServer();
        Spider spider = Spider.create(crawler);
        spider.setScheduler(new RedisScheduler("127.0.0.1"));

        spider.addUrl(ROOT_LINK).thread(threadNum).start();

        SpiderMonitor.instance().register(spider);

    }

    private void initEbeanServer() {
        ServerConfig config = new ServerConfig();
        config.setName("sqlite");
//        config.setCurrentUserProvider(() -> "mysql");
        config.setDatabasePlatformName("sqlite");
        config.loadFromProperties();


        config.setDefaultServer(true);
        config.setRegister(true);
        config.addPackage("codes.showme.mavenrepocrawler.domain");

        EbeanServer ebeanServer = EbeanServerFactory.create(config);

        Ebean.register(ebeanServer, true);

    }

    private static void saveNextPagesObject(List<String> next_pages) throws IOException {

        File file = new File(next_pages_object_path);
        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(file));
        oout.writeObject(next_pages);
        oout.close();
    }
}
