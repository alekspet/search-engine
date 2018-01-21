package search.server.storage

import akka.actor.{ActorSelection, ActorSystem}
import com.typesafe.config.Config
import search.protocol.{shardName, systemName}

import scala.collection.JavaConverters._

/**
  * Create storage nodes selection from configuration.
  **/
object ShardNodeFactory {


  def shardNodes(system: ActorSystem): Seq[ActorSelection] = urisFromConf(system.settings.config)
    .map(system.actorSelection)

  private def urisFromConf(config: Config): Seq[String] = config
    .getConfigList("storage.nodes")
    .asScala
    .map(toActorUri)

  private def toActorUri(config: Config): String =
    s"akka.tcp://$systemName@${config.getString("host")}:${config.getString("port")}/user/$shardName"
}
