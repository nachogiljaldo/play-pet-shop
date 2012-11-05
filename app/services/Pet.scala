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
  def findPets(searchParams : (Option[Int], Long, Option[Long])) : Set[Pet] = {
    val sql = "SELECT id, name, price, birthday, pet_type_id FROM pets WHERE "
    val query = (searchParams match {
      case (None, minPrice, Some(maxPrice)) => { SQL(sql + "price BETWEEN {minPrice} AND {maxPrice}")
                                                .on("maxPrice" -> maxPrice,
                                                    "minPrice" -> minPrice)
                                               }
      case (None, minPrice, None) => { SQL(sql + "price >= {minPrice}")
                                          .on("minPrice" -> minPrice)
                                     }
      case (Some(typeId), minPrice, None) => { SQL(sql + "type_id = {typeId} AND price >= {minPrice}")
                                                .on("typeId" -> typeId,
                                                "minPrice" -> minPrice)
                                             }
      case (Some(typeId), minPrice, Some(maxPrice)) => { SQL(sql + "type_id = {typeId} AND price BETWEEN {minPrice} AND {maxPrice}")
        .on("minPrice" -> minPrice)
      }
    })
    DB.withConnection({ implicit c =>
      query().map(row => {
          val calendar = Calendar.getInstance()
          calendar.setTime(row[Date]("birthday"))
          new Pet(row[Int]("id"), row[String]("name"), row[Long]("price"), calendar, row[Int]("pet_type_id"))
        }).toSet
    })
  }
}
