package de.hfu;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Chris on 18.06.2016.
 */
public class NewsArticleRepository {

  private static Datastore ds;

  static {
    ds = new Morphia().createDatastore(new MongoClient(new ServerAddress("localhost")),
        "NewsArticles");
  }


  public static NewsArticle findById(String id) {
    return ds.find(NewsArticle.class).field("_id").equal(id).get();
  }

  public static boolean deleteByID(String id) {
    ds.delete(NewsArticle.class, id);
    if (ds.find(NewsArticle.class).field("_id").equal(id).get() == null) {
      return true;
    } else {
      return false;
    }
  }

  public static List<NewsArticle> findAll() {
    List<NewsArticle> all = ds.find(NewsArticle.class).asList();
    return all;
  }

  public static List<NewsArticle> findFromTo(String from, String to, Integer minimum) {
    try {
      Date fromDate = convertDate(from);
      Date toDate = convertDate(to);
      System.out.println(fromDate);
      System.out.println(toDate);
      List<NewsArticle> all = ds.find(NewsArticle.class).asList();
      Set<NewsArticle> filtered = new HashSet<NewsArticle>();
      for (NewsArticle x : all) {
        if (x.getPubDate().after(fromDate) && x.getPubDate().before(toDate)) {
          filtered.add(x);
        }
      }
      if (minimum != null) {
        while (filtered.size() < minimum) {
          filtered.add(all.get(new Random().nextInt(all.size())));
        }
      }
      ArrayList<NewsArticle> listFiltered = new ArrayList<NewsArticle>();
      listFiltered.addAll(filtered);
      return listFiltered;
    } catch (Exception e) {
      return null;
    }
  }


  public static Date convertDate(String target) throws Exception {
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    Date pubDate = df.parse(target);
    return pubDate;
  }

  public static void saveAll(Map<String, NewsArticle> NewsArticles) {
    for (Map.Entry<String, NewsArticle> entry : NewsArticles.entrySet()) {
      save(entry.getValue());
    }
  }

  public static void save(Object entity) {
    ds.save(entity);
  }

  public static void dropCollection() {
    ds.delete(ds.createQuery(NewsArticle.class));
  }


  public static void main(String[] args) throws Exception {
    Gson gson = new Gson();
    writeToFile(gson);
    readFromFile(gson);
  }

  public static void readFromFile(Gson gson) throws IOException {
    List<NewsArticle> articles = new ArrayList<NewsArticle>();
    try (BufferedReader br = new BufferedReader(new FileReader("articles.txt"))) {
      for (String line; (line = br.readLine()) != null; ) {
        articles.add(gson.fromJson(line, NewsArticle.class));
      }
    }
    for (NewsArticle newsArticle : articles) {
      save(newsArticle);
    }
  }

  public static void writeToFile(Gson gson) throws IOException {
    List<NewsArticle> newsArticles = findAll();
    FileWriter fw = new FileWriter("articles.txt");
    for (NewsArticle newsArticle : newsArticles) {
      // 2. Java object to JSON, and assign to a String
      String jsonInString = gson.toJson(newsArticle);
      fw.write(jsonInString);
      fw.write("\n");
      // 2. JSON to Java object, read it from a Json String.
    }
    fw.close();
  }

}
