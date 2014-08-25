package ghtorrent

import java.sql.Timestamp

import org.joda.time.{DateTime, DateTimeZone}

import scala.slick.driver.MySQLDriver.simple._

object DateTimeMapper {
  implicit def dateTime =
    MappedColumnType.base[DateTime, Timestamp](
      dt => convert(dt),
      ts => convert(ts)
    )

  def convert(ts: Timestamp): DateTime = {
    new DateTime(ts.getTime).toLocalDateTime.toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
  }

  def convert(dt: DateTime): Timestamp = {
    new Timestamp(dt.withZone(DateTimeZone.UTC).toLocalDateTime.toDateTime.getMillis)
  }
}
