package com.rallyhealth.weepickle.v0.implicits

import scala.annotation.StaticAnnotation

/**
  * May be used on either:
  * 1. subclasses to override [[discriminator]] values (default: FQCN)
  * 2. class fields to override field name.
  *
  * @see http://www.lihaoyi.com/upickle/#CustomKeys
  */
case class key(s: String) extends StaticAnnotation
