package infrastructure

import domain.inventory.StockInventory
import domain.model.StockItem
import domain.model.StockOperation
import java.util.*

class StockInventoryImpl : StockInventory {

    private val storage = HashMap<String, TreeMap<String, Long>>()

    override fun process(operation: StockOperation) {
        when (operation) {
            is StockOperation.Income -> handleIncome(income = operation)
            is StockOperation.Sale -> handleSale(sale = operation)
        }
    }

    override fun currentStocks(): List<StockItem> {
        val result = mutableListOf<StockItem>()
        val sortedGroups = storage.toSortedMap()

        for ((groupId, group) in sortedGroups) {
            for ((productId, count) in group) {
                val stockItem = StockItem(groupId = groupId, productId = productId, count = count)
                result.add(stockItem)
            }
        }

        return result
    }

    private fun handleIncome(income: StockOperation.Income) {
        val groupProducts: TreeMap<String, Long> = storage.computeIfAbsent(income.groupId) { TreeMap() }
        val currentCount = groupProducts[income.productId] ?: 0L
        groupProducts[income.productId] = currentCount + income.count
    }

    private fun handleSale(sale: StockOperation.Sale) {
        val groupProducts = storage[sale.groupId]
            ?: throw IllegalStateException("Cannot sell from non-existent group: ${sale.groupId}")
        var remainingToSell = sale.count

        val iterator = groupProducts.entries.iterator()
        val firstProductId = groupProducts.firstKey()

        while (iterator.hasNext() && remainingToSell > 0) {
            val entry = iterator.next()
            val productId = entry.key
            val currentStock = entry.value

            if (currentStock <= 0) continue

            if (currentStock >= remainingToSell) {
                groupProducts[productId] = currentStock - remainingToSell
                remainingToSell = 0L
            } else {
                remainingToSell -= currentStock
                groupProducts[productId] = 0L
            }
        }

        if (remainingToSell > 0) {
            val currentStockOfFirst = groupProducts.getOrDefault(firstProductId, 0L)
            groupProducts[firstProductId] = currentStockOfFirst - remainingToSell
        }
    }
}