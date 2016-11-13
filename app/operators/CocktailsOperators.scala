package operators

import akka.actor.{ Actor, Props }
import scala.util.Random

import cocktails.Cocktails.{ Cocktail, CocktailDAO }

case class DrinkResult(id: Long, results: Seq[Int])

class CocktailManager(numberOfDrinks: Int, alcoholicsIds: Seq[Int]) extends Actor {
  import CocktailManager.Cocktails

  val StartPoint = 1
  private val countForEach = ((numberOfDrinks: Float) / alcoholicsIds.distinct.length).round
  private val batches = Random.shuffle(Range(StartPoint, numberOfDrinks + 1).toList).grouped(countForEach).toList
  private val result: Seq[DrinkResult] = {
    alcoholicsIds.distinct.zipWithIndex.map {
      case (id, index) => DrinkResult(id, batches(index))
    }
  }

  def receive = {
    case "drinks" => sender ! result
    case "pour" =>
      alcoholicsIds.distinct.zipWithIndex.foreach {
        case(id, index) =>
          context.actorOf(Props[PersonCocktailBartender]) ! Cocktails(id, batches(index))
      }
  }
}

class PersonCocktailBartender extends Actor {
  import CocktailManager.Cocktails

  def receive = {
    case Cocktails(id, cocktailNumbers) =>
      val records = cocktailNumbers.map { number => Cocktail(id, number) }
      CocktailDAO.insert(records)

      context stop self
  }
}

object CocktailManager {
  case class Cocktails(alcoholicId: Long, cocktailIds: Seq[Int])
}
