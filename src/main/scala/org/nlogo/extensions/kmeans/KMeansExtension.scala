package org.nlogo.extensions.kmeans

import scala.collection.JavaConverters._

import org.nlogo.api.AgentSet
import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultClassManager
import org.nlogo.api.DefaultReporter
import org.nlogo.api.ExtensionException
import org.nlogo.api.LogoList
import org.nlogo.api.PrimitiveManager
import org.nlogo.api.ScalaConversions.toLogoList
import org.nlogo.api.Syntax._
import org.nlogo.api.Turtle
import org.nlogo.api.World
import org.nlogo.util.MersenneTwisterFast

import edu.uci.ics.jung.algorithms.util.KMeansClusterer

class KMeansExtension extends DefaultClassManager {
  def load(primitiveManager: PrimitiveManager) {
    primitiveManager.addPrimitive("cluster-by-xy", KMeansClustersPrim)
  }
}

object KMeansClustersPrim extends DefaultReporter {
  override def getSyntax = reporterSyntax(
    Array(AgentsetType, NumberType, NumberType, NumberType),
    ListType)
  override def report(args: Array[Argument], context: Context) = {
    val agentSet = args(0).getAgentSet
    if (!classOf[Turtle].isAssignableFrom(agentSet.`type`))
      throw new ExtensionException("Expected input to be a turtle set.")
    try {
      KMeans.clusters(
        agentSet, // turtles
        args(1).getIntValue, // nbClusters
        args(2).getIntValue, // maxIterations
        args(3).getDoubleValue, // convergenceThreshold
        context.getRNG,
        context.getAgent.world)
    } catch {
      case e: IllegalArgumentException => throw new ExtensionException(e.getMessage)
    }
  }
}

object KMeans {
  def clusters(
    agentSet: AgentSet,
    nbClusters: Int,
    maxIterations: Int,
    convergenceThreshold: Double,
    rng: MersenneTwisterFast,
    world: World): LogoList = {

    val locations = agentSet.agents.asScala
      .collect { case t: Turtle => t -> Array(t.xcor, t.ycor) }
      .toMap
    val nbTurtles = agentSet.count
    lazy val nbDistinctLocations = locations.values.map(_.deep).toSet.size

    // Jung's clusterer is a lot pickier than I think we should be, so
    // we handle the edge cases ourselves and only defer to the actual
    // clusterer when we are sure it's in a position to do its job.
    val cs: Seq[AgentSet] = nbClusters match {

      // Not enough turtles:
      case n if n > agentSet.count => throw new IllegalArgumentException(
        "Not enough turtles to form the requested number of clusters.")

      // zero clusters results in empty list:
      case 0 => Seq()

      // only one cluster requires no clustering:
      case 1 => Seq(agentSet)

      // Not enough distinct turtles:
      case n if n > nbDistinctLocations => throw new IllegalArgumentException(
        "Not enough turtles at distinct locations to form the requested number of clusters.")

      // Same of number of (verified distinct) turtles as clusters:
      case n if n == nbTurtles => splitAgentSet(agentSet, rng, world)

      // every other case handled by the actual clusterer:
      case _ =>
        object Clusterer extends KMeansClusterer[Turtle] {
          rand = rng
          setMaxIterations(maxIterations)
          setConvergenceThreshold(convergenceThreshold)
        }
        Clusterer
          .cluster(locations.asJava, nbClusters)
          .asScala
          .map(c => toTurtleSet(c.keySet.asScala.toSeq, world))
          .toSeq
    }
    toLogoList(cs)
  }
}
