package dev.z
package accepto

// Define Newtypes
type OrganizationId = OrganizationId.Type
object OrganizationId extends Newtype[UUID]

object OrganizationName extends Newtype[String]
type OrganizationName = OrganizationName.Type

// Define Organization without `derives`
case class Organization(
  id: OrganizationId,
  name: OrganizationName,
)

// Provide explicit Eq and Show instances in the companion object
object Organization:
  given Eq[Organization] =
    Eq.fromUniversalEquals
  given Show[Organization] =
    Show.fromToString
