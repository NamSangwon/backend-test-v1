package im.bigs.pg.external.pg.test.port.out

data class TestPgRequest (
    val enc: String, // Base64URL(ciphertext || tag)
)