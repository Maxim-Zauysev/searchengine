package searchengine.services.impl;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.TextAnalyzer;
import searchengine.config.Index;
import searchengine.config.Lemma;
import searchengine.config.Page;
import searchengine.dto.SearchResult;
import searchengine.dto.SearchResults;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.SearchService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;
    private TextAnalyzer textAnalyzer = new TextAnalyzer();
    private LuceneMorphology luceneMorph;

    public SearchServiceImpl() throws IOException {
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SearchResult> siteSearch(String text, String url, int offset, int limit) {
        return new ArrayList<>();
    }

//    @Override
//    public List<SearchResult> allSiteSearch(String text, int offset, int limit) {
//        List<SearchResult> result = new ArrayList<>();
//        Set<String> searchWords = new TreeSet<>();
//        String[] words = text.toLowerCase().replaceAll("[^а-я\\s]", "").split("\\s+");
//        for (String word : words) {
//            if (luceneMorph.checkString(word)) {
//                List<String> wordInfo = luceneMorph.getMorphInfo(word);
//
//                if (!wordInfo.stream().anyMatch(info -> info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"))) {
//                    List<String> wordBaseForms = luceneMorph.getNormalForms(word);
//                    if (!wordBaseForms.isEmpty()) {
//                        String baseForm = wordBaseForms.get(0);
//                        searchWords.add(baseForm);
//                    }
//                }
//            }
//        }
//
//        for (String word : searchWords) {
//            Lemma searchLemma = lemmaRepository.findByLemma(word);
//            if(searchLemma!= null ){
//                List<Index> indices = indexRepository.findByLemmaId(searchLemma.getId());
//                for (Index index : indices) {
//                    SearchResult addResult = new SearchResult();
//                    addResult.setSite(searchLemma.getSite().getUrl());
//                    addResult.setSiteName(searchLemma.getSite().getName());
//                    addResult.setUri(index.getPage().getPath());
//                    addResult.setTitle(extractTitle(index.getPage().getContent()));
//                    addResult.setRelevance(0.5f);
//                    addResult.setSnippet(searchLemma.getLemma());
//                    result.add(addResult);
//                }
//
//
//            }
//        }
//        return result;
//     }


    @Override
    public List<SearchResult> allSiteSearch(String text, int offset, int limit) {
        Set<String> lemmas = extractLemmas(text);
        List<Lemma> filteredLemmas = filterAndSortLemmas(lemmas);
        List<Page> pages = findPagesByLemmas(filteredLemmas);
        return calculateAndSortSearchResults(pages, filteredLemmas, offset, limit);
    }

    private List<Lemma> filterAndSortLemmas(Set<String> lemmas) {
        return lemmas.stream()
                .map(lemmaRepository::findByLemma)
                .filter(Objects::nonNull)
                .filter(this::isNotCommonLemma)
                .sorted(Comparator.comparing(Lemma::getFrequency))
                .collect(Collectors.toList());
    }

    private boolean isNotCommonLemma(Lemma lemma) {
        int threshold = 1000; // Set your threshold
        return lemma.getFrequency() < threshold;
    }
    public String removeHtmlTags(String html) {
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        return text;
    }
    private List<Page> findPagesByLemmas(List<Lemma> lemmas) {
        Set<Integer> pageIds = new HashSet<>();
        for (Lemma lemma : lemmas) {
            List<Index> indices = indexRepository.findByLemmaId(lemma.getId());
            if (pageIds.isEmpty()) {
                pageIds.addAll(indices.stream().map(index -> index.getPage().getId()).collect(Collectors.toSet()));
            } else {
                pageIds.retainAll(indices.stream().map(index -> index.getPage().getId()).collect(Collectors.toSet()));
            }
        }
        return pageIds.isEmpty() ? Collections.emptyList() : (List<Page>) pageRepository.findAllById(pageIds);
    }
    private Set<String> extractLemmas(String text) {
        Set<String> searchWords = new TreeSet<>();
        String[] words = text.toLowerCase().replaceAll("[^а-я\\s]", "").split("\\s+");
        for (String word : words) {
            if (luceneMorph.checkString(word)) {
                List<String> wordInfo = luceneMorph.getMorphInfo(word);
                if (!wordInfo.stream().anyMatch(info -> info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"))) {
                    List<String> wordBaseForms = luceneMorph.getNormalForms(word);
                    if (!wordBaseForms.isEmpty()) {
                        String baseForm = wordBaseForms.get(0);
                        searchWords.add(baseForm);
                    }
                }
            }
        }
        return searchWords;
    }

    private List<SearchResult> calculateAndSortSearchResults(List<Page> pages, List<Lemma> lemmas, int offset, int limit) {
        Map<Integer, Float> pageRelevance = new HashMap<>();
        float maxRelevance = 0;

        Set<String> lemmaWords = lemmas.stream().map(Lemma::getLemma).collect(Collectors.toSet());

        for (Page page : pages) {
            float relevance = 0;
            for (Lemma lemma : lemmas) {
                Index index = indexRepository.findByPageAndLemma(page, lemma);
                if (index != null) {
                    relevance += index.getRank();
                }
            }
            maxRelevance = Math.max(relevance, maxRelevance);
            pageRelevance.put(page.getId(), relevance);
        }

        // Create a final variable for use in the lambda expression
        final float finalMaxRelevance = maxRelevance;

        return pages.stream()
                .map(page -> createSearchResult(page, pageRelevance.get(page.getId()), finalMaxRelevance, lemmaWords))
                .sorted(Comparator.comparing(SearchResult::getRelevance).reversed())
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }


    private SearchResult createSearchResult(Page page, float absoluteRelevance, float maxRelevance, Set<String> lemmaWords) {
        float relativeRelevance = absoluteRelevance / maxRelevance;
        String snippet = generateSnippet(page.getContent(), lemmaWords);
        // Assuming site and siteName are accessible from the page object or elsewhere
        String site = page.getSite().getUrl();
        String siteName = page.getSite().getName();
        return new SearchResult(site, siteName, page.getPath(), extractTitle(page.getContent()), snippet, relativeRelevance);
    }

    private String generateSnippet(String content, Set<String> searchWords) {
        // Remove HTML tags from content
        String plainTextContent = removeHtmlTags(content);
        String lowerCaseContent = plainTextContent.toLowerCase();

        int snippetLength = 200; // Adjust this based on your needs
        int snippetStart = lowerCaseContent.length();
        for (String word : searchWords) {
            int wordPos = lowerCaseContent.indexOf(word);
            if (wordPos >= 0) {
                snippetStart = Math.min(snippetStart, Math.max(0, wordPos - snippetLength / 2));
            }
        }

        int snippetEnd = Math.min(plainTextContent.length(), snippetStart + snippetLength);
        // Extract the snippet
        String snippet = plainTextContent.substring(snippetStart, snippetEnd);

        // Optionally highlight search words in the snippet
        for (String word : searchWords) {
            snippet = snippet.replaceAll("(?i)" + Pattern.quote(word), "<b>" + word + "</b>");
        }

        return snippet;
    }


    public static String extractTitle(String html) {
        try {
            // Разбираем HTML-код с использованием Jsoup
            Document document = Jsoup.parse(html);

            // Извлекаем заголовок страницы
            String title = document.title();

            return title;
        } catch (Exception e) {
            // Обработка ошибок, если что-то пошло не так
            e.printStackTrace();
            return null;
        }
    }

}