/**
 * 
 */
package org.chai.memms.equipment

import org.chai.memms.AbstractEntityController;

/**
 * @author Jean Kahigiso M.
 *
 */
class DepartmentController extends AbstractEntityController{
	def departmentService
	def languageService
	
	def getEntity(def id) {
		return Department.get(id);
	}

	def createEntity() {
		return new Department();
	}

	def getTemplate() {
		return "/entity/department/createDepartment";
	}

	def getLabel() {
		return "department.label";
	}
	
	def deleteEntity(def entity) {
		if (entity.equipments.size() != 0) {
			flash.message = message(code: 'department.hasequipments', args: [message(code: getLabel(), default: 'entity'), params.id], default: 'Department {0} still has associated equipments.')
		}
		else {
			super.deleteEntity(entity)
		}
	}
	
	def getEntityClass() {
		return Department.class;
	}
	def bindParams(def entity) {
		entity.properties = params
	}
	
	def getModel(def entity) {
		[
			department:entity
		]
	}
	
	def list={
		adaptParamsForList()
		def departments = Department.list(params)
		render(view:"/entity/list", model:[
			template:"department/departmentList",
			entities: departments,
			entityCount: Department.count(),
			code: getLabel(),
			entityClass: getEntityClass()
			])
	}
	
	def getAjaxData = {
		List<Department> departments = departmentService.searchDepartment(params['term'], [:])
		render(contentType:"text/json") {
			elements = array {
				departments.each { department ->
					elem (
						key: department.id,
						value: department.getNames(languageService.getCurrentLanguage()) 
					)
				}
			}
		}
		
	}
	def search = {
		adaptParamsForList()
		List<Department> departments = departmentService.searchDepartment(params['q'], params)
				
		render (view:"/entity/list", model:[
			template:"department/departmentList",
			entities: departments,
			entityCount: departmentService.countDepartment(params['q']),
			code: getLabel()
		])
		
	}
	def importer ={
		
	}
	def export = {
		
	}
}