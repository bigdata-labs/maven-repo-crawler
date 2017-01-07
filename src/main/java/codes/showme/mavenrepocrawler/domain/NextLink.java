package codes.showme.mavenrepocrawler.domain;


import io.ebean.Model;
import io.ebean.Update;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/7/17.
 */

@Entity
@Table(name = "next_links")
public class NextLink extends Model implements Serializable {


    private static final long serialVersionUID = -6429619640405460252L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    private long version;

    /**
     * 下一个要爬的链接
     */
    @Column(name = "link", length = 2048)
    private String nextLink;

    /**
     * 已爬
     */
    @Column(name = "crawled")
    private boolean crawled = false;

    public NextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public static void saveToDB(Set<String> links){
        List<NextLink> nextLinks = links.stream().map(NextLink::new).collect(Collectors.toList());
        Link.ebeanServer.insertAll(nextLinks);

    }

    public static List<String> getNextTargetRequests(int count) {
        List<NextLink> list = Link.ebeanServer.find(NextLink.class).where().isNotNull("link").eq("crawled", false).setMaxRows(Math.max(1, count)).findList();
        return list.stream().map(NextLink::getNextLink).collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public boolean isCrawled() {
        return crawled;
    }

    public void setCrawled(boolean crawled) {
        this.crawled = crawled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NextLink nextLink1 = (NextLink) o;

        if (crawled != nextLink1.crawled) return false;
        return nextLink != null ? nextLink.equals(nextLink1.nextLink) : nextLink1.nextLink == null;
    }

    @Override
    public int hashCode() {
        int result = nextLink != null ? nextLink.hashCode() : 0;
        result = 31 * result + (crawled ? 1 : 0);
        return result;
    }

    public static int crawled(String url) {
        Update<NextLink> upd = Link.ebeanServer.createUpdate(NextLink.class, "UPDATE next_links SET crawled = 1 WHERE link=:link");
        upd.set("link", url);
        return upd.execute();
    }

}
