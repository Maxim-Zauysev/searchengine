package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import searchengine.dto.PageResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
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

