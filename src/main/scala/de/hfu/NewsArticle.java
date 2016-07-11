package de.hfu;

import com.google.gson.Gson;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.converters.DoubleConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 18.06.2016.
 */
@Entity("NewsArticle")
public class NewsArticle implements Serializable {

  @Id
  private String id;
  private String title;
  private String description;
  private String link;
  private Date pubDate;
  private Integer cluster;
  private String[] clusterTopics;
  private List<Double> vectorAngles;
  private Double clusterAngle;


  public NewsArticle(){
    //default
  }

  public NewsArticle(Integer cluster, Double clusterAngle, NewsArticle newsArticle, String[] clusterTopics) {
    this.id = newsArticle.id;
    this.title = newsArticle.title;
    this.description = newsArticle.description;
    this.link = newsArticle.link;
    this.cluster = cluster;
    this.clusterAngle = clusterAngle;
    this.clusterTopics = clusterTopics;
  }

  public NewsArticle(NewsArticle newsArticle, List<Double> vectorAngles) {
    this.id = newsArticle.id;
    this.title = newsArticle.title;
    this.description = newsArticle.description;
    this.link = newsArticle.link;
    this.cluster = newsArticle.cluster;
    this.clusterAngle = newsArticle.clusterAngle;
    this.clusterTopics = newsArticle.clusterTopics;
    this.vectorAngles = vectorAngles;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  public Integer getCluster() {
    return cluster;
  }

  public void setCluster(Integer cluster) {
    this.cluster = cluster;
  }

  public String[] getClusterTopics() {
    return clusterTopics;
  }

  public void setClusterTopics(String[] clusterTopics) {
    this.clusterTopics = clusterTopics;
  }

  public List<Double> getVectorAngles() {
    return vectorAngles;
  }

  public void setVectorAngles(List<Double> vectorAngles) {
    this.vectorAngles = vectorAngles;
  }

  public Double getClusterAngle() {
    return clusterAngle;
  }

  public void setClusterAngle(Double clusterAngle) {
    this.clusterAngle = clusterAngle;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
