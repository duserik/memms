/**
 * Copyright (c) 2012, Clinton Health Access Initiative.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chai.memms.security

import java.util.List;
import java.util.Map;

import org.chai.memms.AbstractEntityController;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.chai.memms.inventory.EquipmentType;
import org.chai.memms.inventory.Provider;
import org.chai.memms.inventory.Equipment.Donor;
import org.chai.memms.inventory.Equipment.PurchasedBy;
import org.chai.memms.inventory.EquipmentStatus.Status;
import org.chai.memms.security.User;
import org.chai.memms.security.User.UserType;

import org.chai.location.DataLocation;
import org.chai.location.Location;
import org.chai.location.CalculationLocation;
import org.chai.memms.security.Role


class UserController  extends  AbstractEntityController{
	def userService

	def getEntity(def id) {
		return User.get(id)
	}

	def createEntity() {
		return new User()
	}

	def getLabel() {
		return 'user.label'
	}
	
	def getModel(def entity) {
		def locations = []
		def roles = []
		if (entity.location != null) locations << entity.location
		[
			user:entity,
			roles: Role.list(),
			locations: DataLocation.list(),
			cmd: params['cmd']
		]
	}
	
	
	def getEntityClass(){
		return User.class;
	}
	
	def deleteEntity(def entity) {
		flash.message = message(code: 'user.cannot.be.delete', args: [message(code: getLabel(), default: 'entity'), params.id], default: '{0} cannot but can deactivated instead.')
	}
	
	def getTemplate() {
		return "/entity/user/createUser"
	}
	
	def validateEntity(def entity) {
		//TODO check for duplicate code
		boolean valid = entity.validate() && params['cmd'].validate()
		if (log.isDebugEnabled()) log.debug ("validation for command object ${params['cmd']}: ${params['cmd'].errors}}")
		return valid;
	}
	
	def saveEntity(def entity) {
		if(entity.location) hasAccess(entity.location)
		entity.save(failOnError:true)
	}
	
	
	def bindParams(def entity) {
		if (log.isDebugEnabled()) log.debug('binding params: '+params)
		bindData(entity,params,[exclude:['uuid','passwordHash']])
		
		if(entity.id==null)
			entity.uuid = UUID.randomUUID().toString();
					
		if(params['cmd']?.password != null && !params['cmd']?.password.equals(''))
			entity.passwordHash = new Sha256Hash(params['cmd'].password).toHex();
	}
	

	
	def save = { PasswordCommand cmd ->
		if (log.isDebugEnabled()) log.debug("create.userPassword, params:"+params+"command"+cmd)
		params['cmd'] = cmd;
		super.save()
	}
	
	
	
	def list = {
		adaptParamsForList()
		def users = User.list(offset:params.offset,max:params.max,sort:params.sort ?:"id",order: params.order ?:"desc");
		if(request.xhr)
			this.ajaxModel(users,"")
		else{
			render (view: '/entity/list', model: model(users) << [
				template:"user/userList",
				filterTemplate:"user/userFilter",
				listTop:"user/listTop",				
			])
		}
	}
	
	
	def search = {
		adaptParamsForList()
		def users = userService.searchUser(params['q'], params);
		if(request.xhr)
			this.ajaxModel(users,params['q'])
		else {
			render (view: '/entity/list', model:[
				template:"user/userList",
				filterTemplate:"user/userFilter",
				listTop:"user/listTop",
			])
		}
	}


	def filter = { FilterCommand cmd ->
			adaptParamsForList()
			def users = userService.filterUser(user,cmd.location,cmd.roles,cmd.active,cmd.confirmed,params)
			if(request.xhr)
				this.ajaxModel(users)
			else {
				render(view:"/entity/list", model: model(users) << [
					template:"user/userList",
					filterTemplate:"user/userFilter",
					listTop:"user/listTop"
				])
			}
	}
	
	
		
	def model(def entities) {
		return [
			entities: entities,
			entityCount: entities.totalCount,
			code: getLabel(),
			entityClass: getEntityClass(),
			roles:Role.list(),
			locations: DataLocation.list()
		]
	}
	
	def ajaxModel(def entities,def searchTerm) {
		def model = model(entities) << [q: searchTerm]
		def listHtml = g.render(template:"/entity/user/userList",model:model)
		render(contentType:"text/json") { results = [listHtml] }
	}
	
	def updateActive = {
		if (log.isDebugEnabled()) log.debug("updateActive user.active "+params['user.id'])
		User user = User.get(params.int(['user.id']))
		def property = params['field'];
		if (user == null || property ==null)
			response.sendError(404)
		else {
			def value= false; def entity = null;
			if(property.equals("active")){
				if(user.active) user.active = false
				else {
					user.active = true		
				}
				entity = user.save(flush:true)
			}
			if(entity!=null) value=true
			render(contentType:"text/json") { results = [value]}
		}
	}
	
	def updateConfirmed = {
		if (log.isDebugEnabled()) log.debug("updateActive user.confirmed "+params['user.id'])
		User user = User.get(params.int(['user.id']))
		def property = params['field'];
		if (user == null || property ==null)
			response.sendError(404)
		else {
			def value= false; def entity = null;
			if(property.equals("confirmed")){
				if(user.confirmed) user.confirmed = false
				else {
					user.confirmed = true
				}
				entity = user.save(flush:true)
			}
			if(entity!=null) value=true
			render(contentType:"text/json") { results = [value]}
		}
	}
	
	def getAjaxData = {
		def location = CalculationLocation.get(params["location"])
		List<UserType> userTypes = []
		for(def type: params['userTypes']) 
			userTypes.add(UserType."$type")
       	def users = userService.searchActiveUserByTypeAndLocation(params['term'],userTypes,location)
		render(contentType:"text/json") {
			elements = array {
				users.each { user ->
					elem (
						key: user.id,
						value: user.names
						                                                
					)
				}
			}
		}
		
	}		
}

class PasswordCommand {
	String password
	String repeat

	static constraints = {
		password(blank: true,nullable:true, minSize: 4)
		repeat(nullable:true, validator: {val, obj ->
			val == obj.password
		})
	}
	
	String toString() {
		return "PasswordCommand[password="+password+", repeat="+repeat+"]"
	}
}

class FilterCommand {
	CalculationLocation location
	UserType userType
	Role roles
	String active
	String confirmed
	

	public boolean getActiveStatus(){
		if(active) return null
		else if(active.equals("true")) return true
		else if(active.equals("false")) return false
	}
	
	public boolean getConfirmedStatus(){
		if(confirmed) return null
		else if(confirmed.equals("true")) return true
		else if(confirmed.equals("false")) return false
	}

	static constraints = {

		userType nullable:true
		roles nullable:true
		active nullable:true
		confirmed nullable: true
		location nullable:false, validator:{val, obj ->
			return (obj.userType != null || obj.roles != null || obj.active || obj.confirmed)?true:"select.atleast.one.value.text"
		}
	}

	String toString() {
		return "FilterCommand[CalculationLocation="+location+", UserType="+userType+
		", Role="+roles+", active="+active+
		", confirmed=" + confirmed + "]"
	}
}
