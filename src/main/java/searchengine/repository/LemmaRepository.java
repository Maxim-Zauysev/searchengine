package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.config.Lemma;
import searchengine.config.Site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma,Integer> {
    List<Lemma> findByLemmaAndSite(String lemma, Site site);
    @Modifying
    @Query(value = "DELETE FROM lemma WHERE site_id = ?1", nativeQuery = true)
    void deleteBySite(Integer siteId);
    Integer countBySite(Site site);
    List<Lemma> findAllByLemmaIn(Collection<String> lemmas); // Keep this method
    Lemma findByLemma(String lemma);
}
