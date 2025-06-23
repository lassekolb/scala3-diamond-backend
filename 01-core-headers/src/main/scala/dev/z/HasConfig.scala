package dev.z

trait HasConfig[F[_], Config]:
  def config: F[Config]
