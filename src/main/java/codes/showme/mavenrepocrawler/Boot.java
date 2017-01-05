package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.domain.Link;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/2/17.
 */
public class Boot implements PageProcessor {

    private Site site = Site.me().setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36").setRetryTimes(3).setSleepTime(100).setTimeOut(10000);
    private static final String ROOT_LINK = "https://repo1.maven.org/maven2/";
    private static final Pattern SUFFIXES_PATTERN = Pattern.compile("\\w*\\.(sha1|jar|pom|xml|md5|asc|properties)");

    @Override
    public void process(Page page) {
        page.addTargetRequests(page.getHtml().links().all().stream()
                .filter(l -> l.endsWith("/"))
                .collect(Collectors.toList()));
        page.addTargetRequest(page.getRequest());

        String currentUrl = page.getUrl().get();
        String relativePath = currentUrl.substring(currentUrl.indexOf(ROOT_LINK) + ROOT_LINK.length());

        Link link = new Link();

        //  path type like pom jar xml
        Matcher matcher = SUFFIXES_PATTERN.matcher(relativePath);
        if (matcher.find()) {
            link.setPathType(matcher.group(1));
        }

        // relativePath: org/springframework/boot/spring-boot-parent/1.3.1.RELEASE/
        link.setLink(relativePath);

        String[] pathSplitted = relativePath.split("\\/");

        //the level of path org/springframework/boot/spring-boot-parent/1.3.1.RELEASE/ is 5
        link.setLevel(pathSplitted.length);

        //the parent link of path org/springframework/boot/spring-boot-parent/1.3.1.RELEASE/ is org/springframework/boot/spring-boot-parent/
        if (pathSplitted.length > 1) {
            link.setParentLink(StringUtils.join(Arrays.asList(pathSplitted).subList(0, pathSplitted.length - 1), "/") + "/");
        }

        link.save();

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Boot boot = new Boot();
        boot.initEbeanServer();
        Spider.create(boot).addUrl(ROOT_LINK).thread(20).run();

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
