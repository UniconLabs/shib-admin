package net.unicon.shibboleth.idp.relyingparty.config.mvc

import groovy.text.TemplateEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
/**
 * Spring MVC controller for the config generation UI
 */
class ShibIdpConfigController {

    @Autowired
    private TemplateEngine templateEngine

    @Autowired
    private ResourceLoader resourceLoader

    private static RELYING_PARTY_TEMPLATE_PATH = 'classpath:/templates/relying-party.tpl'

    @RequestMapping('/relying-party.xml')
    def generateRelyingPartyConfig(def relyingPartyConfigRequest) {
        //TODO: obviously need to do the dynamic model binding for the template which would be gathered from the UI
        // For now the model contains hardcoded values so we could prove the dynamic Groovy Markup template binding
        def relyingPartyConfigResponsePayload = generateRelyingPartyConfigFromTemplate([idpEntityId: 'https://localhost:9443/idp/shibboleth',
                                                                                        idpHome    : '/opt/shibboleth-idp'])

        new ResponseEntity<byte[]>(relyingPartyConfigResponsePayload, ['Content-Type': 'application/octet-stream'] as HttpHeaders, HttpStatus.OK)
    }

    @RequestMapping('/')
    def home() {
        'home'
    }

    byte[] generateRelyingPartyConfigFromTemplate(def model) {
        def writable = this.templateEngine.createTemplate(this.resourceLoader.getResource(RELYING_PARTY_TEMPLATE_PATH).URL).make(model)
        def result = new StringWriter()
        writable.writeTo(result)
        result.toString()
    }
}