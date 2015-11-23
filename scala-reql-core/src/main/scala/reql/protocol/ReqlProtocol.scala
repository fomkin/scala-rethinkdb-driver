package reql.protocol

sealed abstract class ReqlProtocol(val value: Int)

//q9
object ReqlProtocol {

  case object ProtoBuf extends ReqlProtocol(656407617)

  case object Json extends ReqlProtocol(2120839367)
}
