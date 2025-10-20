package im.bigs.pg.external.pg.test

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.external.pg.secret.AesGcmEncryptor
import im.bigs.pg.external.pg.test.client.TestPgFeignClient
import im.bigs.pg.external.pg.test.port.out.TestPgRequest
import im.bigs.pg.external.pg.test.port.out.TestPgResponseFailure
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.LocalDateTime

@Component
class TestPgClient(
    private val testPgFeignClient: TestPgFeignClient,
    private val aesGcmEncryptor: AesGcmEncryptor,
    private val objectMapper: ObjectMapper
) : PgClientOutPort {

    @Value("\${external.pg.test.api-key}")
    private lateinit var apiKey: String

    override fun supports(partnerId: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {
        try {
            val encodedData = TestPgRequest(aesGcmEncryptor.encrypt(toPlainText(request)))

            val response = testPgFeignClient.payCreditCard(apiKey=apiKey, request=encodedData)
            return PgApproveResult(
                approvalCode = response.approvalCode,
                approvedAt = LocalDateTime.parse(response.approvedAt),
                status = when (response.status) {
                    "APPROVED" -> im.bigs.pg.domain.payment.PaymentStatus.APPROVED
                    else -> im.bigs.pg.domain.payment.PaymentStatus.CANCELED
                }
            )
        } catch (ex: FeignException) {
            when (ex) {
                is FeignException.Unauthorized ->
                    throw IllegalAccessException("PG API 인증 실패 (401). API-KEY를 확인하세요.")
                is FeignException.UnprocessableEntity -> {
                    val failure = objectMapper.readValue(ex.contentUTF8(), TestPgResponseFailure::class.java)
                    throw IllegalArgumentException("[${failure.code}] 결제 실패: [${failure.errorCode}] ${failure.message}")
                }
                else -> throw IllegalStateException("PG API 호출 오류: ${ex.message}")
            }
        }
    }

    private fun toPlainText(request: PgApproveRequest): String {
        return """
            {
                "partnerId": ${request.partnerId},
                "amount": ${request.amount},
                "cardBin": "${request.cardBin}",
                "cardLast4": "${request.cardLast4}",
                "productName": "${request.productName}"
            }
        """.trimIndent()
    }
}
