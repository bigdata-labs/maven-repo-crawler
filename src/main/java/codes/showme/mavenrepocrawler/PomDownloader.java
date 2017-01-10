package codes.showme.mavenrepocrawler;

import codes.showme.mavenrepocrawler.common.Configuration;
import codes.showme.mavenrepocrawler.common.PropertiesConfig;
import codes.showme.mavenrepocrawler.domain.Link;
import codes.showme.mavenrepocrawler.domain.PomContent;
import io.ebean.PagedList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by jack on 1/8/17.
 */
public class PomDownloader {

    private static final Configuration config = new PropertiesConfig();

    private static AtomicInteger pageIndex = new AtomicInteger(0);
    private static final int pageSize = 100;


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(12);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new DownloaderProcessor());
        }
    }

    private static class DownloaderProcessor implements Runnable {
        private OkHttpClient client = new OkHttpClient();

        @Override
        public void run() {
            while (true) {
                PagedList<Link> linkPagedList = Link.pomFilePagedList(pageIndex.getAndIncrement(), pageSize);
                if (linkPagedList.getList().size() <= 0) {
                    break;
                }
                List<PomContent> pomContentList = linkPagedList.getList().stream().filter(Objects::nonNull)
                        .filter(l -> l.getLink() != null && l.getLink().trim().length() > 0)
                        .map(Link::getLink)
                        .map(link -> {
                            Request request = new Request.Builder()
                                    .url(config.getRootLink() + link)
                                    .build();
                            try {
                                try (Response response = client.newCall(request).execute()) {
                                    return new PomContent(link, response.body().string());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull).collect(Collectors.toList());

                PomContent.db().saveAll(pomContentList);
            }
        }
    }

}
