package searchengine.services;

import searchengine.config.Page;

public interface IndexingService {
    void startIndexing();

    void stopIndexing();

    void indexPage(String url);
}
