package searchengine.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class SearchResults {
    private boolean result;
    private int count;
    private List<SearchResult> data;

    public SearchResults(boolean result, int count, List<SearchResult> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

    // Constructors, getters, and setters
}
