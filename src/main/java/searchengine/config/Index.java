package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@Entity
@Table(name = "index_table")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "page_id", columnDefinition = "INT NOT NULL")
    private Integer pageId;

    @Column(name = "lemma_id", columnDefinition = "INT NOT NULL")
    private Integer lemmaId;

    @Column(name = "rank_value", columnDefinition = "FLOAT NOT NULL")
    private Float rank;


}
