package net.unicon.shibboleth.admin.mvc

import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager
import edu.internet2.middleware.shibboleth.idp.profile.IdPProfileHandlerManager
import org.opensaml.Configuration
import org.opensaml.common.xml.SAMLConstants
import org.opensaml.saml2.metadata.SPSSODescriptor
import org.opensaml.saml2.metadata.provider.MetadataProviderException
import org.opensaml.xml.util.XMLHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import javax.annotation.PostConstruct
import javax.validation.constraints.NotNull

@Controller
@RequestMapping('attribute-authority')
class AttributeAuthorityController {
    @Value('${idp.home}')
    @NotNull
    String shibHome

    ApplicationContext applicationContext

    @PostConstruct
    void setup() {
        this.applicationContext = new FileSystemXmlApplicationContext("file:${shibHome}/conf/internal.xml", "file:${shibHome}/conf/service.xml")
    }

    @ModelAttribute('attributeAuthorityForm')
    def attributeAuthorityForm() {
        new AttributeAuthorityConfig()
    }

    @ModelAttribute('authenticationTypes')
    def authenticationTypes() {
        IdPProfileHandlerManager handlerManager = this.applicationContext.getBean("shibboleth.HandlerManager")
        return handlerManager.loginHandlers.values().collect {it.supportedAuthenticationMethods}.flatten() as Set
    }

    @RequestMapping(method = RequestMethod.GET)
    def view() {
        'attribute-authority'
    }

    @RequestMapping(method = RequestMethod.POST)
    def postView(Model model, @ModelAttribute('attributeAuthorityForm') AttributeAuthorityConfig attributeAuthorityConfig) {
        if (attributeAuthorityConfig.principal) {
            SAML2AttributeAuthority saml2AttributeAuthority = applicationContext.getBean("shibboleth.SAML2AttributeAuthority", SAML2AttributeAuthority)
            def samlMdRelyingPartyConfigurationManager = applicationContext.getBean(SAMLMDRelyingPartyConfigurationManager)

            def requestCtx = new BaseSAMLProfileRequestContext().with {
                metadataProvider = samlMdRelyingPartyConfigurationManager.metadataProvider
                principalName = attributeAuthorityConfig.principal
                principalAuthenticationMethod = attributeAuthorityConfig.authnMethod

                def requester = attributeAuthorityConfig.requester
                if (requester) {
                    relyingPartyConfiguration = samlMdRelyingPartyConfigurationManager.getRelyingPartyConfiguration(attributeAuthorityConfig.requester)
                } else {
                    requester = samlMdRelyingPartyConfigurationManager.anonymousRelyingConfiguration.relyingPartyId
                    relyingPartyConfiguration = samlMdRelyingPartyConfigurationManager.anonymousRelyingConfiguration
                }

                try {
                    inboundMessageIssuer = requester
                    peerEntityId = requester
                    peerEntityMetadata = metadataProvider.getEntityDescriptor(requester)
                    if (peerEntityMetadata) {
                        peerEntityRole = SPSSODescriptor.DEFAULT_ELEMENT_NAME
                        peerEntityRoleMetadata = peerEntityMetadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS)
                    }
                } catch (MetadataProviderException e) {
                    model.addAttribute('attributeAuthorityResponse', "Unable to query for metadata for requester ${requester}\n${e.stackTrace.join("\n")}")
                    return 'attribute-authority'
                }

                try {
                    def issuer = relyingPartyConfiguration.providerId
                    outboundMessageIssuer = issuer
                    localEntityId = issuer
                    localEntityMetadata = metadataProvider.getEntityDescriptor(issuer)
                } catch (MetadataProviderException e) {
                    model.addAttribute('attributeAuthorityResponse', "Unable to query for metadata for issuer ${issuer}\n${e.stackTrace.join("\n")}")
                    return 'attribute-authority'
                }

                return it
            }
            def map = saml2AttributeAuthority.getAttributes(requestCtx)
            def attributeStatement = saml2AttributeAuthority.buildAttributeStatement(null, map.values())

            if (attributeStatement) {
                def statementMarshaller = Configuration.getMarshallerFactory().getMarshaller(attributeStatement)
                model.addAttribute('attributeAuthorityResponse', XMLHelper.prettyPrintXML(statementMarshaller.marshall(attributeStatement)))
            } else {
                model.addAttribute('attributeAuthorityResponse', 'Nothing found for query')
            }
        }
        return 'attribute-authority'
    }

    static class AttributeAuthorityConfig {
        String principal
        String requester
        String authnMethod
    }
}
