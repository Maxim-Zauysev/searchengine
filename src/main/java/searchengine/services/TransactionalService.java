package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Index;
import searchengine.config.Lemma;
import searchengine.config.Page;
import searchengine.config.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import org.apache.log4j.Logger;

import java.util.List;


@Service
public class TransactionalService {

    Logger logger = Logger.getLogger(TransactionalService.class);

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IndexRepository indexTableRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Transactional
    public void deleteSiteAndPages(Site site) {
        if(siteRepository.existsByUrl(site.getUrl())){
            Integer sideIdToRemove = siteRepository.getSiteIdByUrl(site.getUrl()).get(0);

            pageRepository.deleteBySite(sideIdToRemove);
            siteRepository.deleteSiteById(sideIdToRemove);

        }
    }

}
