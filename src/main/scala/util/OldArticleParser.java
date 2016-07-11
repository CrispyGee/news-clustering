package util;

import de.hfu.NewsArticle;
import de.hfu.NewsArticleRepository;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Chris on 21.06.2016.
 */
public class OldArticleParser {

  private static BufferedReader br;


  public static void main(String[] args) throws Exception {
    try {
      br = new BufferedReader(new FileReader("src/main/resources/OldArticles.txt"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    br.readLine();
    int i = 0;
    while (true) {
      NewsArticle newsArticle = new NewsArticle();
      i++;
      String currentLine = br.readLine();
      if (currentLine == null) {
        break;
      }
      currentLine += br.readLine();
        System.out.println(i);
      String id = currentLine.split("'id")[1].split("'")[1];
      String title = currentLine.split("title")[1].split("'")[1];
      String desc = currentLine.split("description")[1].split("'")[1];
      String link = currentLine.split("link")[1].split("'")[1];
      String pubDate = currentLine.split("pubDate")[1];
      pubDate = pubDate.substring(1, pubDate.length()-1);
      newsArticle.setId(id);
      newsArticle.setTitle(title);
      newsArticle.setDescription(desc);
      newsArticle.setLink(link);
      newsArticle.setPubDate(convertDate(pubDate));
      System.out.println(newsArticle);
      NewsArticleRepository.save(newsArticle);
      br.readLine();
    }
  }

  public static Date convertDate(String target) throws Exception{
    DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
    Date pubDate = df.parse(target);
    return pubDate;
  }

}
