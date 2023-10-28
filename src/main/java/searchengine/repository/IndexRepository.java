package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Index;
import searchengine.config.Page;

@Repository
public interface IndexRepository extends CrudRepository<Index,Integer> {
}
