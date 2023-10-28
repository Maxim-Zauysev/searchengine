package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.config.Page;
import searchengine.config.Site;

import java.util.List;

@Repository
public interface PageRepository  extends CrudRepository<Page,Integer> {

    boolean existsByPath(String path);

    @Modifying
    @Query(nativeQuery = true,value = "delete from page  where site_id=:siteId")
    void deleteBySite(@Param("siteId") Integer siteId);


    List<Page> findBySite(Site site);
}
