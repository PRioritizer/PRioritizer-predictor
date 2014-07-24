package util

import org.joda.time.DateTime

object Extensions {
  implicit class RichDateTime(dateTime: DateTime) {
    def isWithin(after: DateTime, before: DateTime): Boolean =
      dateTime.isAfter(after) && dateTime.isBefore(before)

    def isBetween(after: DateTime, before: DateTime): Boolean = isWithin(after, before)

    def isWithin(window: Window): Boolean =
      dateTime.isWithin(window.start, window.end)

    def isBetween(window: Window): Boolean = isWithin(window)
  }
}
