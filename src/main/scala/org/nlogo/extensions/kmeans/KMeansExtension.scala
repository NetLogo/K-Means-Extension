package org.nlogo.extensions.kmeans

import scala.collection.JavaConverters._
import org.nlogo.api.AgentSet
import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultClassManager
import org.nlogo.api.DefaultReporter
import org.nlogo.api.ExtensionException
import org.nlogo.api.PrimitiveManager
import org.nlogo.api.ScalaConversions.toLogoList
import org.nlogo.api.Syntax._
import org.nlogo.api.Turtle
import edu.uci.ics.jung.algorithms.util.KMeansClusterer
import edu.uci.ics.jung.algorithms.util.KMeansClusterer.NotEnoughClustersException

class KMeansExtension extends DefaultClassManager {
  def load(primitiveManager: PrimitiveManager) {
    primitiveManager.addPrimitive("clusters", KMeansClustersPrim)
  }
}

object KMeansClustersPrim extends DefaultReporter {
  override def getSyntax = reporterSyntax(
    Array(AgentsetType, NumberType, NumberType, NumberType),
    ListType)
  override def report(args: Array[Argument], context: Context) =
    try {
      toLogoList(KMeans.clusters(
        args(0).getAgentSet, // turtles
        args(1).getIntValue, // nbClusters
        args(2).getIntValue, // maxIterations
        args(3).getDoubleValue, // convergenceThreshold
        context.getRNG))
    } catch {
      case e: java.lang.IllegalArgumentException => throw new ExtensionException(e)
      case e: NotEnoughClustersException         => throw new ExtensionException(e)
    }
}

object KMeans {
  def clusters(
    turtles: AgentSet,
    nbClusters: Int,
    maxIterations: Int,
    convergenceThreshold: Double,
    rng: java.util.Random): Seq[Seq[org.nlogo.api.Turtle]] = {
    if (turtles.count > 0) {
      object Clusterer extends KMeansClusterer[Turtle] {
        rand = rng
        setMaxIterations(maxIterations)
        setConvergenceThreshold(convergenceThreshold)
      }
      val locations =
        turtles.agents.asScala.collect {
          case t: Turtle => t -> Array(t.xcor, t.ycor)
        }.toMap.asJava
      Clusterer
        .cluster(locations, nbClusters)
        .asScala
        .map(_.keySet.asScala.toSeq)
        .toSeq
    } else Seq()
  }
}