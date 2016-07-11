package de.hfu;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chris on 18.06.2016.
 */
public class NewsFeedParser {


  private static String WELT = "http://www.welt.de/?service=Rss";
  private static String SPIEGEL = "http://www.spiegel.de/schlagzeilen/tops/index.rss";

  public static void main(String[] args) throws Exception {
    while (true) {
      parseFeeds();
      Thread.sleep(1000 * 60 * 10);
    }
  }

  public static void parseFeeds() throws Exception {
    String weltFeedString = getFeedAsString(WELT);
    String spiegelFeedString = getFeedAsString(SPIEGEL);
    List<NewsArticle> weltArticles = parseFeed(weltFeedString);
    List<NewsArticle> spiegelArticles = parseFeed(spiegelFeedString);
    List<NewsArticle> allArticles = combineNewsArticles(weltArticles, spiegelArticles);
    persistNewsArticles(allArticles);
    System.out.println(NewsArticleRepository.findAll().size());
  }

  private static void persistNewsArticles(List<NewsArticle> allArticles) {
    for (NewsArticle newsArticle :
        allArticles) {
      NewsArticleRepository.save(newsArticle);
    }
  }

  private static List<NewsArticle> combineNewsArticles(List<NewsArticle> weltArticles, List<NewsArticle> spiegelArticles) {
    List<NewsArticle> allArticles = new ArrayList<>();
    allArticles.addAll(weltArticles);
    allArticles.addAll(spiegelArticles);
    return allArticles;
  }


  public static String getFeedAsString(String URL) throws Exception {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(URL);
    CloseableHttpResponse response1 = httpclient.execute(httpGet);
    HttpEntity entity = null;
    try {
      System.out.println(response1.getStatusLine());
      entity = response1.getEntity();
      return EntityUtils.toString(entity);
    } finally {
      EntityUtils.consume(entity);
      response1.close();
    }
  }

  public static List<NewsArticle> parseFeed(String feed) throws Exception {
    List<NewsArticle> newsArticles = new ArrayList<>();
    try {
      Document doc = loadXMLFromString(feed);
      doc.getDocumentElement().normalize();
      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      NodeList nList = doc.getElementsByTagName("item");
      System.out.println("----------------------------");
      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node nNode = nList.item(temp);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
          Element eElement = (Element) nNode;
          NewsArticle newsArticle = new NewsArticle();
          newsArticle.setId(eElement.getElementsByTagName("guid").item(0).getTextContent());
          newsArticle.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
          newsArticle.setTitle(eElement.getElementsByTagName("title").item(0).getTextContent());
          newsArticle.setLink(eElement.getElementsByTagName("link").item(0).getTextContent());
          newsArticle.setPubDate(convertDate(eElement.getElementsByTagName("pubDate").item(0).getTextContent()));
          newsArticles.add(newsArticle);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return newsArticles;
  }

  public static Document loadXMLFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
  }

  public static Date convertDate(String target) throws Exception{
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
    Date pubDate = df.parse(target);
    return pubDate;
  }
}
