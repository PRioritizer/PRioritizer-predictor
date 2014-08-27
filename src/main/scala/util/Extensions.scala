package util

import java.io.{PrintStream, ByteArrayOutputStream}

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

  implicit class EnrichException(ex: Throwable) {
    def stackTraceToString: String = {
      val output = new ByteArrayOutputStream()
      val stream = new PrintStream(output)
      ex.printStackTrace(stream)
      output.toString("UTF-8").trim
    }
  }
}
