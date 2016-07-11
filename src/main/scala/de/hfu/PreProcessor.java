package de.hfu;

import org.apache.commons.lang.StringUtils;
import org.tartarus.snowball.ext.GermanStemmer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by chris on 11.06.2016.
 */
public class PreProcessor {

  private static BufferedReader br;
  private static List<String> stopWords = new ArrayList<String>();


  public static List<String> tokenizeAndRemoveStopwords(String target) {
    List<String> tokenizedTarget = tokenize(target);
    stem(tokenizedTarget);
    List<String> processedTarget = removeStopWords(tokenizedTarget);
    return processedTarget;
  }

  public static List<String> tokenizeAndRemoveAggressively(String target) {
    target = replaceSpecialChars(target);
    List<String> tokenizedTarget = new ArrayList<String>(Arrays.asList(target.split(" ")));
    List<String> capitalizedTarget = removeNonCapitalWords(tokenizedTarget);
    List<String> processedTarget = removeStopWords(capitalizedTarget);
    //stem(tokenizedTarget);
    return processedTarget;
  }

  public static List<String> tokenizeAndRemoveAggressivelyAndStem(String target) {
    List<String> processedTarget = tokenizeAndRemoveAggressively(target);
//    List<String> stemmedTarget = stem(processedTarget);
    return processedTarget;
  }

  private static List<String> removeNonCapitalWords(List<String> tokenizedTarget) {
    List<String> processedTarget = new ArrayList<String>();
    for (String term : tokenizedTarget) {
      if (StringUtils.isNotBlank(term) && Character.isUpperCase(term.charAt(0))) {
        processedTarget.add(term.toLowerCase());
      }
    }
    return processedTarget;
  }

  public static List<String> tokenize(String target) {
    target = replaceSpecialChars(target);
    return new ArrayList<String>(Arrays.asList(target.toLowerCase().split(" ")));
  }

  private static List<String> removeStopWords(List<String> tokenizedTarget) {
    if (stopWords.isEmpty()) {
      loadStopWordList();
    }
    List<String> processedTarget = new ArrayList<String>();
    for (String term : tokenizedTarget) {
      if (!stopWords.contains(term) && StringUtils.isNotBlank(term)) {
        processedTarget.add(term);
      }
    }
    return processedTarget;
  }

  private static List<String> stem(List<String> tokenizedTarget) {
    GermanStemmer stemmer = new GermanStemmer();
    List<String> stemmedTarget = new ArrayList<String>();
    for (String term : tokenizedTarget) {
      stemmer.setCurrent(term);
      stemmer.stem();
      String result = stemmer.getCurrent();
      stemmedTarget.add(result);
    }
    return stemmedTarget;
  }

  private static String replaceSpecialChars(String target) {
    //remove useless characters
    target = target.replace(":", "");
    target = target.replace("-", " ");
    target = target.replace(".", "");
    target = target.replace("–", "");
    target = target.replace("–", "");
    target = target.replace(";", "");
    target = target.replace("\t", " ");
    for (int i = 0; i < 10; i++) {
      target = target.replace("" + i, "");
    }
    return target;
  }

  private static void loadStopWordList() {
    try {
      br = new BufferedReader(new FileReader("stopwords"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      while (true) {
        String currentStopWord = br.readLine();
        if (currentStopWord == null) {
          break;
        }
        stopWords.add(currentStopWord);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
