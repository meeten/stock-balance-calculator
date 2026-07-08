import infrastructure.CsvStockReader
import infrastructure.CsvStockWriter
import infrastructure.StockInventoryImpl

fun main(args: Array<String>) {
    val (inputFile, outputFile) = when (args.size) {
        2 -> args[0] to args[1]
        0 -> DEFAULT_INPUT_FILE to DEFAULT_OUTPUT_FILE
        else -> {
            println("Usage: <input.csv> <output.csv>")
            return
        }
    }

    val reader = CsvStockReader()
    val writer = CsvStockWriter()
    val stockInventory = StockInventoryImpl()

    try {
        reader.read(inputFile) { operation ->
            stockInventory.process(operation)
        }

        writer.write(outputFile, stockInventory.currentStocks())
        println("Stock balances successfully written to '$outputFile'.")
    } catch (e: IllegalArgumentException) {
        println("Input error: ${e.message}")
    } catch (e: IllegalStateException) {
        println("Processing error: ${e.message}")
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
    }
}

private const val DEFAULT_INPUT_FILE = "input.csv"
private const val DEFAULT_OUTPUT_FILE = "output.csv"