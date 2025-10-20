package im.bigs.pg.external.pg.config

import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients(basePackages = ["im.bigs.pg.external.pg"])
@Configuration
class FeignConfig {
}