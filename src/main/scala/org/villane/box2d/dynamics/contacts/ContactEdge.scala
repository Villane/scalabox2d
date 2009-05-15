package org.villane.box2d.dynamics.contacts

/**
 * A contact edge is used to connect bodies and contacts together
 * in a contact graph where each body is a node and each contact
 * is an edge. A contact edge belongs to a doubly linked list
 * maintained in each attached body. Each contact has two contact
 * nodes, one for each attached body.
 */
case class ContactEdge(
  /** Provides quick access to the other body attached. */
  other: Body,
  /** The contact. */
  contact: Contact
)
