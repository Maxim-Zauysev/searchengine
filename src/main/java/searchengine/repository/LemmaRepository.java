package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Lemma;
import searchengine.config.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma,Integer> {
    List<Lemma> findByLemmaAndSite(String lemma, Site site);
    @Modifying
    @Query(value = "DELETE FROM lemma WHERE site_id = ?1", nativeQuery = true)
    void deleteBySite(Integer siteId);

    // Method to count lemmas by site
    Integer countBySite(Site site);
}
