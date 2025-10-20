package im.bigs.pg.external.pg.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@EnableFeignClients(basePackages = ["im.bigs.pg.external.pg"])
@Configuration
class FeignConfig {
}