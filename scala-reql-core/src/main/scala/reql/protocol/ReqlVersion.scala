package reql.protocol

sealed abstract class ReqlVersion(val value: Int)

object ReqlVersion {
  case object V01 extends ReqlVersion(1063369270)
  case object V02 extends ReqlVersion(1915781601)
  case object V03 extends ReqlVersion(1601562686)
  case object V04 extends ReqlVersion(1074539808)
}
