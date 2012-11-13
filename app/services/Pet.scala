package services

import java.util.{Date, Calendar}
import play.api.Play.current
import anorm._
import play.api.db.DB
import ResultSetParser._

/**
 * @author: jjaldo
 */
class Pet(var id: Long, var name : String, var price : Long, var birthday : Calendar, var petTypeId : Int) {

}

object Pet {
  def basicQuery = "SELECT id, name, price, birthday, pet_type_id FROM pets WHERE "
  def insertQuery = "INSERT INTO pets (name, price, birthday, pet_type_id) VALUES({name}, {price}, {birthday}, {typeId});"
  def updateQuery = "UPDATE pets SET name = {name}, price = {price}, birthday = {birthday}, pet_type_id = {typeId} WHERE id = {id};"

  private def rowToPet(row : Row) : Pet = {
    val calendar = Calendar.getInstance()
    calendar.setTime(row[Date]("birthday"))
    new Pet(row[Int]("id"), row[String]("name"), row[Long]("price"), calendar, row[Int]("pet_type_id"))
  }

  def findPets(searchParams : (Option[Int], Long, Option[Long])) : Set[Pet] = {
    val query = (searchParams match {
      case (None, minPrice, Some(maxPrice)) => { SQL(basicQuery + "price BETWEEN {minPrice} AND {maxPrice}")
                                                .on("maxPrice" -> maxPrice,
                                                    "minPrice" -> minPrice)
                                               }
      case (None, minPrice, None) => { SQL(basicQuery + "price >= {minPrice}")
                                          .on("minPrice" -> minPrice)
                                     }
      case (Some(typeId), minPrice, None) => { SQL(basicQuery + "type_id = {typeId} AND price >= {minPrice}")
                                                .on("typeId" -> typeId,
                                                "minPrice" -> minPrice)
                                             }
      case (Some(typeId), minPrice, Some(maxPrice)) => { SQL(basicQuery + "type_id = {typeId} AND price BETWEEN {minPrice} AND {maxPrice}")
        .on("minPrice" -> minPrice)
      }
    })
    DB.withConnection({ implicit c =>
      query().map(rowToPet).toSet
    })
  }



  def persistPet(pet : (Option[Long], Int, Long, String, Date)) : Pet = {
    pet match {
      case (None, typeId, price, name, birthday) => {
        DB.withConnection(implicit c => SQL(insertQuery)
          .on("typeId" -> typeId, "price" -> price, "name" -> name, "birthday" -> birthday).executeInsert()) match {
          case Some(id) => findById(id)
          case _ => throw new Exception("Could not create the object.")
        }
      }
      case (Some(id), typeId, price, name, birthday) => {
        DB.withConnection(implicit c => SQL(updateQuery)
          .on("typeId" -> typeId, "price" -> price, "name" -> name, "id" -> id, "birthday" -> birthday).executeUpdate)
        findById(id)
      }
    }
  }

  def findById(id : Long) : Pet = {
    DB.withConnection({ implicit c =>
      SQL(basicQuery + "id = {id}")
        .on("id" -> id)()
        .map(rowToPet)
      }).head
  }
}
