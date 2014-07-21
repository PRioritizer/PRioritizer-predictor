package util

import org.joda.time.DateTime

object Extensions {
  implicit class RichDateTime(dateTime: DateTime) {
    def isBetween(after: DateTime, before: DateTime): Boolean =
      dateTime.isAfter(after) && dateTime.isBefore(before)
  }
}
