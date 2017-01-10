package codes.showme.mavenrepocrawler.domain;


import io.ebean.Model;
import io.ebean.PagedList;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jack on 1/2/17.
 */
@Entity
@Table(name = "links")
public class Link extends Model implements Serializable {

    private static final long serialVersionUID = -4211827981727329075L;

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

    @Column(name = "group_id", length = 128)
    private String groupId;

    @Column(name = "artifact_id", length = 50)
    private String artifactId;

    @Column(name = "artifact_version", length = 40)
    private String artifact_version;

    public Link(String relativePath) {
        //  path type like pom jar xml
        if (!relativePath.endsWith("/")) {
            String aPath = relativePath.substring(relativePath.lastIndexOf(".") + 1);
            setPathType(aPath);
        }

        if (relativePath.endsWith(".pom")) {
            String[] split = relativePath.split("/");
            if (split.length >= 4) {
                setArtifact_version(split[split.length - 2]);
                setArtifactId(split[split.length - 3]);
                setGroupId(Stream.of(split).limit(split.length - 3).collect(Collectors.joining(".")));
            }
        }

        // relativePath: org/springframework/boot/spring-boot-parent/1.3.1.RELEASE/
        setLink(relativePath);

        String[] pathSplitted = relativePath.split("\\/");


        //the parent link of path org/springframework/boot/spring-boot-parent/1.3.1.RELEASE/ is org/springframework/boot/spring-boot-parent/
        if (pathSplitted.length > 1) {
            setParentLink(StringUtils.join(Arrays.asList(pathSplitted).subList(0, pathSplitted.length - 1), "/") + "/");
        }
    }

    public void save() {
        db().save(this);
    }

    public static void saveAll(List<Link> linkList) {
        try {
            db().insertAll(linkList);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getArtifact_version() {
        return artifact_version;
    }

    public void setArtifact_version(String artifact_version) {
        this.artifact_version = artifact_version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link1 = (Link) o;

        if (link != null ? !link.equals(link1.link) : link1.link != null) return false;
        if (parentLink != null ? !parentLink.equals(link1.parentLink) : link1.parentLink != null) return false;
        return pathType != null ? pathType.equals(link1.pathType) : link1.pathType == null;
    }

    @Override
    public int hashCode() {
        int result = link != null ? link.hashCode() : 0;
        result = 31 * result + (parentLink != null ? parentLink.hashCode() : 0);
        result = 31 * result + (pathType != null ? pathType.hashCode() : 0);
        return result;
    }

    /**
     * count all pom's file
     *
     * @return
     */
    public static int allPomFileCount() {
        return db().find(Link.class).where().eq("path_type", "pom").findCount();
    }

    public static PagedList<Link> pomFilePagedList(int pageIndex, int pageSize) {
        return db().find(Link.class)
                .where()
                .eq("path_type", "pom")
                .order().asc("id")
                .setFirstRow(pageIndex * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();
    }
}
