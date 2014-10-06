package net.unicon.shibboleth.idp.relyingparty.config.mvc

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class HomeController {
    @RequestMapping('/')
    def home() {
        'home'
    }
}
