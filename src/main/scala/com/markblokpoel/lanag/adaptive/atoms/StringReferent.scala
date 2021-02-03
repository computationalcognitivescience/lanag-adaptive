package com.markblokpoel.lanag.adaptive.atoms

/** A referent with a String name
 *
 *  @param label the name of this referent
 */
case class StringReferent(label: String) {
  override def toString: String = label
}
