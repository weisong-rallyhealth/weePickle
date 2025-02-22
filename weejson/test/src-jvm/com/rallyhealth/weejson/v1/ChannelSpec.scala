package com.rallyhealth.weejson.v0

import org.scalatest._
import com.rallyhealth.weepickle.v0.core.NoOpVisitor

class ChannelSpec extends PropSpec with Matchers {

  property("large strings in files are ok") {
    val M = 1000000
    val q = "\""
    val big = q + ("x" * (40 * M)) + q
    val bigEscaped = q + ("\\\\" * (20 * M)) + q

    TestUtil.withTemp(big) { t =>
      scala.util.Try(Readable.fromFile(t).transform(NoOpVisitor)).isSuccess shouldBe true
    }

    TestUtil.withTemp(bigEscaped) { t =>
      scala.util.Try(Readable.fromFile(t).transform(NoOpVisitor)).isSuccess shouldBe true
    }
  }
}
