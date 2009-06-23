package org.villane.box2d.collision.broadphase

trait PairListener {
  // This should return the new pair user data. It is okay if the
  // user data is null.
  def pairAdded(proxyUserData1: AnyRef, proxyUserData2: AnyRef): AnyRef

  // This should free the pair's user data. In extreme circumstances, it is
  // possible
  // this will be called with null pairUserData because the pair never
  // existed.
  def pairRemoved(proxyUserData1: AnyRef, proxyUserData2: AnyRef, pairUserData: AnyRef)
}
