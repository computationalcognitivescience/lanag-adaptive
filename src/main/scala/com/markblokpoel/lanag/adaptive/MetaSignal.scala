package com.markblokpoel.lanag.adaptive

case class MetaSignal(signal: Option[StringSignal]) {
  def understood: Boolean = signal.isEmpty
}
