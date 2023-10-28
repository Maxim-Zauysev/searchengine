package searchengine.config;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextAnalyzer {
    private LuceneMorphology luceneMorph;

    public TextAnalyzer() {
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> analyzeText(String text) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^а-я\\s]", "").split("\\s+"); // Преобразование к нижнему регистру и удаление символов, не являющихся буквами
        for (String word : words) {
            List<String> wordInfo = luceneMorph.getMorphInfo(word);

            if (!wordInfo.stream().anyMatch(info -> info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"))) {
                List<String> wordBaseForms = luceneMorph.getNormalForms(word);
                if (!wordBaseForms.isEmpty()) {
                    String baseForm = wordBaseForms.get(0);
                    wordCount.put(baseForm, wordCount.getOrDefault(baseForm, 0) + 1);
                }
            }
        }
        return wordCount;
    }

    public String removeHtmlTags(String html) {
        String htmlTagPattern = "<[^>]*>";
        Pattern pattern = Pattern.compile(htmlTagPattern);
        Matcher matcher = pattern.matcher(html);

        String cleanText = matcher.replaceAll("");

        return cleanText;
    }
}


