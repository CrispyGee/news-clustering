package de.hfu

import com.google.gson.Gson

/**
  * Created by chris on 07.06.2016.
  */
object Main {


  def main(args: Array[String]) {
    if (args.length > 0) {
      if (args.apply(0).equals("parsefeed")) {
        NewsFeedParser.parseFeeds
      }
      else if (args.apply(0).equals("parsefile")) {
        NewsArticleRepository.readFromFile(new Gson())
      }
      else if (args.length == 4) {
        ClusterBuilder.start(args.apply(0), args.apply(1), Integer.parseInt(args.apply(2)), Integer.parseInt(args.apply(3)))
      }
      else if (args.length == 3) {
        ClusterBuilder.start(args.apply(0), args.apply(1), Integer.parseInt(args.apply(2)), null)
      }
    }
    if (args.length == 0) {
      throw new Exception("please insert console arguments, e.g.: \"01.07.2016 07.07.2016 2 20\"\n" +
        "i.e. news from the given time span with 2 clusters and minimum of 20 articles")
    }
  }


}
