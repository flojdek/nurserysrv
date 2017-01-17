package pl.malekrasnale.utils

import org.joda.time.{LocalDate, DateTimeZone, DateTime, LocalDateTime}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

object MongoDBUtil {
  implicit val localDateRead: Reads[LocalDate] = {
    (__ \ "$date").read[Long].map {
      date => new LocalDate(date, DateTimeZone.UTC)
    }
  }

  implicit val localDateWrite: Writes[LocalDate] = new Writes[LocalDate] {
    def writes(localDate: LocalDate): JsValue = Json.obj(
      "$date" -> localDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
    )
  }

  implicit val localDateTimeRead: Reads[LocalDateTime] = {
    (__ \ "$date").read[Long].map {
      dateTime => new LocalDateTime(dateTime, DateTimeZone.UTC)
    }
  }

  implicit val localDateTimeWrite: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(dateTime: LocalDateTime): JsValue = Json.obj(
      "$date" -> dateTime.toDateTime(DateTimeZone.UTC).getMillis
    )
  }

  implicit val dateTimeRead: Reads[DateTime] = {
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }
  }

  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }

  implicit val objectIdRead: Reads[BSONObjectID] = {
    (__ \ "$oid").read[String].map { oid =>
      BSONObjectID(oid)
    }
  }

  implicit val objectIdWrite: Writes[BSONObjectID] = new Writes[BSONObjectID] {
    def writes(objectId: BSONObjectID): JsValue = Json.obj(
      "$oid" -> objectId.stringify
    )
  }
}
