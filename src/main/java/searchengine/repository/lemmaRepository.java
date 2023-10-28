package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Lemma;
import searchengine.config.Page;

@Repository
public interface lemmaRepository  extends CrudRepository<Lemma,Integer> {
}
