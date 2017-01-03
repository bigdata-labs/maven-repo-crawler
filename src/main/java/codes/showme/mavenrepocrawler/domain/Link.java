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

    @Column(name = "has_child_link")
    private boolean hasChildLink;

    @Column(name = "md5", length = 2048)
    private String md5Value;

    @Column(name = "sha1", length = 2048)
    private String sha1Value;

    @Column(name = "level")
    private int level;

    @Column(name = "pom", columnDefinition = "TEXT")
    private String pom;

    public void save(){
        ebeanServer.save(this);
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

    public boolean isHasChildLink() {
        return hasChildLink;
    }

    public void setHasChildLink(boolean hasChildLink) {
        this.hasChildLink = hasChildLink;
    }

    public String getMd5Value() {
        return md5Value;
    }

    public void setMd5Value(String md5Value) {
        this.md5Value = md5Value;
    }

    public String getSha1Value() {
        return sha1Value;
    }

    public void setSha1Value(String sha1Value) {
        this.sha1Value = sha1Value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPom() {
        return pom;
    }

    public void setPom(String pom) {
        this.pom = pom;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Link{" +
                "id=" + id +
                ", link='" + link + '\'' +
                ", parentLink='" + parentLink + '\'' +
                ", hasChildLink=" + hasChildLink +
                ", md5Value='" + md5Value + '\'' +
                ", sha1Value='" + sha1Value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link1 = (Link) o;

        if (id != link1.id) return false;
        if (link != null ? !link.equals(link1.link) : link1.link != null) return false;
        if (md5Value != null ? !md5Value.equals(link1.md5Value) : link1.md5Value != null) return false;
        return sha1Value != null ? sha1Value.equals(link1.sha1Value) : link1.sha1Value == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (md5Value != null ? md5Value.hashCode() : 0);
        result = 31 * result + (sha1Value != null ? sha1Value.hashCode() : 0);
        return result;
    }
}
