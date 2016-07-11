package util;

import de.hfu.NewsArticle;
import de.hfu.NewsArticleRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chris on 21.06.2016.
 */
public class DateConverter {

  public static void main(String[] args) throws Exception {
    List<NewsArticle> news = NewsArticleRepository.findAll();
    for (NewsArticle newsArticle:
         news) {
//      if (newsArticle.getPubDate() == null && newsArticle.getDate()!=null){
//        newsArticle.setPubDate(convertDate(newsArticle.getDate()));
//        newsArticle.setDate(null);
//        NewsArticleRepository.save(newsArticle);
//      }
    }
  }

  public static Date convertDate(String target) throws Exception{
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
    Date pubDate = df.parse(target);
    return pubDate;
  }
}
