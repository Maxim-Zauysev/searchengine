package searchengine;

import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        TextAnalyzer textAnalyzer = new TextAnalyzer();

        String text = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
                "что леопард постоянно обитает в некоторых районах Северного\n" +
                "Кавказа.";
        String html = "<!DOCTYPE html>\n" +
				"<html>\n" +
				"<head>\n" +
				"    <title>Пример HTML</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"    <h1>Заголовок</h1>\n" +
				"    <p>Это абзац текста.</p>\n" +
				"    <a href=\"https://www.example.com\">Ссылка на Example.com</a>\n" +
				"</body>\n" +
				"</html>\n";

        Map<String, Integer> wordCount = textAnalyzer.analyzeText(text);
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            System.out.println(entry.getKey() + " - "+ entry.getValue());
        }

		System.out.println(textAnalyzer.removeHtmlTags(html));

    }
}
