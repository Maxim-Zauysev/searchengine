package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import org.apache.log4j.Logger;


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
    public void deleteSiteInfo(Site site) {
        if(siteRepository.existsByUrl(site.getUrl())){
            Integer siteIdToRemove = siteRepository.getSiteIdByUrl(site.getUrl()).get(0);

            indexTableRepository.deleteBySite(siteIdToRemove);
            lemmaRepository.deleteBySite(siteIdToRemove);
            pageRepository.deleteBySite(siteIdToRemove);
            siteRepository.deleteSiteById(siteIdToRemove);
        }
    }


}
