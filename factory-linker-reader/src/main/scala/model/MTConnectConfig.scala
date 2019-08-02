package model

case class MTConnectConfig(endpoint: String, machineName: String, relativePath: String, nodes: Seq[MTConnectConfigNode])
case class MTConnectConfigNode(name: String, xpath: String)