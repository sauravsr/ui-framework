package models


import play.api.libs.json.Json


case class Employee(FirstName : String, LastName : String, Salary : Int)

object Employee {
  
  implicit val formatter = Json.format[Employee]
}