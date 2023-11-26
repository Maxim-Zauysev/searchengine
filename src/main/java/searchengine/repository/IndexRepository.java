package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.config.Index;
import searchengine.config.Lemma;
import searchengine.config.Page;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index,Integer> {
    @Modifying
    @Query(value = "DELETE FROM index_table WHERE page_id IN (SELECT id FROM page WHERE site_id = ?1)", nativeQuery = true)
    void deleteBySite(Integer siteId);

    List<Index> findByLemmaId(Integer lemmaId);
    List<Index> findByPageId(Integer pageId);
    Index findByPageAndLemma(Page page, Lemma lemma);
}
