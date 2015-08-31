package reql.dsl

import pushka.Ast

trait TypesImplicits {

  implicit def toStr(value: String): types.Str = new types.Str {
    def ast: Ast = Ast.Str(value)
  }
  implicit def toNum(value: Int): types.Num = new types.Num {
    def ast: Ast = Ast.Num(value)
  }

  implicit def toNum(value: Float): types.Num = new types.Num {
    def ast: Ast = Ast.Num(value)
  }

  implicit def toNum(value: Double): types.Num = new types.Num {
    def ast: Ast = Ast.Num(value)
  }

  implicit def toBool(value: Boolean): types.Bool = new types.Bool {
    def ast: Ast = if (value) Ast.True else Ast.False
  }
}
