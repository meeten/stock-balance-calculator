import infrastructure.CsvStockReader
import infrastructure.CsvStockWriter
import infrastructure.StockInventoryImpl
import java.io.IOException

fun main(args: Array<String>) {
    try {
        require(args.size == 2) {
            "Usage: <input.csv> <output.csv>"
        }

        val inputFile = args[0]
        val outputFile = args[1]

        val reader = CsvStockReader()
        val writer = CsvStockWriter()
        val stockInventory = StockInventoryImpl()

        reader.read(inputFile) { operation ->
            stockInventory.process(operation)
        }

        writer.write(outputFile, stockInventory.currentStocks())

        println("Stock calculation completed successfully")
    } catch (e: IllegalArgumentException) {
        System.err.println("Input error: ${e.message}")
    } catch (e: IllegalStateException) {
        System.err.println("Processing error: ${e.message}")
    } catch (e: IOException) {
        System.err.println("File error: ${e.message}")
    } catch (e: Exception) {
        System.err.println("Unexpected error: ${e.message}")
        e.printStackTrace()
    }
}