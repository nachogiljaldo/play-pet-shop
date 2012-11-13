package services

import anorm._
import play.api.db.DB
import play.api.Play.current

/**
 * @author: jjaldo
 */
class PetType(var id : Long, var description : String) {
  implicit def PetTypeToTuple = (id.toString, description)
}

object PetType {
  val baseQuery = "SELECT id, name FROM pets_types"

  private def rowToPetType(row : Row) : PetType = {
    new PetType(row[Int]("id"), row[String]("name"))
  }

  def getAllPetTypes() : List[PetType] = {
    DB.withConnection({implicit c =>
      SQL(baseQuery)().map(rowToPetType).toList
    })
  }

  def getPetTypeById(id : Long) : PetType = {
    DB.withConnection({implicit c =>
      rowToPetType(SQL(baseQuery + " WHERE id = {id}").on("id" -> id).single())
    })
  }
}
