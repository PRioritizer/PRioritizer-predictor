package ghtorrent

import com.mongodb._
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId

class MongoDatabase(host: String, port: Int, username: String, password: String, databaseName: String, collectionName: String) {
  private var client: MongoClient = _
  private var database: DB = _
  private var collection: DBCollection = _

  def open(): Unit = {
    val server = new ServerAddress(host, port)
    client = if (username != null && username.nonEmpty) {
      val credential = MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray)
      new MongoClient(server, java.util.Arrays.asList(credential))
    } else {
      new MongoClient(server)
    }

    client.setReadPreference(ReadPreference.secondaryPreferred())
    database = client.getDB(databaseName)
    collection = database.getCollection(collectionName)
  }

  def getObject(objectId: String, select: List[String]) : Map[String, String] = {
    if (objectId == "")
      return Map()

    val query = MongoDBObject("_id" -> new ObjectId(objectId))

    val fields = new BasicDBObject()
    select.foreach(f => fields.put(f, 1))

    val result = collection.findOne(query, fields)
    select
      .map(f => getField(result, f).map(v => (f, v)))
      .flatten
      .toMap
  }

  private def getField(obj: DBObject, fullPath: String): Option[String] = {
    def iteration(x: AnyRef, path: Array[String]): Option[String] = {
      x match {
        case o: DBObject => iteration(o.get(path.head), path.tail)
        case s: String => Some(s)
        case i: Integer => Some(i.toString)
        case _ => None
      }
    }
    iteration(obj, fullPath.split("""\."""))
  }

  def close(): Unit = {
    if (client != null)
      client.close()
  }
}
