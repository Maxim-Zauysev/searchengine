package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.transaction.annotation.Transactional;
import searchengine.TextAnalyzer;
import searchengine.config.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.TransactionalService;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private TransactionalService transactionalService;

    private final SitesList sites;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final TextAnalyzer textAnalyzer = new TextAnalyzer();

    @Override
    public void indexPage(String url) {
        if(isValidUrl(url)) {
            Site site = new Site();
            site.setName(extractSiteName(url));
            site.setUrl(url);
            forkJoinPool.submit(() -> processSite(site));
        }else {
            throw new IllegalArgumentException();
        }
    }

    //TODO:
    @Override
    public void stopIndexing() {

    }

    @Override
    public void startIndexing() {
        List<Site> sitesToIndex = sites.getSites();
        sitesToIndex.forEach((site -> {
            forkJoinPool.submit(() -> processSite(site));
        }));
    }

    void processSite(Site site) {
        try {
            transactionalService.deleteSiteAndPages(site);

            site.setStatus(SiteStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            Site savedSite = siteRepository.save(site);

            Document doc = Jsoup.connect(site.getUrl())
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();

            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String url = link.absUrl("href");

                if (url.startsWith(savedSite.getUrl()) && !pageRepository.existsByPath(url)) {
                    forkJoinPool.submit(() -> processPage(savedSite, url));
                }
            }

            savedSite.setStatus(SiteStatus.INDEXED);
            savedSite.setStatusTime(LocalDateTime.now());
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError(e.getMessage());
            site.setStatusTime(LocalDateTime.now());
            e.printStackTrace();
        } finally {
            siteRepository.save(site);
        }
    }

    void processPage(Site site, String url) {
        try {

            Page page = new Page();
            page.setPath(url);
            page.setSite(site);

            Connection connection = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com");

            Connection.Response response = connection.execute();
            Document pageDoc = response.parse();

            String htmlContent = pageDoc.html();
            page.setContent(htmlContent);
            page.setCode(response.statusCode());

            pageRepository.save(page);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String extractSiteName(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                // Удаляем www и вырезаем только доменное имя
                host = host.startsWith("www.") ? host.substring(4) : host;
                return host;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null; // Если не удалось извлечь имя сайта
    }

    private boolean isValidUrl(String url) {
        String urlPattern = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(/\\S*)?$";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

}