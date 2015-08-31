package reql.protocol

sealed abstract class ReqlProtocol(val value: Int)

object ReqlProtocol {
  case object PROTOBUF extends ReqlProtocol(656407617)
  case object JSON extends ReqlProtocol(2120839367)
}
