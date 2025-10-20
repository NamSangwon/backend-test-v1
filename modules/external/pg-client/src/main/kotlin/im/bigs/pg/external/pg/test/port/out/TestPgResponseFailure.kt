package im.bigs.pg.external.pg.test.port.out

data class TestPgResponseFailure (
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)