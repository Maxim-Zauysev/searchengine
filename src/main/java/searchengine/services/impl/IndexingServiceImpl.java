package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Page;
import searchengine.config.Site;
import searchengine.config.SiteStatus;
import searchengine.config.SitesList;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService  {


    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;

    private final SitesList sites;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public void startIndexing() {


        List<Site> sitesToIndex = sites.getSites();
        sitesToIndex.forEach((site -> {
            forkJoinPool.submit(() -> processSite(site));
        }));
    }

    private void processSite(Site site) {
        try {

            if(siteRepository.existsByUrl(site.getUrl())){
                pageRepository.deleteBySite(site.getId());
                siteRepository.delete(site);
            }
            site.setStatus(SiteStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            Document doc = Jsoup.connect(site.getUrl())
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();

            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String url = link.absUrl("href");

                if (!pageRepository.existsByPath(url)) {
                    forkJoinPool.submit(() -> processPage(site, url));
                }
            }

            site.setStatus(SiteStatus.INDEXED);
            site.setStatusTime(LocalDateTime.now());
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError(e.getMessage());
            site.setStatusTime(LocalDateTime.now());
            e.printStackTrace();
        } finally {
            siteRepository.save(site);
        }
    }

    private void processPage(Site site, String url) {
        try {
            Page page = new Page();
            page.setPath(url);
            page.setSite(site);

            Document pageDoc = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com")
                    .get();

            page.setContent(pageDoc.html());
            //200 временная заглушка
            page.setCode(200);

            pageRepository.save(page);
        } catch (Exception e) {
        }
    }
}

