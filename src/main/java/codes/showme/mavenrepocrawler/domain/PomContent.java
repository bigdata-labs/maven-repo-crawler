package codes.showme.mavenrepocrawler.domain;

import io.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by jack on 1/9/17.
 */
@Entity
@Table(name = "pom_content")
public class PomContent extends Model implements Serializable {

    private static final long serialVersionUID = 7787033845920836323L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    private long version;

    @Lob
    private String content;

    @Column(name = "link", length = 2048)
    private String link;

    public PomContent(String link, String pomContent) {
        this.link = link;
        this.content = pomContent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
