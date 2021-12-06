package com.markblokpoel.lanag.adaptive.atoms

/** A meta signal, contains a signal and an 'understood' Boolean
  *
  * @param signal the string signal that is communicated
  */
case class MetaSignal(signal: Option[StringSignal]) {
  def understood: Boolean = signal.isEmpty
  def getSignal: StringSignal = signal.get

  override def toString: String =
    if (understood)
      "I understand"
    else
      getSignal.toString
}
