package controllers

import javax.inject._

import models.Employee
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


/**
  * Simple controller that directly stores and retrieves [models.Employee] instances into a MongoDB Collection
  * Input is first converted into a employee and then the employee is converted to JsObject to be stored in MongoDB
  */
@Singleton
class EmployeeController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  //Fetching the collection from MongoDB
  def employeesFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("employees"))

  // CREATE a new EMPLOYEE for provided FirstName, Last Name and Salary in URL 
  def create(fname: String, lname: String, sal: Int) = Action.async {
    for {
      employees <- employeesFuture
      lastError <- employees.insert(Employee(fname, lname, sal))
    } yield
      Ok("Mongo LastError: %s".format(lastError))
  }

  // CREATE a new EMPLOYEE for a JSON employee object
  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Employee](request.body) match {
      case JsSuccess(employee, _) =>
        for {
          employees <- employeesFuture
          lastError <- employees.insert(employee)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 city")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a Employee from the json provided. " + Errors.show(errors)))
    }
  }
// CREATE multiple EMPLOYEES for a JSON employees objects
  def createBulkFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Employee]](request.body) match {
      case JsSuccess(newEmployees, _) =>
        employeesFuture.flatMap { employees =>
          val documents = newEmployees.map(implicitly[employees.ImplicitlyDocumentProducer](_))

          employees.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} cities")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a Employee from the json provided. " + Errors.show(errors)))
    }
  }

  def findByName(fname: String) = Action.async {
    // let's do our query
    val futureCitiesList: Future[List[Employee]] = employeesFuture.flatMap {
      // find all cities with name `name`
      _.find(Json.obj("FirstName" -> fname)).
      // perform the query and get a cursor of JsObject
      cursor[Employee](ReadPreference.primary).
      // Coollect the results as a list
      collect[List]()
    }

    // everything's ok! Let's reply with a JsValue
    futureCitiesList.map { employees =>
      Ok(Json.toJson(employees))
    }
  }
  
   def findBySalary(salary: Int) = Action.async {
    // let's do our query
    val futureCitiesList: Future[List[Employee]] = employeesFuture.flatMap {
      // find all cities with name `name`
      _.find(Json.obj("Salary" -> salary)).
      // perform the query and get a cursor of JsObject
      cursor[Employee](ReadPreference.primary).
      // Coollect the results as a list
      collect[List]()
    }

    // everything's ok! Let's reply with a JsValue
    futureCitiesList.map { employees =>
      Ok(Json.toJson(employees))
    }
  }
   
  def findAllEmployees() = Action.async {
    // let's do our query
    val futureCitiesList: Future[List[Employee]] = employeesFuture.flatMap {
      // find all cities with name `name`
      _.find(Json.obj()).
      // perform the query and get a cursor of JsObject
      cursor[Employee](ReadPreference.primary).
      // Coollect the results as a list
      collect[List]()
    }

    // everything's ok! Let's reply with a JsValue
    futureCitiesList.map { employees =>
      Ok(Json.toJson(employees))
    }
  }
  
}


