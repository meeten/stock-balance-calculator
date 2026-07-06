package domain.model

sealed interface StockOperation {

    val groupId: String
    val count: Long

    data class Income(
        override val groupId: String,
        val productId: String,
        override val count: Long,
    ) : StockOperation

    data class Sale(
        override val groupId: String,
        override val count: Long,
    ) : StockOperation
}