package org.villane.box2d.dynamics.joints

import dynamics.Body

class JointDef {
  var body1: Body = null
  var body2: Body = null
  var collideConnected = false
  var userData: AnyRef = null
}
