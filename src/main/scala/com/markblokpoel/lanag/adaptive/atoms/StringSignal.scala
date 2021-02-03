package com.markblokpoel.lanag.adaptive.atoms

/** A signal with a String name
 *
 *  @param label the name of this signal
 */
case class StringSignal(label: String) {
  override def toString: String = label
}
