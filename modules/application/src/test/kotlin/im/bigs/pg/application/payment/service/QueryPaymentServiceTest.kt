package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class 결제조회서비스Test {
    private val paymentRepo = mockk<PaymentOutPort>()
    private val service = QueryPaymentsService(paymentRepo)

    @Test
    @DisplayName("필터를 기반으로 결제 내역 조회 시, 통계는 반드시 필터와 동일한 집합을 대상으로 계산되어야 하며, 커서 기반 페이지네이션을 사용해야 한다")
    fun `필터를 기반으로 결제 내역 조회 시, 통계는 반드시 필터와 동일한 집합을 대상으로 계산되어야 하며, 커서 기반 페이지네이션을 사용해야 한다`() {
        val payments = listOf(
            createPayment(1L, LocalDateTime.of(2024, 1, 2, 0, 0), BigDecimal("1000")),
            createPayment(2L, LocalDateTime.of(2024, 1, 1, 0, 0), BigDecimal("2000"))
        )
        val firstPageQuery = slot<PaymentQuery>()
        every { paymentRepo.findBy(capture(firstPageQuery)) } returns PaymentPage(
            items = payments,
            nextCursorCreatedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            nextCursorId = 2L,
            hasNext = false
        )

        val summaryFilter = slot<PaymentSummaryFilter>()
        every { paymentRepo.summary(capture(summaryFilter)) } returns PaymentSummaryProjection(
            count = 2,
            totalAmount = BigDecimal("3000"),
            totalNetAmount = BigDecimal("2700")
        )

        val filter = QueryFilter(
            partnerId = 1L,
            status = "APPROVED",
            from = LocalDateTime.of(2024, 1, 1, 0, 0),
            to = LocalDateTime.of(2024, 1, 2, 23, 59),
            cursor = null,
            limit = 2
        )
        val result = service.query(filter)

        with(firstPageQuery.captured) {
            assertEquals(filter.partnerId, partnerId)
            assertEquals(PaymentStatus.APPROVED, status)
            assertEquals(filter.from, from)
            assertEquals(filter.to, to)
            assertEquals(filter.limit, limit)
        }

        with(summaryFilter.captured) {
            assertEquals(filter.partnerId, partnerId)
            assertEquals(PaymentStatus.APPROVED, status)
            assertEquals(filter.from, from)
            assertEquals(filter.to, to)
        }

        assertEquals(2, result.items.size)
        assertEquals(2, result.summary.count)
        assertEquals(BigDecimal("3000"), result.summary.totalAmount)
        assertEquals(BigDecimal("2700"), result.summary.totalNetAmount)
        assertFalse(result.hasNext)
        assertNotNull(result.nextCursor)
    }

    private fun createPayment(
        id: Long,
        createdAt: LocalDateTime,
        amount: BigDecimal
    ) = Payment(
        id = id,
        partnerId = 1L,
        amount = amount,
        appliedFeeRate = BigDecimal("0.1"),
        feeAmount = amount.multiply(BigDecimal("0.1")),
        netAmount = amount.multiply(BigDecimal("0.9")),
        cardBin = "123456",
        cardLast4 = "1234",
        approvalCode = "TEST-$id",
        approvedAt = createdAt,
        status = PaymentStatus.APPROVED,
        createdAt = createdAt
    )
}