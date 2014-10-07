package net.unicon.shibboleth.admin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableAutoConfiguration
class ShibIdpRelyingPartyConfigGenerationApplication {

    static main(args) {
        SpringApplication.run ShibIdpRelyingPartyConfigGenerationApplication, args
    }
}
