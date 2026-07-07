import infrastructure.CsvStockReader
import infrastructure.CsvStockWriter
import infrastructure.StockInventoryImpl

fun main(args: Array<String>) {
    require(args.size == 2) {
        "Usage: <input.csv> <output.csv>"
    }

    val inputFile = args[0]
    val outputFile = args[1]

    val reader = CsvStockReader()
    val writer = CsvStockWriter()
    val stockInventory = StockInventoryImpl()

    try {
        reader.read(inputFile) { operation ->
            stockInventory.process(operation)
        }

        writer.write(outputFile, stockInventory.currentStocks())
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}