package reql.dsl

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait NestedReqlContext[Data] extends ReqlContext[Data] {
  
  import ReqlContext._
  
  def context: ReqlContext[Data]
  
  def parseResponse(data: Array[Byte]): ParsedResponse = {
    context.parseResponse(data)
  }

  def runCursorQuery[U](query: ReqlArg)(f: CursorCb[Data]): Unit = {
    context.runCursorQuery(query)(f)
  }

  def runAtomQuery[U](query: ReqlArg)(f: AtomCb[Data]): Unit = {
    context.runAtomQuery(query)(f)
  }
}
