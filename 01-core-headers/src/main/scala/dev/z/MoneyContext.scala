package dev.z

import squants.market.*

given MoneyContext = defaultMoneyContext

// given defaultMoneyContext: MoneyContext =
//   MoneyContext(
//     currencies = Set(USD, EUR, GBP, JPY),
//     defaultCurrency = USD,
//     rates = Seq.empty,
//   )
