package dev.z

sealed trait Retriable derives Show

object Retriable:
  case object CreateInvoice extends Retriable
