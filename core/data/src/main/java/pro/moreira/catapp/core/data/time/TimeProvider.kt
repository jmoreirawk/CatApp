package pro.moreira.catapp.core.data.time

internal interface TimeProvider {
    fun nowMillis(): Long
}

internal object SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
