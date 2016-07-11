package de.hfu

/**
  * Created by chris on 07.06.2016.
  */

import java.io.{File, PrintWriter}
import java.util

import org.apache.spark.mllib.clustering._
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.{Vector, DenseVector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConverters._


import scala.collection.mutable

object ClusterBuilder {

  val conf = new SparkConf().setAppName("test").setMaster("local[4]")
  val sc = new SparkContext(conf)
  var w2model: Word2VecModel = null
  var numClusters = 4
  val numSynonyms = 3
  var word2vecmode = true
  var fromDate: String = null
  var toDate: String = null
  var path: String = null
  var kmeancenters: Array[Vector] = null
  var bisectingcenters: Array[Vector] = null

  def start(from: String, to: String, clustersAmount: Int, minimum: Integer): Unit = {
    fromDate = from
    toDate = to
    numClusters = clustersAmount
    path = from + "-" + to + "-clusters-" + numClusters
    new File(path).mkdir()
    val newsArticles = sc.parallelize(NewsArticleRepository.findFromTo(from, to, minimum).asScala)
    if (newsArticles.count() < 1) {
      throw new Exception("no news articles in this time span found")
    }
    TfIdfClusters(newsArticles)
    Word2VecClusters(newsArticles)
  }

  def TfIdfClusters(newsArticles: RDD[NewsArticle]): Unit = {
    val input: RDD[Vector] = calculateTfIdf(newsArticles)
    val kMeansClusteredArticles = applyKMeansClustering(newsArticles zip input)
    for (i <- 0 until numClusters) {
      logClusterResult(kMeansClusteredArticles.filter(x => x.getCluster.equals(i)), "k-meansTfIdf", i)
      logClusterVectorResult(kMeansClusteredArticles.filter(x => x.getCluster.equals(i)), "k-meansTfIdf", i)
    }
    val bisectingKMeansClusteredArticles = applyBisectingKMeansClustering(newsArticles zip input)
    for (i <- 0 until numClusters) {
      logClusterResult(bisectingKMeansClusteredArticles.filter(x => x.getCluster.equals(i)), "bisectingTfIdf", i)
      logClusterVectorResult(bisectingKMeansClusteredArticles.filter(x => x.getCluster.equals(i)), "bisectingTfIdf", i)
    }
  }

  def Word2VecClusters(newsArticles: RDD[NewsArticle]): Unit = {
    val input2: RDD[(NewsArticle, Vector)] = calculateWord2Vec(newsArticles)
    val kMeansClusteredArticles2 = applyKMeansClustering(input2)
    for (i <- 0 until numClusters) {
      logClusterResult(kMeansClusteredArticles2.filter(x => x.getCluster.equals(i)), "k-meansWord2Vec", i)
      logClusterVectorResult(kMeansClusteredArticles2.filter(x => x.getCluster.equals(i)), "k-meansWord2Vec", i)
    }
    val bisectingKMeansClusteredArticles2 = applyBisectingKMeansClustering(input2)
    for (i <- 0 until numClusters) {
      logClusterResult(bisectingKMeansClusteredArticles2.filter(x => x.getCluster.equals(i)), "bisectingWord2Vec", i)
      logClusterVectorResult(bisectingKMeansClusteredArticles2.filter(x => x.getCluster.equals(i)), "bisectingWord2Vec", i)
    }
  }

  def logClusterResult(clusteredArticles: RDD[NewsArticle], algorithm: String, clusterNum: Int): Unit = {
    val pw = new PrintWriter(new File(path + "/clusterAnalysis-" + clusterNum + "-" + algorithm + ".txt"))
    clusteredArticles.collect().foreach(x => pw.write(x.getClusterAngle + "\n"))
    pw.close
  }

  def logClusterVectorResult(clusteredArticles: RDD[NewsArticle], algorithm: String, clusterNum: Int): Unit = {
    val pw = new PrintWriter(new File(path + "/vectorAnalysis-" + clusterNum + "-" + algorithm + ".txt"))
    clusteredArticles.collect().foreach({ x =>
      val currentAngles = x.getVectorAngles.asScala.mkString(" ")
      pw.write(x.getClusterAngle + " " + currentAngles +"\n")
    })
    pw.close
  }

  def applyKMeansClustering(tupels: RDD[(NewsArticle, Vector)]): RDD[NewsArticle] = {
    val numIterations = 25
    val model = KMeans.train(tupels.map(x => x._2), numClusters, numIterations)
    kmeancenters = model.clusterCenters
    val cluster_centers = sc.parallelize(model.clusterCenters.zipWithIndex.map { e => (e._2, e._1) })
    //    if (word2vecmode) var cluster_topics = cluster_centers.mapValues(x => w2model.findSynonyms(x, numSynonyms).map(x => x._1)).collect()
    val filteredTuples = tupels.filter(x => x._2.numNonzeros > 1)
    val predictedTuples = filteredTuples.map(x => {
      val featVec = x._2
      val prediction = model.predict(featVec)
      val centerAngle = calculateAngle(x._2, model.clusterCenters.apply(prediction))
      (new NewsArticle(prediction, centerAngle, x._1, null), featVec)
    })
    return calcAllVectors(predictedTuples)
  }

  def calcAllVectors(predictedTuples: RDD[(NewsArticle, Vector)]): RDD[NewsArticle] = {
    var vectorsPerCluster = scala.collection.mutable.ArrayBuffer.empty[Array[Vector]]
    for (i <- 0 until numClusters) {
      val currentArray: Array[Vector] = predictedTuples.filter(y => y._1.getCluster.equals(i)).map(x => x._2).collect()
      vectorsPerCluster += currentArray
    }
    return predictedTuples.map(x =>
      new NewsArticle(x._1, calculateMultipleAngles(x._2, vectorsPerCluster(x._1.getCluster))))
  }

  def applyBisectingKMeansClustering(tupels: RDD[(NewsArticle, Vector)]): RDD[NewsArticle] = {
    val bkm = new BisectingKMeans().setK(numClusters)
    val model = bkm.run(tupels.map(x => x._2))
    bisectingcenters = model.clusterCenters
    val cluster_centers = sc.parallelize(model.clusterCenters.zipWithIndex.map { e => (e._2, e._1) })
    //    if (word2vecmode) var cluster_topics = cluster_centers.mapValues(x => w2model.findSynonyms(x, numSynonyms).map(x => x._1)).collect()
    val filteredTuples = tupels.filter(x => x._2.numNonzeros > 1)
    val predictedTuples = filteredTuples.map(x => {
      val featVec = x._2
      val prediction = model.predict(featVec)
      val centerAngle = calculateAngle(x._2, model.clusterCenters.apply(prediction))
      (new NewsArticle(prediction, centerAngle, x._1, null), featVec)
    })
    return calcAllVectors(predictedTuples)
  }


  def calculateMultipleAngles(vec1: Vector, vecsToCompare: Array[Vector]): java.util.List[java.lang.Double] = {
    val list = new java.util.ArrayList[java.lang.Double]()
    for (i <- 0 until vecsToCompare.size) {
      list.add(calculateAngle(vec1, vecsToCompare(i)))
    }
    return list
  }

  def calculateAngle(vec1: Vector, vec2: Vector): Double = {
    var v1xv2: Double = 0.0;
    var lengthV1: Double = 0.0;
    var lengthV2: Double = 0.0;
    for (i <- 0 until vec1.size - 1) {
      val x1 = vec1.apply(i)
      val x2 = vec2.apply(i)
      v1xv2 = v1xv2 + x1 * x2
      lengthV1 = lengthV1 + scala.math.pow(x1, 2)
      lengthV2 = lengthV2 + scala.math.pow(x2, 2)
    }
    lengthV1 = scala.math.sqrt(lengthV1)
    lengthV2 = scala.math.sqrt(lengthV1)
    return v1xv2 / (lengthV1 * lengthV2)
  }

  def sumUpVectors(vec1: Vector, vec2: Vector): Vector = {
    val matrix = vec1.toDense.toArray zip vec2.toDense.toArray
    var sum = new DenseVector(matrix.map(x => x._1 + x._2))
    return sum
  }

  def logClusterData(clusterVectors: Array[Vector], clusterModel: String): Unit = {
    val pw = new PrintWriter(new File(path + "/" + "-clustercenters-" + (if (word2vecmode) "w2v-" else "tfidf-") + clusterModel + "-" + fromDate + "-" + toDate + ".txt"))
    for (i <- 0 until clusterVectors.size) {
      for (j <- i + 1 until clusterVectors.size) {
        pw.write("Distance between cluster " + i + " and " + j + " = " + Vectors.sqdist(clusterVectors(i), clusterVectors(j)) + "\n")
        pw.write("Angle between cluster " + i + " and " + j + " = " + calculateAngle(clusterVectors(i), clusterVectors(j)) + "\n\n")
      }
    }
    pw.close()
  }

  def calculateTfIdf(newsArticles: RDD[NewsArticle]): RDD[Vector] = {
    word2vecmode = false
    val documents = newsArticles.map(x => PreProcessor.tokenizeAndRemoveStopwords(x.getTitle).asScala ++ PreProcessor.tokenizeAndRemoveStopwords(x.getDescription).asScala)
    val hashingTF = new HashingTF()
    val tf: RDD[Vector] = hashingTF.transform(documents)
    val idf = new IDF().fit(tf)
    val tfidf: RDD[Vector] = idf.transform(tf)
    return tfidf
  }

  def calculateWord2Vec(newsArticles: RDD[NewsArticle]): RDD[(NewsArticle, Vector)] = {
    word2vecmode = true;
    val documents = sc.parallelize(NewsArticleRepository.findAll().asScala)
      .map(x => PreProcessor.tokenizeAndRemoveStopwords(x.getTitle).asScala ++ PreProcessor.tokenizeAndRemoveStopwords(x.getDescription).asScala)
    val word2vec = new Word2Vec()
    w2model = word2vec.fit(documents)
    val titleVectorPairs = newsArticles.map(x => (x, simpleDoc2Vec(PreProcessor.tokenizeAndRemoveStopwords(x.getTitle).asScala)))
    return titleVectorPairs
  }

  def simpleDoc2Vec(title: mutable.Buffer[String]): Vector = {
    return divVector(title.map(f => wordToVector(f)).reduce((A1, A2) => sumUpVectors(A1, A2)), title.size)
  }

  def wordToVectorWithFactor(f: String): Vector = {
    val w2Vector = wordToVector(f)
    val factoredVector = new DenseVector(w2Vector.toArray.map(x => x))
    return factoredVector
  }

  def divVector(m: Vector, divisor: Double): Vector = {
    return new DenseVector(m.toArray.map(x => x / divisor))
  }

  def wordToVector(w: String): Vector = {
    try {
      return w2model.transform(w)
    } catch {
      case e: Exception => return Vectors.zeros(100)
    }
  }

}
