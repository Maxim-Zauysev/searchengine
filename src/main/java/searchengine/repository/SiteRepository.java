package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.config.Site;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {
    void deleteById(Integer id);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM site WHERE url = :url")
    int countByUrl(@Param("url") String url);

    default boolean existsByUrl(String url) {
        return countByUrl(url) > 0;
    }

    @Modifying
    @Query(nativeQuery = true, value = "SELECT id FROM site WHERE url = :url")
    List<Integer> getSiteIdByUrl(@Param("url") String url);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM site WHERE id = :id")
    void deleteSiteById(@Param("id") Integer id);

    Site findByUrl(String site);
}
