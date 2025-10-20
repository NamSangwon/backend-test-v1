package im.bigs.pg.external.pg.secret

interface Encryptor {
    fun encrypt(plainText: String): String
}