// https://raw.githubusercontent.com/rethinkdb/rethinkdb/v2.1.1/src/rdb_protocol/ql2.proto
object ApiDefinitions {

  def genGroupModule(termType: Int, name: String) = {
    module(termType = termType, name = name)(Top.Function, Top.Datum.Obj)(
      fun(Top.Sequence)(multiarg("field", Top.Datum.Str)),
      fun(name + "_f", Top.Sequence)(multiarg("f", Top.FunctionArg(1)))
    )
  }

  val modules = Seq(

    module(termType = 12, name = "error")(Top.Error)(
      fun(arg("message", Top.Datum.Str))
    ),
    module(termType = 169, name = "uuid")(Top.Datum)(fun()),
    module(termType = 13, name = "implicitVar")(Top.Datum)(fun()),
    // TODO hadcode it
    module(termType = 154, name = "http")(Top.Datum.Obj, Top.Datum.Str)(
      fun(arg("url", Top.Datum.Str))
    ),

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
    ),

    module(termType = 57, name = "dbCreate")(Top.Datum.Obj)(
      fun(arg("name", Top.Datum.Str))
    ),

    // DB_LIST       = 59; // -> ARRAY
    module(termType = 59, name = "dbList")(Top.Datum.Arr)(fun()),

    // * Data Operators
    // Returns a reference to a database.
    // STRING -> Database
    module(termType = 14, name = "db", doc =
      """
        |Reference a database.
        |Example: Explicitly specify a database for a query.
        |{{{
        |r.db('heroes').table('marvel').run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Database)(
        fun(arg("name", Top.Datum.Str))
      ),

    // TABLE = 15;
    // Database, STRING, {read_mode:STRING, identifier_format:STRING} -> Table
    // STRING, {read_mode:STRING, identifier_format:STRING} -> Table
    module(termType = 15, name = "table", doc =
      """
        |Select all documents in a table. This command can be chained with other commands to do further processing on the data.
        |Example: Return all documents in the table 'marvel' of the default database.
        |{{{
        |r.table('marvel').run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Sequence.Table)(
        fun(Top.Database)(
          arg("name", Top.Datum.Str),
          opt("read_mode", Top.Datum.Str),
          opt("identifier_format", Top.Datum.Str)
        )
      ),

    /*
      TABLE_CREATE  = 60; // Database, STRING, {primary_key:STRING, shards:NUMBER, replicas:NUMBER, primary_replica_tag:STRING} -> OBJECT
                          // Database, STRING, {primary_key:STRING, shards:NUMBER, replicas:OBJECT, primary_replica_tag:STRING} -> OBJECT
                          // STRING, {primary_key:STRING, shards:NUMBER, replicas:NUMBER, primary_replica_tag:STRING} -> OBJECT
                          // STRING, {primary_key:STRING, shards:NUMBER, replicas:OBJECT, primary_replica_tag:STRING} -> OBJECT

     */
    module(termType = 60, name = "tableCreate")(Top.Datum.Obj)(
      fun(Top.Database)(
        arg("name", Top.Datum.Str),
        opt("primary_key", Top.Datum.Str),
        opt("shards", Top.Datum.Num),
        opt("replicas", Top.Datum),
        opt("primary_replica_tag", Top.Datum.Str)
      )
    ),

    // INDEX_CREATE = 75;
    // Table, STRING, Function(1), {multi:BOOL} -> OBJECT
    module(termType = 75, name = "indexCreate")(Top.Datum.Obj)(
      fun(Top.Sequence.Table)(
        arg("name", Top.Datum.Str),
        opt("multi", Top.Datum.Bool),
        opt("geo", Top.Datum.Bool)
      ),
      fun(Top.Sequence.Table)(
        arg("name", Top.Datum.Str),
        arg("f", Top.FunctionArg(1)),
        opt("multi", Top.Datum.Bool),
        opt("geo", Top.Datum.Bool)
      )
    ),

    // INDEX_WAIT = 140; // Table, STRING... -> ARRAY
    module(termType = 140, name = "indexWait")(Top.Datum.Arr)(
      fun(Top.Sequence.Table)(multiarg("indexies", Top.Datum.Str))
    ),

    module(termType = 77, name = "indexList")(Top.Datum.Arr)(
      fun(Top.Sequence.Table)()
    ),

    module(termType = 62, name = "tableList")(Top.Datum.Arr)(
      fun(Top.Database)()
    ),

    // Gets a single element from a table by its primary or a secondary key.
    // Table, STRING -> SingleSelection | Table, NUMBER -> SingleSelection |
    // Table, STRING -> NULL            | Table, NUMBER -> NULL |
    module(termType = 16, name = "get")(Top.Datum.SingleSelection)(
      fun(Top.Sequence.Table)(arg("key", Top.Datum))
    ),

//    module(termType = 78, name = "getAll")(Top.Datum.Arr)(
//      fun(Top.Sequence.Table)(multiarg("keys", Top.Datum), opt("index", Top.Datum.Str))
//    ),

    // Sequence -> BOOL
    module(termType = 86, name = "isEmpty")(Top.Datum.Bool)(
      fun(Top.Sequence)()
    ),

    // GET_FIELD  = 31; // OBJECT, STRING -> DATUM
    // | Sequence, STRING -> Sequence
    module(termType = 31, name = "getField")(Top.AnyType)(
      fun(Top.Datum.Obj)(arg("field_name", Top.Datum.Str))
    ),

    // Check whether an object contains all the specified fields,
    // or filters a sequence so that all objects inside of it
    // contain all the specified fields.
    //HAS_FIELDS = 32; // OBJECT, Pathspec... -> BOOL
    module(termType = 32, name = "hasFields")(Top.Datum.Bool)(
      fun(Top.Datum.Obj)(multiarg("fields", Top.Datum.Str))
    ),

    // Calls a function on data
    //FUNCALL  = 64; // Function(*), DATUM... -> DATUM
    module(termType = 64, name = "applyFunction")(Top.Datum)(
      fun(arg("f", Top.FunctionArg(1)), arg("value", Top.Datum))
    ),

    //PLUCK    = 33; // Sequence, Pathspec... -> Sequence | OBJECT, Pathspec... -> OBJECT
    module(termType = 33, name = "pluck")(Top.AnyType)(
      fun(Top.Sequence)(multiarg("cols", Top.Datum.Str)),
      fun(Top.Datum.Obj)(multiarg("cols", Top.Datum.Str))
    ),

    module(termType = 38, name = "map", doc =
      """
        |Transform each element of one or more sequences by applying a mapping function to them. If map is run with two or more sequences, it will iterate for as many items as there are in the shortest sequence.
        |Example: Return the first five squares.
        |{{{
        |r.expr([1, 2, 3, 4, 5]).map(function (val) {
        |    return val.mul(val);
        |}).run(conn, callback);
        |}}}
        |// Result passed to callback
        |[1, 4, 9, 16, 25]
      """.stripMargin
    )(Top.Sequence)(
      fun(Top.Sequence)(arg("f", Top.FunctionArg(1)))
    ),

    //WITHOUT  = 34; // Sequence, Pathspec... -> Sequence | OBJECT, Pathspec... -> OBJECT
    module(termType = 34, name = "without", doc =
      """Get a subset of an object by selecting some attributes to discard, or
        |map that over a sequence.  (Both unpick and without, polymorphic.)
      """.stripMargin)(Top.AnyType)(
      fun(Top.Datum)(multiarg("objects", Top.Datum.Str))
    ),

    //MERGE    = 35; // OBJECT... -> OBJECT | Sequence -> Sequence
    module(termType = 35, name = "merge")(Top.AnyType)(
      fun(Top.Datum)(multiarg("objects", Top.Datum)),
      fun(Top.Datum)(arg("f",  Top.FunctionArg(1)))
    ),

    module(termType = 39, name = "filter", doc =
      """Filter a sequence with either a function or a shortcut
        |object (see API docs for details).  The body of FILTER is
        |wrapped in an implicit `.default(false)`, and you can
        |change the default value by specifying the `default`
        |optarg.  If you make the default `r.error`, all errors
        |caught by `default` will be rethrown as if the `default`
        |did not exist.
      """.stripMargin
    )(Top.Sequence)(
      // Sequence, Function(1), {default:DATUM} -> Sequence |
      // Sequence, OBJECT, {default:DATUM} -> Sequence
      fun(Top.Sequence)(arg("f", Top.FunctionArg(1)), opt("default", Top.Datum)),
      fun(Top.Sequence)(arg("x", Top.Datum), opt("default", Top.Datum))
    ),

    //BRACKET = 170; // Sequence | OBJECT, NUMBER | STRING -> DATUM
    module(termType = 170, name = "apply")(Top.AnyType)(
      fun(Top.Datum)(arg("field", Top.Datum.Str)),
      fun(Top.Datum)(arg("i", Top.Datum.Num))
    ),

    // LIMIT = 71; // Sequence, NUMBER -> Sequence
    module(termType = 71, name = "limit")(Top.Sequence)(
      fun(Top.Sequence)(arg("count", Top.Datum.Num))
    ),

    // SKIP  = 70; // Sequence, NUMBER -> Sequence
    module(termType = 70, name = "skip")(Top.Sequence)(
      fun(Top.Sequence)(arg("count", Top.Datum.Num))
    ),

    // Updates all the rows in a selection.  Calls its Function with the row
    // to be updated, and then merges the result of that call.
    // UPDATE   = 53; // StreamSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} ->
    // OBJECT |
    // SingleSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
    // StreamSelection, OBJECT,      {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
    // SingleSelection, OBJECT,      {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT
    module(termType = 53, name = "update", doc =
      """Updates all the rows in a selection.  Calls its Function with the row
        |to be updated, and then merges the result of that call.
      """.stripMargin
    )(Top.Datum.Obj)(
        fun(Top.Datum.SingleSelection)(
          arg("f", Top.FunctionArg(1)),
          opt("non_atomic", Top.Datum.Bool),
          opt("durability", Top.Datum.Str),
          opt("return_changes", Top.Datum.Bool)
        ),
        fun(Top.Datum.SingleSelection)(
          arg("obj", Top.Datum.Obj),
          opt("non_atomic", Top.Datum.Bool),
          opt("durability", Top.Datum.Str),
          opt("return_changes", Top.Datum.Bool)
        )
    ),

    // REPLACE  = 55;
    // StreamSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
    // SingleSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT
    module(termType = 55, name = "replace", doc =
      """Replaces all the rows in a selection.  Calls its Function with the row
        |to be replaced, and then discards it and stores the result of that call.
      """.stripMargin
    )(Top.Datum.Obj)(
      fun(Top.Datum.SingleSelection)(
        arg("f", Top.FunctionArg(1)),
        opt("non_atomic", Top.Datum.Bool),
        opt("durability", Top.Datum.Str),
        opt("return_changes", Top.Datum.Bool)
      ),
      fun(Top.Datum.SingleSelection)(
        arg("obj", Top.Datum.Obj),
        opt("non_atomic", Top.Datum.Bool),
        opt("durability", Top.Datum.Str),
        opt("return_changes", Top.Datum.Bool)
      )
    ),

    // Table, OBJECT, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT |
    // Table, Sequence, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT
    module(termType = 56, name = "insert", doc =
      """Inserts into a table.  If `conflict` is replace, overwrites
        |entries with the same primary key.  If `conflict` is
        |update, does an update on the entry.  If `conflict` is
        |error, or is omitted, conflicts will trigger an error.
        |{{{
        |r.table("posts").insert({
        |    id: 1,
        |    title: "Lorem ipsum",
        |    content: "Dolor sit amet"
        |}).run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Datum.Obj)(
      fun(Top.Sequence.Table)(
        arg("data", Top.Datum.Obj),
        opt("conflict", Top.Datum.Str),
        opt("durability", Top.Datum.Str),
        opt("return_changes", Top.Datum.Bool)
      ),
      fun(Top.Sequence.Table)(
        arg("batch", Top.Sequence),
        opt("conflict", Top.Datum.Str),
        opt("durability", Top.Datum.Str),
        opt("return_changes", Top.Datum.Bool)
      )
    ),

    // SEQUENCE, STRING -> GROUPED_SEQUENCE | SEQUENCE, FUNCTION -> GROUPED_SEQUENCE
    genGroupModule(144, "group"),
    genGroupModule(145, "sum"),
    genGroupModule(146, "avg"),
    genGroupModule(147, "min"),
    genGroupModule(148, "max"),

    module(termType = 152, name = "changes")(Top.Sequence.Stream)(
      fun(Top.Sequence)(opt("include_initial", Top.Datum.Bool), opt("include_states", Top.Datum.Bool)),
      fun(Top.Datum.SingleSelection)(opt("include_initial", Top.Datum.Bool), opt("include_states", Top.Datum.Bool))
    ),

    //----------------------------------------------------
    //
    //  Work with time
    //
    //----------------------------------------------------

    module(termType = 102, name = "toEpochTime",
      doc="Returns seconds since epoch in UTC given a time."
    )(Top.Datum.Num)(
      fun(Top.PseudoType.Time)()
    ),

    module(termType = 103, name = "now")(Top.PseudoType.Time)(fun()),
    module(termType = 104, name = "inTimezone", doc =
      """
        |Puts a time into an ISO 8601 timezone.
      """.stripMargin)(Top.Datum)(
        fun(Top.PseudoType.Time)(arg("timezone", Top.Datum.Str))
      ),
    module(termType = 105, name = "during", doc =
      """
        |a.during(b, c) returns whether a is in the range [b, c]
      """.stripMargin)(Top.Datum.Bool)(
        fun(Top.PseudoType.Time)(arg("left", Top.PseudoType.Time), arg("right", Top.PseudoType.Time))
      ),
    module(termType = 106, name = "date", doc =
      """
        |Retrieves the date portion of a time.
      """.stripMargin)(Top.PseudoType.Time)(
        fun(Top.PseudoType.Time)()
      ),
    module(termType = 126, name = "timeOfDay", doc =
      """
        |x.time_of_day == x.date - x
      """.stripMargin)(Top.Datum.Num)(
        fun(Top.PseudoType.Time)()
      ),
    module(termType = 127, name = "timezone", doc =
      """
        |Returns the timezone of a time.
      """.stripMargin)(Top.Datum.Str)(
        fun(Top.PseudoType.Time)()
      ),

    // Append a single element to the end of an array (like `snoc`).
//    APPEND = 29; // ARRAY, DATUM -> ARRAY
  // Prepend a single element to the end of an array (like `cons`).
 // PREPEND = 80; // ARRAY, DATUM -> ARRAY
    module(termType = 29, name = "append")(Top.Arr)(
      fun(Top.Datum.Arr)(arg("x", Top.Datum))
    ),
    module(termType = 80, name = "prepend")(Top.Arr)(
      fun(Top.Datum.Arr)(arg("x", Top.Datum))
    ),
    //CONTAINS = 93; // Sequence, (DATUM | Function(1))... -> BOOL
    module(termType = 93, name = "contains")(Top.Datum.Bool)(
      fun(Top.Sequence)(multiarg("x", Top.Datum)),
      fun(Top.Sequence)(arg("f", Top.FunctionArg(1)))
    ),

    // Deletes all the rows in a selection.
    //DELETE   = 54; // StreamSelection, {durability:STRING, return_changes:BOOL} -> OBJECT | SingleSelection -> OBJECT
    module(termType = 54, name = "delete")(Top.Datum.Obj)(
      fun(Top.Sequence.StreamSelection)(opt("durability", Top.Datum.Str), opt("return_changes", Top.Datum.Bool)),
      fun(Top.Datum.SingleSelection)()
    ),

    module(termType = 128, name = "year")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 129, name = "month")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 130, name = "day")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 131, name = "dayOfWeek")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 132, name = "dayOfYear")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 133, name = "hours")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 134, name = "minutes")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 135, name = "seconds")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),

    module(termType = 136, name = "time", doc =
      """
        |time(year, month, day[, hour, minute, second], timezone)
        |
        |Create a time object for a specific time.
        |
        |A few restrictions exist on the arguments:
        |
        |  * year is an integer between 1400 and 9,999.
        |  * month is an integer between 1 and 12.
        |  * day is an integer between 1 and 31.
        |  * hour is an integer.
        |  * minutes is an integer.
        |  * seconds is a double. Its value will be rounded to three decimal places (millisecond-precision).
        |  * timezone can be 'Z' (for UTC) or a string with the format ±[hh]:[mm].
        |
        |Example:
        |
        |Update the birthdate of the user "John" to November 3rd, 1986 UTC.
        |{{{
        |r.table("user").get("John").update({birthdate: r.time(1986, 11, 3, 'Z')})
        |    .run(conn, callback)
        |}}}
      """.stripMargin)(Top.PseudoType.Time)(
        fun(
          arg("year", Top.Datum.Num),
          arg("month", Top.Datum.Num),
          arg("day", Top.Datum.Num),
          arg("timezone", Top.Datum.Str)
        ),
        fun(
          arg("year", Top.Datum.Num),
          arg("month", Top.Datum.Num),
          arg("day", Top.Datum.Num),
          arg("hour", Top.Datum.Num),
          arg("minutes", Top.Datum.Num),
          arg("seconds", Top.Datum.Num),
          arg("timezone", Top.Datum.Str)
        )
      ),
      module(termType = 41, name = "orderBy")(Top.Sequence)(
        fun(Top.Sequence)(multiarg("field", Top.Datum.Str), opt("index", Top.Datum.Str)),
        fun(Top.Sequence)(multiarg("ordering", Top.Ordering), opt("index", Top.Ordering))
      ),
      module(termType = 73, name = "asc")(Top.Ordering)(
        fun(arg("field", Top.Datum.Str))
      ),
      module(termType = 74, name = "desc")(Top.Ordering)(
        fun(arg("field", Top.Datum.Str))
      ),

      //COUNT = 43; // Sequence -> NUMBER | Sequence, DATUM -> NUMBER | Sequence, Function(1) -> NUMBER
      module(termType = 43, name = "count")(Top.Datum.Num)(
        fun(Top.Sequence)(),
        fun(Top.Sequence)(arg("value", Top.Datum)),
        fun(Top.Sequence)(arg("f", Top.FunctionArg(1)))
      ),

      //DEFAULT = 92; // Top, Top -> Top
      module(termType = 92, name = "default")(Top.AnyType)(
        fun(Top.Datum)(arg("value", Top))
      )

    /*
        IMPLEMENT ME

        // Constants for ISO 8601 days of the week.
        MONDAY = 107;    // -> 1
        TUESDAY = 108;   // -> 2
        WEDNESDAY = 109; // -> 3
        THURSDAY = 110;  // -> 4
        FRIDAY = 111;    // -> 5
        SATURDAY = 112;  // -> 6
        SUNDAY = 113;    // -> 7

        // Constants for ISO 8601 months.
        JANUARY = 114;   // -> 1
        FEBRUARY = 115;  // -> 2
        MARCH = 116;     // -> 3
        APRIL = 117;     // -> 4
        MAY = 118;       // -> 5
        JUNE = 119;      // -> 6
        JULY = 120;      // -> 7
        AUGUST = 121;    // -> 8
        SEPTEMBER = 122; // -> 9
        OCTOBER = 123;   // -> 10
        NOVEMBER = 124;  // -> 11
        DECEMBER = 125;  // -> 12

     */
  )


}
