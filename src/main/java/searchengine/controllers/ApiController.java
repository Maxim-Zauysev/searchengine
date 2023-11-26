package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import searchengine.dto.BadRequest;
import searchengine.dto.PageResponse;
import searchengine.dto.SearchResult;
import searchengine.dto.SearchResults;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final SiteRepository siteRepository;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService, SiteRepository siteRepository) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.siteRepository = siteRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "")
                                             String request, @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (request.isEmpty())
            return createBadRequest("Empty request");

        if (!site.isEmpty() && siteRepository.findByUrl(site) == null)
            return createBadRequest("Required page not found");

        List<SearchResult> searchData = !site.isEmpty() ?
                searchService.siteSearch(request, site, offset, limit) :
                searchService.allSiteSearch(request, offset, limit);

        return new ResponseEntity<>(new SearchResults(true, searchData.size(), searchData), HttpStatus.OK);

    }

    private ResponseEntity<Object> createBadRequest(String message) {
        return new ResponseEntity<>(new BadRequest(false, message), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        indexingService.startIndexing();
        return ResponseEntity.ok("Индексация запущена.");
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam("url") String url) {
        if (isValidUrl(url)) {
            indexingService.indexPage(url.trim());
            return ResponseEntity.ok(new PageResponse(true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PageResponse(false, "Не корректная ссылка"));
        }

    }

    private boolean isValidUrl(String url) {
        String urlPattern = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(/\\S*)?$";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }


}

