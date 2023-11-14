package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.config.Index;
import searchengine.config.Page;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index,Integer> {

}
