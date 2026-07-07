package infrastructure

import domain.model.StockOperation
import java.io.File

class CsvStockReader {

    fun read(filePath: String, block: (StockOperation) -> Unit) {
        val file = File(filePath)
        require(file.exists()) { "File does not exist: $filePath" }

        file.useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank()) return@forEach

                try {
                    val operation = parseLine(line)
                    block(operation)
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Invalid CSV line: '$line'", e)
                }
            }
        }
    }

    private fun parseLine(line: String): StockOperation {
        val parts = line.split(";").map { it.trim() }

        return when (parts.size) {
            INCOME_COLUMNS -> StockOperation.Income(
                groupId = parts[0],
                productId = parts[1],
                count = parts[2].toLong()
            )

            SALE_COLUMNS -> StockOperation.Sale(
                groupId = parts[0],
                count = parts[1].toLong()
            )

            else -> throw IllegalArgumentException("Unknown format line: $line")
        }
    }

    private companion object {
        const val INCOME_COLUMNS = 3
        const val SALE_COLUMNS = 2
    }
}