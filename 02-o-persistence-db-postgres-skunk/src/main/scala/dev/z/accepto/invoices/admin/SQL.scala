package dev.z
package accepto
package invoices
package admin

import skunk.*
import skunk.implicits.*

object SQL:
  import dev.z.accepto.invoices.SQL.*
  lazy val insertInvoice: Command[Invoice] =
    sql"""
      INSERT INTO invoices (id, organization_id, amount, currency_code, buyer, seller, date, due_date)
      VALUES ${encoder.values}
    """.command

  def insertInvoices(batchSize: Int): Command[List[Invoice]] =
    sql"""
      INSERT INTO invoices (id, organization_id, amount, currency_code, buyer, seller, date, due_date)
      VALUES ${encoder.values.list(batchSize)}
    """.command
