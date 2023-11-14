package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Lemma;
import searchengine.config.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma,Integer> {

}
