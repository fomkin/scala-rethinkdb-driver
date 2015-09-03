package reql.dsl

/**
 * Definition of reql types of data.
 */
object types {

  trait Top extends ReqlArg

  trait Datum extends Top

  trait Sequence extends Top with CursorResultQuery

  trait Arr extends Datum with Sequence

  trait Database extends Top

  trait Function extends Top

  trait Ordering extends Top

  trait Pathspec extends Top

  trait Error extends Top

  trait Null extends Datum  with AtomResultQuery

  trait Bool extends Datum with AtomResultQuery

  trait Num extends Datum with AtomResultQuery

  trait Str extends Datum with AtomResultQuery

  trait Obj extends Datum with AtomResultQuery

  trait SingleSelection extends Datum with Obj with AtomResultQuery

  trait Stream extends Sequence

  trait StreamSelection extends Stream

  trait Table extends StreamSelection

  // Pseudo types

  trait Time extends Top

  trait Binary extends Top
  
  // Query types

  trait CursorResultQuery extends ReqlArg

  trait AtomResultQuery extends ReqlArg
  
  // Var
  
  object Var {
    def apply(n: Int): Var = new Var {
      val json = s"[10, [$n]]"
    }
  }
  
  trait Var extends ReqlArg with Sequence with Database 
  with Function with Null with Num with Str
  with Bool with Time with Ordering

  
  def extractJson(from: ReqlArg): String = from.json

  def extractJson(from: (Var) ⇒ Function): String = {
    val body = from(Var(1)) 
    s"[69, [[2, [1]], ${body.json}]]"
  }

  def extractJson(from: (Var, Var) ⇒ Function): String = {
    val body = from(Var(1), Var(2))
    s"[69, [[2, [1, 2]], ${body.json}]]"
  }

  def extractJson(from: (Var, Var, Var) ⇒ Function): String = {
    val body = from(Var(1), Var(2), Var(3))
    s"[69, [[2, [1, 2, 3]], ${body.json}]]"
  }

}
