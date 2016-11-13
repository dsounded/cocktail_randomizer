package cocktails

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._

import org.joda.time.DateTime

object Cocktails {
  import com.github.tototoshi.slick.H2JodaSupport._

  case class CocktailPresenter(numberOfDrinks: Int, alcoholicsIds: Seq[Int])

  case class Cocktail(alcoholicId: Long, cocktailNumber: Int, createdAt: DateTime = DateTime.now)

  class CocktailsTable(tag: Tag) extends Table[Cocktail](tag, "cocktails") {
    def alcoholicId    = column[Long]("alcoholic_id")
    def cocktailNumber = column[Int]("cocktail_number")
    def createdAt      = column[DateTime]("created_at")
    def *              = (alcoholicId, cocktailNumber, createdAt) <> (Cocktail.tupled, Cocktail.unapply _)
  }

  object CocktailDAO extends HasDatabaseConfig[JdbcProfile] {
    protected implicit val app = play.api.Play.current
    protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]

    val cocktails = TableQuery[CocktailsTable]

    def insert(cocktailBatch: Seq[Cocktail]) = {
      db.run(cocktails ++= cocktailBatch)
    }
  }
}
