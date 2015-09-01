object ApiDefinitions {

  // fun definitions for
  val groupedFunctions = Seq(
    fun(Top.Sequence)(arg("fieldName", Top.Datum.Str)) //,
    //fun(Top.Sequence)(arg("field", Top.Datum.Field)),
    //fun(Top.Sequence)(arg("f", Top.Function))
  )

  val modules = Seq(
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
      ),
      fun(Top.Database)(
        arg("name", Top.Datum.Str)
      ),
      fun(Top.Database)(
        arg("name", Top.Datum.Str),
        opt("read_mode", Top.Datum.Str)
      )
    ),

    // Gets a single element from a table by its primary or a secondary key.
    // Table, STRING -> SingleSelection | Table, NUMBER -> SingleSelection |
    // Table, STRING -> NULL            | Table, NUMBER -> NULL |
    module(termType = 16, name = "get")(Top.Datum.SingleSelection)(
      fun(Top.Sequence.Table)(arg("key", Top.Datum.Str)),
      fun(Top.Sequence.Table)(arg("key", Top.Datum.Num))
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
      fun(Top.Sequence.Table)(arg("data", Top.Datum.Obj)),
      fun(Top.Sequence.Table)(arg("data", Top.Sequence))
    ),

    // SEQUENCE, STRING -> GROUPED_SEQUENCE | SEQUENCE, FUNCTION -> GROUPED_SEQUENCE
    module(termType = 144, name = "group")(Top.Sequence)(groupedFunctions: _*),
    module(termType = 145, name = "sum")(Top.Sequence)(groupedFunctions: _*),
    module(termType = 146, name = "avg")(Top.Sequence)(groupedFunctions: _*),
    module(termType = 147, name = "min")(Top.Sequence)(groupedFunctions: _*),
    module(termType = 148, name = "max")(Top.Sequence)(groupedFunctions: _*),

    module(termType = 152, name = "changes")(Top.Sequence.Stream)(
      fun(Top.Sequence.Table)()
    ),

    module(termType = 103, name = "now")(Top.Datum)(
      fun()
    )
  )
}
