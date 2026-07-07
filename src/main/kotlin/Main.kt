import infrastructure.CsvStockReader
import infrastructure.CsvStockWriter
import infrastructure.StockInventoryImpl


private const val INPUT_FILE = "input.csv"
private const val OUTPUT_FILE = "output.csv"

fun main() {
    val reader = CsvStockReader()
    val writer = CsvStockWriter()
    val stockInventory = StockInventoryImpl()

    try {
        reader.read(INPUT_FILE) { operation ->
            stockInventory.process(operation)
        }
        writer.write(OUTPUT_FILE, stockInventory.currentStocks())
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}