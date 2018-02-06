package com.bonosludos

import com.github.tminglei.slickpg._
import org.json4s.{JValue, JsonMethods}
import org.json4s.native.Document
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait CustomPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgRangeSupport
  with PgHStoreSupport
  with PgSearchSupport
  with PgJson4sSupport
  with PgNetSupport
  with PgLTreeSupport {

  /// for json support
  override val pgjson = "jsonb"
  type DOCType = org.json4s.native.Document
  override val jsonMethods: JsonMethods[Document] = org.json4s.native.JsonMethods.asInstanceOf[JsonMethods[DOCType]]

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api: MyAPI.type = MyAPI

  // val plainAPI = new API with Json4sJsonPlainImplicits

  object MyAPI extends API with ArrayImplicits
    with DateTimeImplicits
    with JsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {
    implicit val strListTypeMapper: DriverJdbcType[List[String]] = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val json4sJsonArrayTypeMapper: DriverJdbcType[List[JValue]] =
      new AdvancedArrayJdbcType[JValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JValue](j => jsonMethods.compact(jsonMethods.render(j)))(v)
      ).to(_.toList)
  }

}

object CustomPostgresProfile extends CustomPostgresProfile