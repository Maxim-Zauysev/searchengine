package searchengine.services;

import searchengine.dto.SearchResult;
import searchengine.dto.SearchResults;

import java.util.List;

public interface SearchService {
    List<SearchResult> siteSearch(String text, String url, int offset, int limit);
    List<SearchResult>   allSiteSearch(String text, int offset, int limit);
}