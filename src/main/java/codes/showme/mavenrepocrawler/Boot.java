package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.domain.Link;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/2/17.
 */
public class Boot implements PageProcessor {

    private static final Pattern CURRENT_URL_PATTERN = Pattern.compile("\\/maven2(\\/([\\/\\w \\.-]*)*)");
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100).setTimeOut(10000);

    @Override
    public void process(Page page) {
        page.addTargetRequests(page.getHtml().links().all().stream()
                .filter(l -> l.endsWith("/")
                        || l.endsWith(".pom")
                        || l.endsWith(".md5")
                        || l.endsWith(".sha1")
                        || l.endsWith(".asc")
                        || l.endsWith(".xml"))
                .collect(Collectors.toList()));

        page.addTargetRequest(page.getRequest());
        String h1 = page.getHtml().$("h1").get();
        Matcher matcher = CURRENT_URL_PATTERN.matcher(h1);
        if (matcher.find()) {
            String group = matcher.group(1);
            page.putField("link", group);
            Link link = new Link();
            link.setLink(group);
            link.setLevel(group.split("\\/").length - 1);
            link.save();
        }


    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Boot boot = new Boot();
        boot.initEbeanServer();
        Spider.create(boot).addUrl("https://repo1.maven.org/maven2/").thread(20).run();

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
}
