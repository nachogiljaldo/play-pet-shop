package controllers

import play.api.mvc._

object HelpApplication extends Controller {
  
  def index = Action {
    Ok(views.html.help("Your new application is ready."))
  }
  
}