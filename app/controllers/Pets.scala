package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import services.Pet
import java.util.Calendar

object Pets extends Controller {

  val petSearch = Form(
    tuple(
      "typeId" -> optional(number),
      "minPrice" -> longNumber,
      "maxPrice" -> optional(longNumber)
    ) verifying("Invalid price range", result => result match {
      case (typeId, minPrice, None) => true
      case (typeId, minPrice, Some(maxPrice)) => maxPrice >= minPrice
    })
  )

  val petCreation = Form(
    tuple(
      "id" -> optional(longNumber),
      "typeId" -> number,
      "price" -> longNumber,
      "name" -> text,
      "birthday" -> date
    )
  )

  def show(id : Long) = Action {
    Ok(views.html.pet(Pet.findById(id)))
  }

  def create = Action {
    Ok(views.html.create(petCreation.fill((None, 0, -1L, "", Calendar.getInstance().getTime))))
  }

  def doCreate = Action { implicit request =>
    petCreation.bindFromRequest().fold(
      hasErrors => create.apply(request)
      , success => Ok(views.html.pet(Pet.persistPet(success))))
  }

  def update(id : Long) = TODO

  def search = Action { implicit request =>
    Ok(views.html.search(petSearch.fill((None, 0L, None)), Nil, Pet.findPets((None, 0, None))))
  }

  def performSearch = Action { implicit request =>
    petSearch.bindFromRequest().fold(
      hasErrors => Ok(views.html.search(petSearch, hasErrors.globalErrors, Set[Pet]()))
      , success => Ok(views.html.search(petSearch, Nil, Pet.findPets(success))))
  }

}