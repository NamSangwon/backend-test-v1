package im.bigs.pg.external.pg.test.port.out

data class TestPgResponseSuccess (
    val approvalCode: String,
    val approvedAt: String,
    val maskedCardLast4: String,
    val amount: Int,
    val status: String
)