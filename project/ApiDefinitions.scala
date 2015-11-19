// https://raw.githubusercontent.com/rethinkdb/rethinkdb/v2.1.1/src/rdb_protocol/ql2.proto
object ApiDefinitions {

  def genGroupModule(termType: Int, name: String) = {
    module(termType = termType, name = name)(Top.Function, Top.Datum.Obj)(
      fun(Top.Sequence)(multiarg("field", Top.Datum.Str)),
      fun(name + "_f", Top.Sequence)(multiarg("f", Top.FunctionArg(1)))
    )
  }

  val modules = Seq(

    // Takes an HTTP URL and gets it.  If the get succeeds and
    //  returns valid JSON, it is converted into a DATUM
    // HTTP = 153; // STRING {data: OBJECT | STRING,
    //         timeout: !NUMBER,
    //         method: STRING,
    //         params: OBJECT,
    //         header: OBJECT | ARRAY,
    //         attempts: NUMBER,
    //         redirects: NUMBER,
    //         verify: BOOL,
    //         page: FUNC | STRING,
    //         page_limit: NUMBER,
    //         auth: OBJECT,
    //         result_format: STRING,
    //         } -> STRING | STREAM

    //-------------------------------------------------------------------------
    //
    //  Math
    //
    //-------------------------------------------------------------------------

    // EQ  = 17; // DATUM... -> BOOL
    module(termType = 17, name = "eq")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum), arg("b", Top.Datum)),
      fun(Top.Datum)(arg("and", Top.Datum)),
      fun("===", Top.Datum)(arg("and", Top.Datum))
    ),
    // NE  = 18; // DATUM... -> BOOL
    module(termType = 18, name = "ne")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum), arg("b", Top.Datum)),
      fun(Top.Datum)(arg("and", Top.Datum)),
      fun("!===", Top.Datum)(arg("and", Top.Datum))
    ),

    //LT  = 19; // DATUM... -> BOOL
    module(termType = 19, name = "lt")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum), arg("b", Top.Datum)),
      fun(Top.Datum)(arg("thn", Top.Datum)),
      fun("<", Top.Datum)(arg("thn", Top.Datum))
    ),

    //GT  = 21; // DATUM... -> BOOL
    module(termType = 21, name = "gt")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum), arg("b", Top.Datum)),
      fun(Top.Datum)(arg("thn", Top.Datum)),
      fun(">", Top.Datum)(arg("thn", Top.Datum))
    ),

    // Executes its first argument, and returns its second argument if it
    // got [true] or its third argument if it got [false] (like an `if`
    // statement).
    //BRANCH  = 65; // BOOL, Top, Top -> Top
    module(termType = 65, name = "branch")(Top.AnyType)(
      fun(arg("condition", Top.Datum.Bool), arg("thn", Top), arg("els", Top))
    ),

    //OR = 66; // BOOL... -> BOOL
    module(termType = 66, name = "or")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum.Bool), arg("b", Top.Datum.Bool)),
      fun(Top.Datum.Bool)(arg("b", Top.Datum.Bool)),
      fun("||", Top.Datum)(arg("b", Top.Datum))
    ),

    //  AND     = 67; // BOOL... -> BOOL
    module(termType = 67, name = "and")(Top.Datum.Bool)(
      fun(arg("a", Top.Datum.Bool), arg("b", Top.Datum.Bool)),
      fun(Top.Datum.Bool)(arg("b", Top.Datum.Bool)),
      fun("&&", Top.Datum)(arg("b", Top.Datum))
    ),

    // SUB = 25; // NUMBER... -> NUMBER
    module(termType = 24, name = "add")(Top.Datum.Num)(
      fun(multiarg("values", Top.Datum.Num)),
      fun(Top.Datum.Num)(arg("value", Top.Datum.Num)),
      fun("+", Top.Datum.Num)(arg("value", Top.Datum.Num))
    ),

    // SUB = 25; // NUMBER... -> NUMBER
    module(termType = 25, name = "sub")(Top.Datum.Num)(
      fun(multiarg("values", Top.Datum.Num)),
      fun(Top.Datum.Num)(arg("value", Top.Datum.Num)),
      fun("-", Top.Datum.Num)(arg("value", Top.Datum.Num))
    )
  )


}