package codes.showme.mavenrepocrawler.domain;


import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Model;
import io.ebeaninternal.server.transaction.AutoCommitJdbcTransaction;
import io.ebeaninternal.server.transaction.JdbcTransaction;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jack on 1/2/17.
 */
@Entity
@Table(name = "links")
public class Link extends Model implements Serializable {

    public static EbeanServer ebeanServer = Ebean.getDefaultServer();

    private static final long serialVersionUID = -4211827981727329075L;


    private static final Pattern SUFFIXES_PATTERN = Pattern.compile("\\w*\\.(sha1|jar|pom|xml|md5|asc|properties)");


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    private long version;

    /**
     * 链接
     */
    @Column(name = "link", length = 1024)
    private String link;

    @Column(name = "parent_link", length = 1024)
    private String parentLink;

    @Column(name = "path_type", length = 1024)
    private String pathType;


    @Column(name = "level")
    private int level;

    public void save() {
        ebeanServer.save(this);
    }

    public static void saveAll(List<Link> linkList) {
        try {
            ebeanServer.insertAll(linkList);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static Link convert(String rootLink, String url) {
        if (url.length() < rootLink.length()) {
            return new Link();
        }

        int beginIndex = url.indexOf(rootLink) + rootLink.length();
        String relativePath = url.substring(beginIndex);
        Link link = new Link();

        //  path type like pom jar xml
        if (!url.endsWith("/")) {
            String aPath = relativePath.substring(relativePath.lastIndexOf(".") + 1);
            link.setPathType(aPath);
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
        return link;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getParentLink() {
        return parentLink;
    }

    public void setParentLink(String parentLink) {
        this.parentLink = parentLink;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link1 = (Link) o;

        if (level != link1.level) return false;
        if (link != null ? !link.equals(link1.link) : link1.link != null) return false;
        if (parentLink != null ? !parentLink.equals(link1.parentLink) : link1.parentLink != null) return false;
        return pathType != null ? pathType.equals(link1.pathType) : link1.pathType == null;
    }

    @Override
    public int hashCode() {
        int result = link != null ? link.hashCode() : 0;
        result = 31 * result + (parentLink != null ? parentLink.hashCode() : 0);
        result = 31 * result + (pathType != null ? pathType.hashCode() : 0);
        result = 31 * result + level;
        return result;
    }

    /**
     * for redis
     * @return
     */
    public String getKey() {
        return link + parentLink + pathType + level;
    }

    /**
     * count all pom's file
     * @return
     */
    public static int allPomFileCount() {
        return ebeanServer.find(Link.class).where().eq("path_type", "pom").findCount();
    }
}
