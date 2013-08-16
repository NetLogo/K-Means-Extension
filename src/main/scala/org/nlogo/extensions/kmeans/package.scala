package org.nlogo.extensions

import org.nlogo.agent
import org.nlogo.agent.Turtle
import org.nlogo.api
import org.nlogo.util.MersenneTwisterFast

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

  def splitAgentSet(
    agentSet: api.AgentSet,
    rng: MersenneTwisterFast,
    world: api.World): Seq[api.AgentSet] = {
    val xs = Seq.newBuilder[api.AgentSet]
    val it = agentSet.asInstanceOf[agent.AgentSet].shufflerator(rng)
    val t = classOf[agent.Turtle]
    val w = world.asInstanceOf[agent.World]
    while (it.hasNext)
      xs += new agent.ArrayAgentSet(t, Array(it.next), w)
    xs.result
  }
}
