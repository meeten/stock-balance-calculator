package domain.inventory

import domain.model.StockItem
import domain.model.StockOperation

interface StockInventory {

    fun process(operation: StockOperation)

    fun currentStocks(): List<StockItem>
}