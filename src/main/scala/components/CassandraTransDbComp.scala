package components

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import db.SenzCassandraCluster
import protocols.{Trans, Agent}

/**
 * Created by eranga on 2/2/16
 */
trait CassandraTransDbComp extends TransDbComp {

  this: SenzCassandraCluster =>

  val transDb = new CassandraTransDB

  class CassandraTransDB extends TransDb {

    def init() = {
      // query to create agent
      val sqlCreateTableAgent = "CREATE TABLE IF NOT EXISTS agent (username TEXT PRIMARY KEY, branch TEXT);"

      // queries to create trans
      val sqlCreateTableTrans = "CREATE TABLE IF NOT EXISTS trans (agent TEXT, timestamp TEXT, account TEXT, amount TEXT, status TEXT, PRIMARY KEY(agent, timestamp));"
      val sqlCreateIndexTransStatus = "CREATE INDEX trans_status on trans(status);"
    }

    override def createAgent(agent: Agent) = {
      // insert query
      val statement = QueryBuilder.insertInto("agent")
        .value("name", agent.username)
        .value("branch", agent.branch)

      session.execute(statement)
    }

    override def getAgent(username: String): Agent = {
      // select query
      val selectStmt = select().all()
        .from("agent")
        .where(QueryBuilder.eq("name", username))
        .limit(1)

      val resultSet = session.execute(selectStmt)
      val row = resultSet.one()

      Agent(row.getString("name"), row.getString("branch"))
    }

    override def createTrans(trans: Trans) = {
      // insert query
      val statement = QueryBuilder.insertInto("trans")
        .value("agent", trans.agent)
        .value("timestamp", trans.timestamp)
        .value("account", trans.account)
        .value("amount", trans.amount)
        .value("status", trans.status)

      session.execute(statement)
    }

    override def updateTrans(trans: Trans) = {
      // update query
      val statement = QueryBuilder.update("trans")
        .`with`(set("status", trans.status))
        .where(QueryBuilder.eq("timestamp", trans.timestamp)).and(QueryBuilder.eq("agent", trans.agent))

      session.execute(statement)
    }

    override def getTrans(agent: String, timestamp: String): Trans = {
      // select query
      val selectStmt = select().all()
        .from("trans")
        .where(QueryBuilder.eq("agent", "234212")).and(QueryBuilder.eq("timestamp", "w234234"))
        .limit(1)

      val resultSet = session.execute(selectStmt)
      val row = resultSet.one()

      Trans(row.getString("agent"), row.getString("timestamp"), row.getString("account"), row.getString("amount"), row.getString("status"))
    }
  }

}