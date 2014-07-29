package ghtorrent

import scala.slick.driver.MySQLDriver.simple._
import org.joda.time.{DateTimeZone, DateTime}
import java.sql.Timestamp

object DateTimeMapper {
  implicit def dateTime =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.withZone(DateTimeZone.UTC).toLocalDateTime.toDateTime.getMillis),
      ts => new DateTime(ts.getTime).toLocalDateTime.toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
    )
}
