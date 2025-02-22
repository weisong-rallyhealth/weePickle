package com.rallyhealth.weepickle.v0
import com.rallyhealth.weepickle.v0.implicits.MacroImplicits
import com.rallyhealth.weepickle.v0.core.Visitor

trait JsReaderWriters extends com.rallyhealth.weepickle.v0.core.Types with MacroImplicits{

  implicit val JsValueR: Reader[com.rallyhealth.weejson.v0.Value] = new Reader.Delegate(com.rallyhealth.weejson.v0.Value)

  implicit def JsObjR: Reader[com.rallyhealth.weejson.v0.Obj] = JsValueR.narrow[com.rallyhealth.weejson.v0.Obj]
  implicit def JsArrR: Reader[com.rallyhealth.weejson.v0.Arr] = JsValueR.narrow[com.rallyhealth.weejson.v0.Arr]
  implicit def JsStrR: Reader[com.rallyhealth.weejson.v0.Str] = JsValueR.narrow[com.rallyhealth.weejson.v0.Str]
  implicit def JsNumR: Reader[com.rallyhealth.weejson.v0.Num] = JsValueR.narrow[com.rallyhealth.weejson.v0.Num]
  implicit def JsBoolR: Reader[com.rallyhealth.weejson.v0.Bool] = JsValueR.narrow[com.rallyhealth.weejson.v0.Bool]
  implicit def JsTrueR: Reader[com.rallyhealth.weejson.v0.True.type] = JsValueR.narrow[com.rallyhealth.weejson.v0.True.type]
  implicit def JsFalseR: Reader[com.rallyhealth.weejson.v0.False.type] = JsValueR.narrow[com.rallyhealth.weejson.v0.False.type]
  implicit def JsNullR: Reader[com.rallyhealth.weejson.v0.Null.type] = JsValueR.narrow[com.rallyhealth.weejson.v0.Null.type]


  implicit def JsObjW: Writer[com.rallyhealth.weejson.v0.Obj] = JsValueW.narrow[com.rallyhealth.weejson.v0.Obj]
  implicit def JsArrW: Writer[com.rallyhealth.weejson.v0.Arr] = JsValueW.narrow[com.rallyhealth.weejson.v0.Arr]
  implicit def JsStrW: Writer[com.rallyhealth.weejson.v0.Str] = JsValueW.narrow[com.rallyhealth.weejson.v0.Str]
  implicit def JsNumW: Writer[com.rallyhealth.weejson.v0.Num] = JsValueW.narrow[com.rallyhealth.weejson.v0.Num]
  implicit def JsBoolW: Writer[com.rallyhealth.weejson.v0.Bool] = JsValueW.narrow[com.rallyhealth.weejson.v0.Bool]
  implicit def JsTrueW: Writer[com.rallyhealth.weejson.v0.True.type] = JsValueW.narrow[com.rallyhealth.weejson.v0.True.type]
  implicit def JsFalseW: Writer[com.rallyhealth.weejson.v0.False.type] = JsValueW.narrow[com.rallyhealth.weejson.v0.False.type]
  implicit def JsNullW: Writer[com.rallyhealth.weejson.v0.Null.type] = JsValueW.narrow[com.rallyhealth.weejson.v0.Null.type]
  implicit val JsValueW: Writer[com.rallyhealth.weejson.v0.Value] = new Writer[com.rallyhealth.weejson.v0.Value] {
    def write0[R](out: Visitor[_, R], v: com.rallyhealth.weejson.v0.Value): R = com.rallyhealth.weejson.v0.WeeJson.transform(v, out)
  }
}
