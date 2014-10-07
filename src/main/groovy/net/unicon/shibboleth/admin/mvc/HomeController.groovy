package net.unicon.shibboleth.admin.mvc

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class HomeController {
    @RequestMapping('/')
    def home() {
        'home'
    }
}
