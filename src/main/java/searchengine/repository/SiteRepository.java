package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Site;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site,Long> {
    void deleteById(Long id);
    List<Site> findByUrl(String url);
    void deleteByUrl(String url);
    boolean existsByUrl(String url);
}
