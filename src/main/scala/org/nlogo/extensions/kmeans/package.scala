package org.nlogo.extensions

import scala.collection.JavaConverters._

import org.nlogo.agent
import org.nlogo.agent.Turtle
import org.nlogo.api

package object kmeans {
  def toTurtleSet(
    turtles: Traversable[api.Turtle],
    world: api.World): api.AgentSet = {
    val agents = turtles
      .collect { case t: agent.Turtle => t }
      .toArray[agent.Agent]
    new agent.ArrayAgentSet(
      classOf[agent.Turtle],
      agents,
      world.asInstanceOf[agent.World])
  }

  def toTurtleSets(
    turtleSeqs: Seq[Seq[api.Turtle]],
    world: api.World,
    rng: java.util.Random): Seq[api.AgentSet] = {
    val turtleSets: Seq[api.AgentSet] =
      turtleSeqs.map(toTurtleSet(_, world))(collection.breakOut)
    new scala.util.Random(rng).shuffle(turtleSets)
  }
}
