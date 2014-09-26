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

    private static DEFAULT_IDP_ENTITY_ID = 'https://localhost:9443/shibboleth/idp'

    private static DEFAULT_IDP_HOME_PATH = '/opt/shibboleth-idp'

    @RequestMapping('/relying-party.xml')
    def generateRelyingPartyConfig(ConfigForm relyingPartyConfigRequest) {

        //TODO: more advanced relying-party dynamic config values
        //TODO: form submission validation (required fields, etc.)
        def relyingPartyConfigResponsePayload = generateRelyingPartyConfigFromTemplate([idpEntityId: relyingPartyConfigRequest.idpEntityId,
                                                                                        idpHome    : relyingPartyConfigRequest.idpHome])

        new ResponseEntity<byte[]>(relyingPartyConfigResponsePayload, ['Content-Type': 'application/octet-stream'] as HttpHeaders, HttpStatus.OK)
    }

    @RequestMapping('/')
    def home(Map model) {
        //Populate the form-backing object with defaults
        model.relyingPartyConfigForm = new ConfigForm(idpEntityId: DEFAULT_IDP_ENTITY_ID, idpHome: DEFAULT_IDP_HOME_PATH)
        'home'
    }

    byte[] generateRelyingPartyConfigFromTemplate(def model) {
        def writable = this.templateEngine.createTemplate(this.resourceLoader.getResource(RELYING_PARTY_TEMPLATE_PATH).URL).make(model)
        def result = new StringWriter()
        writable.writeTo(result)
        result.toString()
    }

    static class ConfigForm {
        String idpEntityId
        String idpHome
    }
}