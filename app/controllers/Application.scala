package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import users.Users.{ UsersDAO, UserPresenter }
import cocktails.Cocktails.CocktailPresenter

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import com.google.inject.Inject

import scala.concurrent.duration._

import operators.{ CocktailManager, DrinkResult }

class Application @Inject() (operator: ActorSystem) extends Controller {
  implicit val timeout = Timeout(2 seconds)

  val cocktailForm = Form(
    mapping("numberOfDrinks" -> number,
            "alcoholicsIds" -> seq(number))
            (CocktailPresenter.apply)(CocktailPresenter.unapply)
  )

  val userForm = Form(
    mapping("name" -> text)(UserPresenter.apply)(UserPresenter.unapply)
  )

  def index = Action.async {
    val users = UsersDAO.all

    users.map(theUsers => Ok(views.html.index(theUsers)))
  }

  def addUser = Action {
    Ok(views.html.user(userForm))
  }

  def saveUser = Action { implicit request =>
    val presenter = userForm.bindFromRequest.get
    UsersDAO.insert(presenter)

    Redirect(routes.Application.index)
  }

  def buildCocktails = Action.async {
    val users = UsersDAO.all

    users.map(theUsers => Ok(views.html.cocktail(cocktailForm, theUsers)))
  }

  def saveCocktails = Action.async { implicit request =>
    val data = cocktailForm.bindFromRequest.get
    val manager = operator.actorOf(Props(classOf[CocktailManager], data.numberOfDrinks, data.alcoholicsIds))
    manager ! "pour"
    val users = UsersDAO.all

    for {
      response <- (manager ? "drinks").mapTo[Seq[DrinkResult]]
      users <- users
    } yield Ok(views.html.results(response, users))
  }
}
