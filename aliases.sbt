import MyUtil._

addCommandAlias("l", "projects")
addCommandAlias("ll", "projects")
addCommandAlias("ls", "projects")
addCommandAlias("cd", "project")
addCommandAlias("root", "cd accepto")
addCommandAlias("c", "compile")
addCommandAlias("ca", "Test / compile")
addCommandAlias("t", "test")
addCommandAlias("r", "run")
addCommandAlias("rm", "main/run")
addCommandAlias("rs", "reStart")
addCommandAlias("s", "reStop")
addCommandAlias("star", "thankYouStars")
addCommandAlias(
  "styleCheck",
  "scalafmtSbtCheck; scalafmtCheckAll; scalafixAll --check",
)
addCommandAlias(
  "styleFix",
  "scalafixAll; scalafmtSbt; scalafmtAll",
)
addCommandAlias(
  "up2date",
  "reload plugins; dependencyUpdates; reload return; dependencyUpdates",
)

onLoadMessage +=
  s"""|
      |╭─────────────────────────────────╮
      |│     List of defined ${styled("aliases")}     │
      |├─────────────┬───────────────────┤
      |│ ${styled("l")} | ${styled("ll")} | ${styled("ls")} │ projects          │
      |│ ${styled("cd")}          │ project           │
      |│ ${styled("root")}        │ cd root           │
      |│ ${styled("c")}           │ compile           │
      |│ ${styled("ca")}          │ compile all       │
      |│ ${styled("t")}           │ test              │
      |│ ${styled("r")}           │ run               │
      |│ ${styled("rm")}          │ run main          │
      |│ ${styled("rs")}          │ reStart           │
      |│ ${styled("s")}           │ reStop            │
      |│ ${styled("star")}        │ thankYouStars     │
      |│ ${styled("styleCheck")}  │ fmt & fix check   │
      |│ ${styled("styleFix")}    │ fix then fmt      │
      |│ ${styled("up2date")}     │ dependencyUpdates │
      |╰─────────────┴───────────────────╯""".stripMargin
