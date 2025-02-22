package com.rallyhealth.weejson.v0

import org.scalatest._
import org.scalatest.prop._

class CharBuilderSpec extends PropSpec with Matchers with PropertyChecks {

  property("append") {
    forAll { xs: List[Char] =>
      val builder = new com.rallyhealth.weejson.v0.util.CharBuilder
      xs.foreach(builder.append)
      builder.makeString shouldBe xs.mkString
    }
  }

  property("extend") {
    forAll { xs: List[String] =>
      val builder = new com.rallyhealth.weejson.v0.util.CharBuilder
      xs.foreach(builder.extend)
      builder.makeString shouldBe xs.mkString
    }
  }
}
