package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import services.Pet

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

  def show(id : Long) = Action {
    Ok(views.html.pet(id))
  }

  def create = TODO

  def update(id : Long) = TODO

  def search = Action { implicit request =>
    Ok(views.html.search(petSearch.fill((None, 0L, None)), Nil, Pet.findPets((None, 0, None))))
  }

  def performSearch = Action { implicit request =>
    petSearch.bindFromRequest().fold(
      hasErrors => Ok(views.html.search(petSearch, hasErrors.globalErrors, Set[Pet]()))
      , success => Ok(views.html.search(petSearch, Nil, Pet.findPets(petSearch.get))))
  }

}