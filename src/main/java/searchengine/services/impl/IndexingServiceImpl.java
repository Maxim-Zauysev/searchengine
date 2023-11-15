package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;

import searchengine.TextAnalyzer;
import searchengine.config.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.TransactionalService;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
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
    private final ConcurrentHashMap<Future, Site> activeIndexingTasks = new ConcurrentHashMap<>();

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

    @Override
    public void stopIndexing() {
        // Interrupt all running tasks
        for (Map.Entry<Future, Site> entry : activeIndexingTasks.entrySet()) {
            entry.getKey().cancel(true); // Interrupt the task
            Site site = entry.getValue();
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация остановлена пользователем");
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site); // Update the site status in the database
        }
        activeIndexingTasks.clear(); // Clear the list of active tasks
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
            transactionalService.deleteSiteInfo(site);

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

            Map<String, Integer> lemmasCount = textAnalyzer.analyzeText(textAnalyzer.removeHtmlTags(htmlContent));
            saveLemmasAndIndexes(site, page, lemmasCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLemmasAndIndexes(Site site, Page page, Map<String, Integer> lemmasCount) {
        for (Map.Entry<String, Integer> entry : lemmasCount.entrySet()) {
            String lemmaString = entry.getKey();
            Integer count = entry.getValue();

            List<Lemma> existingLemmas = lemmaRepository.findByLemmaAndSite(lemmaString, site);

            Lemma lemma;
            if (existingLemmas.isEmpty()) {
                lemma = new Lemma();
                lemma.setLemma(lemmaString);
                lemma.setFrequency(1);
                lemma.setSite(site);
            } else {
                // Assuming the first match is what you need or handle duplicates appropriately
                lemma = existingLemmas.get(0);
                lemma.setFrequency(lemma.getFrequency() + 1);
            }
            lemmaRepository.save(lemma);

            Index index = new Index();
            index.setLemma(lemma);
            index.setPage(page);
            index.setRank(count.floatValue());
            indexRepository.save(index);
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