package infrastructure

import domain.model.StockItem
import java.io.File

class CsvStockWriter {

    fun write(filePath: String, stocks: List<StockItem>) {
        val file = File(filePath)

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            stocks.forEach { stock ->
                writer.write("${stock.groupId};${stock.productId};${stock.count}")
                writer.newLine()
            }
        }
    }
}