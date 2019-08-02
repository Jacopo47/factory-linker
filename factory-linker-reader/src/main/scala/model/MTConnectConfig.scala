package model

case class MTConnectConfig(endpoint: String, machineName: String, relativePath: String, namespace: Namespace,nodes: Seq[MTConnectConfigNode])
case class MTConnectConfigNode(name: String, xpath: String)
case class Namespace(prefix: String, uri: String)