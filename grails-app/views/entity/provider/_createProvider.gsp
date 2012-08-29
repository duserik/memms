<%@ page import="org.chai.memms.equipment.Provider.Type" %>
<div class="entity-form-container togglable">
	<div>
		<h3>
			<g:if test="${provider.id != null}">
				<g:message code="default.edit.label" args="[message(code:'provider.label')]" />
			</g:if>
			<g:else>
				<g:message code="default.new.label" args="[message(code:'provider.label')]" />
			</g:else>
		</h3>
		<g:locales />
	</div>
		<g:form url="[controller:'provider', action:'save', params:[targetURI: targetURI]]" useToken="true">
			<g:input name="code" label="${message(code:'entity.code.label')}" bean="${provider}" field="code"/>
			<g:selectFromEnum name="type" bean="${provider}" values="${Type.values()}" field="type" label="${message(code:'entity.type.label')}"/>
			<g:input name="contact.contactName" label="${message(code:'entity.name.label')}" bean="${provider?.contact}" field="contactName"/>
	      	<g:input name="contact.email" label="${message(code:'contact.email.label')}" bean="${provider?.contact}" field="email"/>
	      	<g:input name="contact.phone" label="${message(code:'contact.phone.label')}" bean="${provider?.contact}" field="phone"/>
	      	<g:input name="contact.poBox" label="${message(code:'contact.pobox.label')}" bean="${provider?.contact}" field="poBox"/>
	  	    <g:i18nTextarea name="contact.addressDescriptions" bean="${provider?.contact}" label="${message(code:'contact.address.descriptions.label')}" field="addressDescriptions" height="150" width="300" maxHeight="150" />		
			<g:if test="${provider.id != null}">
				<input type="hidden" name="id" value="${provider.id}"></input>
			</g:if>
			<div>
				<button type="submit"><g:message code="default.button.save.label"/></button>
				<a href="${createLink(uri: targetURI)}"><g:message code="default.link.cancel.label"/></a>
			</div>
		</g:form>
</div>