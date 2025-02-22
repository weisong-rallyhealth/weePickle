package com.rallyhealth.weejson.v0

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import com.rallyhealth.weepickle.v0.core.{ArrVisitor, ObjVisitor, Visitor}
object JNumIndexCheckFacade extends JsVisitor[Boolean, Boolean] {
  def visitArray(length: Int, index: Int)  = new ArrVisitor[Boolean, Boolean] {
    var failed = false
    def subVisitor = JNumIndexCheckFacade
    def visitKey(s: CharSequence, index: Int): Unit = ()
    def visitValue(v: Boolean, index: Int): Unit = {
      if (!v) failed = true
    }
    def visitEnd(index: Int): Boolean = !failed
  }
  def visitObject(length: Int, index: Int) = new ObjVisitor[Boolean, Boolean] {
    var failed = false
    def subVisitor = JNumIndexCheckFacade
    def visitValue(v: Boolean, index: Int): Unit = {
      if (!v) failed = true
    }
    def visitEnd(index: Int): Boolean = !failed

    def visitKey(index: Int) = JNumIndexCheckFacade

    def visitKeyValue(v: Any): Unit = ()
  }

  def visitNull(index: Int): Boolean = true
  def visitFalse(index: Int): Boolean = true
  def visitTrue(index: Int): Boolean = true
  def visitFloat64StringParts(s: CharSequence, decIndex: Int, expIndex: Int, index: Int): Boolean = {
    val input = s.toString
    val inputDecIndex = input.indexOf('.')
    val inputExpIndex = if (input.indexOf('e') == -1) input.indexOf("E") else input.indexOf('e')

    decIndex == inputDecIndex && expIndex == inputExpIndex
  }
  def visitString(s: CharSequence, index: Int): Boolean = true
}


class JNumIndexCheck extends PropSpec with Matchers with PropertyChecks {

  property("visitFloat64StringParts provides the correct indices with parseFromString") {
    forAll { (value: BigDecimal) =>
      val json = s"""{ "num": ${value.toString} }"""
      StringParser.transform(json, JNumIndexCheckFacade) shouldBe true
    }
  }

  property("visitFloat64StringParts provides the correct indices with parseFromByteBuffer") {
    forAll { (value: BigDecimal) =>
      val json = s"""{ "num": ${value.toString} }"""
      val bb = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8))
      ByteBufferParser.transform(bb, JNumIndexCheckFacade) shouldBe true
    }
  }

  property("visitFloat64StringParts provides the correct indices at the top level with parseFromString") {
    forAll { (value: BigDecimal) =>
      StringParser.transform(value.toString, JNumIndexCheckFacade) shouldBe true
    }
  }

  property("visitFloat64StringParts provides the correct indices at the top level with parseFromByteBuffer") {
    forAll { (value: BigDecimal) =>
      val bb = ByteBuffer.wrap(value.toString.getBytes(StandardCharsets.UTF_8))
      ByteBufferParser.transform(bb, JNumIndexCheckFacade) shouldBe true
    }
  }
}
