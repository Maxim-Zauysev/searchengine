package searchengine;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
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
        // Разрешаем буквы, пробелы и дефисы
        String[] words = text.toLowerCase().replaceAll("[^а-я\\s-]", "").split("\\s+");

        for (String word : words) {
            // Пропускаем слишком короткие слова
            if (word.length() < 3) continue;

            // Обработка слов с дефисом
            if (word.contains("-")) {
                String[] parts = word.split("-");
                for (String part : parts) {
                    processWord(part, wordCount);
                }
                continue;
            }

            // Обычная обработка слова
            processWord(word, wordCount);
        }
        return wordCount;
    }

    private void processWord(String word, Map<String, Integer> wordCount) {
        if (luceneMorph.checkString(word)) {
            try {
                List<String> wordInfo = luceneMorph.getMorphInfo(word);
                if (!wordInfo.stream().anyMatch(info -> info.contains("СОЮЗ") || info.contains("ПРЕДЛ") || info.contains("МЕЖД"))) {
                    List<String> wordBaseForms = luceneMorph.getNormalForms(word);
                    if (!wordBaseForms.isEmpty()) {
                        String baseForm = wordBaseForms.get(0);
                        wordCount.put(baseForm, wordCount.getOrDefault(baseForm, 0) + 1);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Unable to get morph info for word: " + word);
            }
        }
    }


    public String removeHtmlTags(String html) {
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        return text;
    }
}


