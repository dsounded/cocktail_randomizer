package operators

import akka.actor.{ Actor, Props }
import scala.util.Random

import cocktails.Cocktails.{ Cocktail, CocktailDAO }

case class DrinkResult(id: Long, results: Seq[Int])

class CocktailManager(numberOfDrinks: Int, alcoholicsIds: Seq[Int]) extends Actor {
  val StartPoint = 1
  private val countForEach = ((numberOfDrinks: Float) / alcoholicsIds.distinct.length).round
  private val batches = Random.shuffle(Range(StartPoint, numberOfDrinks + 1).toList).grouped(countForEach).toList
  private val result: Seq[DrinkResult] = {
    alcoholicsIds.distinct.zipWithIndex.map {
      case (id, index) => DrinkResult(id, batches(index))
    }
  }

  def receive = {
    case "drinks" =>
      sender ! result

      context stop self
    case "pour" =>
      result.foreach(drinks => context.actorOf(Props[PersonCocktailBartender]) ! drinks)
  }
}

class PersonCocktailBartender extends Actor {
  def receive = {
    case DrinkResult(id, cocktailNumbers) =>
      val records = cocktailNumbers.map { number => Cocktail(id, number) }
      CocktailDAO.insert(records)

      context stop self
  }
}
