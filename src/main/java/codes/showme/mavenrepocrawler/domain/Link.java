package codes.showme.mavenrepocrawler.domain;


import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by jack on 1/2/17.
 */
@Entity
@Table(name = "links")
public class Link extends Model implements Serializable {

    private static EbeanServer ebeanServer = Ebean.getDefaultServer();

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
}
