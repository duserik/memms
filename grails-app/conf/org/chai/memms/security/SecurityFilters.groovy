package org.chai.memms.security

/**
 * Generated by the Shiro plugin. This filters class protects all URLs
 * via access control by convention.
 */
class SecurityFilters {
    def filters = {
        all(uri: "/**") {
            before = {
				log.debug("filtering params: "+params+", controller: "+controllerName+", action: "+actionName)
                // Ignore direct views (e.g. the default main index page).
                if (!controllerName) return false
				
                // Access control by convention.
              //  accessControl()
            }
        }
    }
}
