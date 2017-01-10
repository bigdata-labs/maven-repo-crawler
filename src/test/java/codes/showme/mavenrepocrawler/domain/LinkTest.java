package codes.showme.mavenrepocrawler.domain;

import org.junit.Assert;

/**
 * Created by jack on 1/11/17.
 */
public class LinkTest {

    @org.junit.Test
    public void construct() throws Exception {
        Link link = new Link("org/springframework/spring-context/4.2.7.RELEASE/spring-context-4.2.7.RELEASE.pom");
        Assert.assertEquals("org/springframework/spring-context/4.2.7.RELEASE/spring-context-4.2.7.RELEASE.pom", link.getLink());
        Assert.assertEquals("org/springframework/spring-context/4.2.7.RELEASE/", link.getParentLink());
        Assert.assertEquals("pom", link.getPathType());
        Assert.assertEquals("4.2.7.RELEASE", link.getArtifact_version());
        Assert.assertEquals("org.springframework", link.getGroupId());
        Assert.assertEquals("spring-context", link.getArtifactId());


        Link link1 = new Link("org/s/4.2.7.RELEASE.3.4.../spring.pom");
        Assert.assertEquals("org/s/4.2.7.RELEASE.3.4.../spring.pom", link1.getLink());
        Assert.assertEquals("org/s/4.2.7.RELEASE.3.4.../", link1.getParentLink());
        Assert.assertEquals("pom", link1.getPathType());
        Assert.assertEquals("4.2.7.RELEASE.3.4...", link1.getArtifact_version());
        Assert.assertEquals("org", link1.getGroupId());
        Assert.assertEquals("s", link1.getArtifactId());


        Link link2 = new Link("org/s/4.2.7.RELEASE.3.4.../spring.jar");
        Assert.assertEquals("org/s/4.2.7.RELEASE.3.4.../spring.jar", link2.getLink());
        Assert.assertEquals("org/s/4.2.7.RELEASE.3.4.../", link2.getParentLink());
        Assert.assertEquals("jar", link2.getPathType());
        Assert.assertNull(link2.getArtifact_version());
        Assert.assertNull(link2.getGroupId());
        Assert.assertNull(link2.getArtifactId());



        Link link3 = new Link("org/s/spring.jar");
        Assert.assertEquals("org/s/spring.jar", link3.getLink());
        Assert.assertEquals("org/s/", link3.getParentLink());
        Assert.assertEquals("jar",link3.getPathType());
        Assert.assertNull(link3.getArtifact_version());
        Assert.assertNull(link3.getGroupId());
        Assert.assertNull(link3.getArtifactId());


    }

}