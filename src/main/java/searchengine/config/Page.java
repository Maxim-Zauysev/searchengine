package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

    @Setter
    @Getter
    @Entity
    @Table(name = "page")
    public class Page {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne
        @JoinColumn(name = "site_id", nullable = false)
        private Site site;

        @Column(columnDefinition = "TEXT NOT NULL")
        private String path;

        @Column(columnDefinition = "INT NOT NULL")
        private Integer code;

        @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
        private String content;

        public Page() {
        }
    }
