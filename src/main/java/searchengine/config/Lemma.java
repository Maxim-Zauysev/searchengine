package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "site_id",columnDefinition = "INT NOT NULL")
    private Integer siteId;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;

    @Column(name = "frequency", columnDefinition = "INT NOT NULL")
    private Integer frequency;

    public Lemma() {
    }
}


