package pro.moreira.catapp.core.domain.model

data class Lifespan(val lower: Int, val upper: Int) {
    val selectedValue: Int get() = lower

    companion object {
        fun parse(value: String): Lifespan? {
            val trimmed = value.trim()
            if (trimmed.isBlank()) return null

            val parts = trimmed.split("-").map { it.trim() }
            return when (parts.size) {
                2 -> {
                    val lower = parts[0].toIntOrNull() ?: return null
                    val upper = parts[1].toIntOrNull() ?: return null
                    Lifespan(lower, upper)
                }
                1 -> {
                    val single = parts[0].toIntOrNull() ?: return null
                    Lifespan(single, single)
                }
                else -> null
            }
        }
    }
}

fun List<Lifespan>.averageSelectedValue(): Double? {
    if (isEmpty()) return null
    return map { it.selectedValue }.average()
}