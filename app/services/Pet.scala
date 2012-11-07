package services

import java.util.{Date, Calendar}
import play.api.Play.current
import anorm._
import play.api.db.DB

/**
 * @author: jjaldo
 */
class Pet(var id: Int, var name : String, var price : Long, var birthday : Calendar, var petTypeId : Int) {

}

object Pet {
  def basicQuery = "SELECT id, name, price, birthday, pet_type_id FROM pets WHERE "

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

  def findById(id : Long) : Pet = {
    DB.withConnection({ implicit c =>
      SQL(basicQuery + "id = {id}")
        .on("id" -> id)()
        .map(rowToPet)
      }).head
  }
}
