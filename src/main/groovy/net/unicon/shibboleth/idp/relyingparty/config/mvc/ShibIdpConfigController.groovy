package net.unicon.shibboleth.idp.relyingparty.config.mvc

import groovy.text.TemplateEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Spring MVC controller for the config generation UI
 */
@Controller
@SessionAttributes('relyingPartyConfigForm')
class ShibIdpConfigController {

    @Autowired
    private TemplateEngine templateEngine

    @Autowired
    private ResourceLoader resourceLoader

    private static RELYING_PARTY_TEMPLATE_PATH = 'classpath:/templates/relying-party.tpl'

    private static DEFAULT_IDP_ENTITY_ID = 'https://localhost:9443/shibboleth/idp'

    private static DEFAULT_IDP_HOME_PATH = '/opt/shibboleth-idp'

    @ModelAttribute('metadataProviderConfigForm')
    def metadataProviderConfigForm() {
        //Form-backing object to bind to from the UI
        new MetadataProviderConfig()
    }

    @ModelAttribute('relyingPartyConfigForm')
    def relyingPartyConfigForm() {
        //Populate the form-backing object with defaults (and store it in HTTP session by means of type-level @SessionAttributes annotation)
        new IdpConfig(idpEntityId: DEFAULT_IDP_ENTITY_ID, idpHome: DEFAULT_IDP_HOME_PATH)
    }

    @RequestMapping('/')
    def home() {
        'home'
    }

    @RequestMapping(value = '/metadataProvider', method = RequestMethod.POST)
    def addMetadataProvider(@ModelAttribute('relyingPartyConfigForm') IdpConfig relyingPartyConfigRequest, MetadataProviderConfig mdProviderConfigRequest) {
        //TODO obviously needs validation, etc.
        relyingPartyConfigRequest.addMetadataProvider mdProviderConfigRequest
        'home'
    }


    @RequestMapping(value = '/relying-party.xml', method = RequestMethod.POST)
    def generateRelyingPartyConfig(@ModelAttribute('relyingPartyConfigForm') IdpConfig relyingPartyConfigRequest) {

        //TODO: more advanced relying-party dynamic config values
        //TODO: form submission validation (required fields, etc.)
        def relyingPartyConfigResponsePayload = generateRelyingPartyConfigFromTemplate([idpEntityId: relyingPartyConfigRequest.idpEntityId,
                                                                                        idpHome    : relyingPartyConfigRequest.idpHome,
                                                                                        metadataProviders: relyingPartyConfigRequest.metadataProviders])

        new ResponseEntity<byte[]>(relyingPartyConfigResponsePayload, ['Content-Type': 'application/octet-stream'] as HttpHeaders, HttpStatus.OK)
    }

    private byte[] generateRelyingPartyConfigFromTemplate(def model) {
        def writable = this.templateEngine.createTemplate(this.resourceLoader.getResource(RELYING_PARTY_TEMPLATE_PATH).URL).make(model)
        def result = new StringWriter()
        writable.writeTo(result)
        result.toString()
    }

    private mockMetadataProviders() {
        [new MetadataProviderConfig(id: 'URLMD',
                metadataUrl: 'http://www.testshib.org/metadata/testshib-providers.xml',
                backingFile: '/opt/shibboleth-idp/metadata/testshib.xml'),
                           new MetadataProviderConfig(id: 'URLMD2', metadataFile: '/opt/shibboleth-idp/metadata/example-sp-metadata.xml')]
    }

    static class MetadataProviderConfig {
        String id
        String metadataUrl
        String backingFile
        String metadataFile

        def getType() {
            if(metadataUrl) {
                return 'metadata:FileBackedHTTPMetadataProvider'
            }
            else if (metadataFile) {
                return 'metadata:FilesystemMetadataProvider'
            }
            else {
                return 'metadata:Unknown'
            }
        }
    }

    static class IdpConfig {
        String idpEntityId
        String idpHome
        //Just being explicit here about the type
        List<MetadataProviderConfig> metadataProviders = []

        def addMetadataProvider(MetadataProviderConfig mdProviderConfig) {
            metadataProviders << mdProviderConfig
        }
    }
}