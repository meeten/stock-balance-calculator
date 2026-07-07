package infrastructure

import domain.inventory.StockInventory
import domain.model.StockItem
import domain.model.StockOperation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class StockInventoryTest {

    private lateinit var stockInventory: StockInventory

    @BeforeEach
    fun setUp() {
        stockInventory = StockInventoryImpl()
    }

    @Test
    fun `When income is processed then stock increases`() {
        val groupId = "A"
        val productId = "1"
        val count = 10L

        val operation = StockOperation.Income(groupId, productId, count)

        stockInventory.process(operation)

        val actualStockItem = stockInventory.currentStocks()[0]
        val expectedStockItem = StockItem(groupId, productId, count)
        assertEquals(expectedStockItem, actualStockItem)
    }

    @Test
    fun `When multiple income operations are processed then all stock items should be stored`() {
        val expectedStockItems = mutableListOf<StockItem>()
        val n = 100
        repeat(n) {
            val groupId = "group_$it"
            val productId = "product_$it"
            val count = it.toLong()
            val operation = StockOperation.Income(groupId, productId, count)

            stockInventory.process(operation)
            expectedStockItems.add(StockItem(groupId, productId, count))
        }

        val actualStockItems = stockInventory.currentStocks()
        val sortedExpectedStockItems = expectedStockItems.sortedWith(compareBy({ it.groupId }, { it.productId }))
        assertEquals(sortedExpectedStockItems, actualStockItems)
    }

    @Test
    fun `When same item arrives then count for that item should be summed up`() {
        val groupId = "A"
        val productId = "1"
        val count1 = 10L
        val count2 = 15L

        stockInventory.process(StockOperation.Income(groupId, productId, count1))
        stockInventory.process(StockOperation.Income(groupId, productId, count2))

        val actualStockItems = stockInventory.currentStocks()
        val expectedStockItems = listOf(
            StockItem(groupId, productId, count1 + count2)
        )

        assertEquals(expectedStockItems, actualStockItems)
    }

    @Test
    fun `When sale count is less than first item stock then it should partially decrease first item count`() {
        val groupId = "A"
        val productId = "1"
        val count = 50L

        stockInventory.process(StockOperation.Income(groupId, productId, count))
        stockInventory.process(StockOperation.Sale(groupId, 25))

        val actualStockItem = stockInventory.currentStocks()[0]
        val expectedStockItem = StockItem(groupId, productId, 25)
        assertEquals(expectedStockItem, actualStockItem)
    }

    @Test
    fun `When sale count equals income count then stock should be zero`() {
        val groupId = "A"
        val productId = "1"
        val count = 10L

        stockInventory.process(StockOperation.Income(groupId, productId, count))
        stockInventory.process(StockOperation.Sale(groupId, count))

        val actualStockItem = stockInventory.currentStocks()[0]
        val expectedStockItem = StockItem(groupId, productId, 0)
        assertEquals(expectedStockItem, actualStockItem)
    }

    @Test
    fun `When income arrives after negative balance then it should reduce debt`() {
        val groupId = "A"
        val productId = "1"
        val count = 50L

        stockInventory.process(StockOperation.Income(groupId, productId, count))
        stockInventory.process(StockOperation.Sale(groupId, 70))

        var actualStockItem = stockInventory.currentStocks()[0]
        var expectedStockItem = StockItem(groupId, productId, -20)
        assertEquals(expectedStockItem, actualStockItem)

        stockInventory.process(StockOperation.Income(groupId, productId, 21))
        actualStockItem = stockInventory.currentStocks()[0]
        expectedStockItem = StockItem(groupId, productId, 1)
        assertEquals(expectedStockItem, actualStockItem)
    }

    @Test
    fun `When highest-ranked item is out of stock then sales continue with the next-ranked item`() {
        val groupId = "A"

        val productId1 = "1"
        val count1 = 10L

        val productId2 = "2"
        val count2 = 10L

        stockInventory.process(StockOperation.Income(groupId, productId1, count1))
        stockInventory.process(StockOperation.Income(groupId, productId2, count2))
        stockInventory.process(StockOperation.Sale(groupId, 15L))

        val actualStockItems = stockInventory.currentStocks()
        val expectedStockItems = listOf(
            StockItem(groupId, productId1, 0),
            StockItem(groupId, productId2, 5)
        )
        assertEquals(expectedStockItems, actualStockItems)
    }

    @Test
    fun `When sale exceeds stock then remaining becomes negative`() {
        val groupId = "A"

        val productId1 = "1"
        val count1 = 10L

        val productId2 = "2"
        val count2 = 10L

        val productId3 = "3"
        val count3 = 30L

        stockInventory.process(StockOperation.Income(groupId, productId1, count1))
        stockInventory.process(StockOperation.Income(groupId, productId2, count2))
        stockInventory.process(StockOperation.Income(groupId, productId3, count3))
        stockInventory.process(StockOperation.Sale(groupId, 100L))

        val actualStockItems = stockInventory.currentStocks()
        val expectedStockItems = listOf(
            StockItem(groupId, productId1, -50),
            StockItem(groupId, productId2, 0),
            StockItem(groupId, productId3, 0)
        )
        assertEquals(expectedStockItems, actualStockItems)
    }

    @Test
    fun `When sale is made from a non-existent group then exception is thrown`() {
        val groupId = "A"
        val count = 50L

        assertThrows<IllegalStateException> {
            stockInventory.process(StockOperation.Sale(groupId, count))
        }
    }
}