package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.common.Configuration;
import codes.showme.mavenrepocrawler.common.PropertiesConfig;
import codes.showme.mavenrepocrawler.domain.Link;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import javax.management.JMException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/2/17.
 */
public class Crawler implements PageProcessor {

    private static final Configuration config = new PropertiesConfig();

    private Site site = Site.me().setSleepTime(1).setTimeOut(10000).setUserAgent("Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16").setRetryTimes(3);

    private static final String ROOT_LINK = config.getRootLink();
    private static final int threadNum = 12;
    String filePath = "/Users/jack/links.txt";

    public static void main(String[] args) throws JMException, InterruptedException {

        Crawler crawler = new Crawler();
//        crawler.initEbeanServer();
        Spider spider = Spider.create(crawler);
        spider.setScheduler(new RedisScheduler(config.getRedisIP()));
        spider.addUrl(ROOT_LINK).thread(threadNum).start();

    }

    @Override
    public void process(Page page) {
        try {
            String currentUrl = page.getRequest().getUrl();

            List<String> allLinks = page.getHtml().xpath("a/text()").all().stream()
                    .filter(l -> !l.equals("../") && !l.equals("Parent Directory"))
                    .map(l -> currentUrl + l)
                    .collect(Collectors.toList());

            page.addTargetRequests(getNextTargetRequest(allLinks, currentUrl));

            String linksBlock
                    = allLinks.stream()
                    .filter(l -> !l.equals(currentUrl))
                    .filter(l -> l.trim().length() > 0)
                    .filter(l -> l.length() > ROOT_LINK.length())
                    .map(l -> l.substring(l.indexOf(ROOT_LINK) + ROOT_LINK.length()))
                    .reduce("\n", (s1, s2) -> s1 + "\n"+s2);

            synchronized (this) {
                try (Writer fileWriter = new FileWriter(filePath, true)){
                    fileWriter.write(linksBlock);
                } catch (IOException e) {
                    System.out.println("Problem occurs when deleting the directory : " + filePath);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void saveLinksToHDFS(String linksBlock) throws IOException {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        // hadoopnamenode -> 192.168.11.154
        configuration.set("fs.default.name", config.getHadoopURL());
        FileSystem fs = FileSystem.get(configuration);
        Path linksSavedPath = new Path(config.getLinksDirectory());
        if (!fs.exists(linksSavedPath)) {
            fs.create(linksSavedPath);
        }
        try (FSDataOutputStream out = fs.append(linksSavedPath)) {
            out.write(("\n" + linksBlock).getBytes("UTF-8"));
        }

    }

    private static void saveLinksToDB(List<String> allLinks, String currentUrl){
        List<Link> linkList = allLinks.stream()
                .filter(l -> !l.equals(currentUrl))
                .filter(l -> l.trim().length() > 0)
                .filter(l -> l.length() > ROOT_LINK.length())
                .map(l -> {
                    int beginIndex = l.indexOf(ROOT_LINK) + ROOT_LINK.length();
                    return new Link(l.substring(beginIndex));
                })
                .collect(Collectors.toList());


        // save links to db
        Link.saveAll(linkList);
    }

    private List<String> getNextTargetRequest(List<String> alllinks, String currentUrl) {
        return alllinks.stream()
                .filter(l -> !currentUrl.equals(l))
                .filter(l -> l.endsWith("/"))
                .collect(Collectors.toList());

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

    public static JedisPool createJedisPool() {
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
