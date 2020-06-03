package com.markblokpoel.lanag.adaptive

case class MetaSignal(signal: Option[StringSignal]) {
  def understood: Boolean = signal.isEmpty
  def getSignal: StringSignal = signal.get

  override def toString: String = if (understood)
    "I understand"
  else
    getSignal.toString
}
