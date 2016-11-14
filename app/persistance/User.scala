package users

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._

import org.joda.time.DateTime

import cocktails.Cocktails.CocktailsTable

import scala.concurrent.{ ExecutionContext, Future }

object Users {
  import com.github.tototoshi.slick.H2JodaSupport._

  case class UserPresenter(name: String)

  case class User(id: Long = 0, name: String, createdAt: DateTime = DateTime.now)

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def id           = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name         = column[String]("name")
    def createdAt    = column[DateTime]("created_at")
    def *            = (id,name, createdAt) <> (User.tupled, User.unapply _)
  }

  case class UserWithCocktails(id: Long = 0, name: String, createdAt: DateTime = DateTime.now, cocktailNumber: Option[Int])

  object UsersDAO extends HasDatabaseConfig[JdbcProfile] {
    protected implicit val app = play.api.Play.current
    protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]

    val users = TableQuery[UsersTable]
    val cocktails = TableQuery[CocktailsTable]

    def insert(user: User) = db.run(users += user)

    def insert(presenter: UserPresenter) = {
      val user = User(0, presenter.name)

      db.run(users += user)
    }

    def allWithCocktails(implicit ec: ExecutionContext): Future[Seq[UserWithCocktails]] = {
      val join = for {
        (user, cocktail) <- users joinLeft cocktails on(_.id === _.alcoholicId)
      } yield (user, cocktail)

      val records = join.groupBy { case (user, cocktail) => (user.id, user.name, user.createdAt) }.map {
        case ((id, name, createdAt), union) => (id, name, createdAt, union.map(_._2).map(_.map(_.cocktailNumber).getOrElse(0)).map { result =>
          Case If result > 0 Then 1 Else 0
        }.sum)
      }

      db.run(records.sortBy(record => record._4.desc).result).map(_.map(UserWithCocktails.tupled))
    }

    def all: Future[Seq[User]] = db.run(users.result)
  }
}
