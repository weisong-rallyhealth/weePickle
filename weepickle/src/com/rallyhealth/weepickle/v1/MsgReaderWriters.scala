package com.rallyhealth.weepickle.v0

import com.rallyhealth.weepack.v0.WeePack
import com.rallyhealth.weepickle.v0.core.Visitor
import com.rallyhealth.weepickle.v0.implicits.MacroImplicits

trait MsgReaderWriters extends com.rallyhealth.weepickle.v0.core.Types with MacroImplicits{
  implicit val MsgValueR: Reader[com.rallyhealth.weepack.v0.Msg] = new Reader.Delegate(com.rallyhealth.weepack.v0.Msg)

  implicit val MsgValueW: Writer[com.rallyhealth.weepack.v0.Msg] = new Writer[com.rallyhealth.weepack.v0.Msg] {
    def write0[R](out: Visitor[_, R], v: com.rallyhealth.weepack.v0.Msg): R = WeePack.transform(v, out)
  }
}
