package users

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._

import org.joda.time.DateTime

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

  object UsersDAO extends HasDatabaseConfig[JdbcProfile] {
    protected implicit val app = play.api.Play.current
    protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]

    val users = TableQuery[UsersTable]

    def insert(user: User) = db.run(users += user)

    def insert(presenter: UserPresenter) = {
      val user = User(0, presenter.name)

      db.run(users += user)
    }

    def all = db.run(users.result)
  }
}
