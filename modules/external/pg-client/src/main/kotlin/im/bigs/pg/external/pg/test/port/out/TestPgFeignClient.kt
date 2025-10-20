package im.bigs.pg.external.pg.test.client

import im.bigs.pg.external.pg.config.FeignConfig
import im.bigs.pg.external.pg.test.port.out.TestPgRequest
import im.bigs.pg.external.pg.test.port.out.TestPgResponseSuccess
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "testPgClient", url = "\${test.pg.api.url}", configuration=[FeignConfig::class])
interface TestPgFeignClient {
    @PostMapping("/api/v1/pay/credit-card")
    fun payCreditCard(
        @RequestHeader("API-KEY") apiKey: String,
        @RequestBody request: TestPgRequest
    ): TestPgResponseSuccess
}