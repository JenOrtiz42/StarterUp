package jen.doughapp.domain

import kotlin.math.roundToInt

data class StarterRatio(
    val starterPortion: Int,
    val flourPortion: Int,
    val waterPortion: Int
) {
    val displayString: String
        get() = "$starterPortion:$flourPortion:$waterPortion"

    val totalParts: Int
        get() = starterPortion + flourPortion + waterPortion

    /**
     * Calculates the weight of a specific portion given a total target weight
     */
    fun calculateAmount(targetTotal: String, portion: Int): Int {
        val total = targetTotal.toDoubleOrNull() ?: 0.0
        if (totalParts <= 0) return 0
        return ((total / totalParts) * portion).roundToInt()
    }

    /**
     * Calculates the weight of all portions given a total target weight
     */
    fun calculateAmounts(targetTotal: String): LevainIngredients {
        val total = targetTotal.toDoubleOrNull() ?: 0.0
        if (totalParts <= 0) return LevainIngredients(0, 0, 0)

        val unit = total / totalParts
        return LevainIngredients(
            (unit * starterPortion).roundToInt(),
            (unit * flourPortion).roundToInt(),
            (unit * waterPortion).roundToInt()
        )
    }

    companion object {
        /**
         * Converts a string like "1:2:2" into a StarterRatio.
         * Returns null if the format is invalid or contains non-numeric values.
         */
        fun fromString(value: String): StarterRatio? {
            val parts = value.split(":")
            if (parts.size != 3) return null

            return try {
                StarterRatio(
                    starterPortion = parts[0].trim().toInt(),
                    flourPortion = parts[1].trim().toInt(),
                    waterPortion = parts[2].trim().toInt()
                )
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}

/**
 * Represents the actual calculated weights for a levain build.
 */
data class LevainIngredients(val starterWeight: Int,
                             val flourWeight: Int,
                             val waterWeight: Int
) {
    val totalWeight: Int get() = starterWeight + flourWeight + waterWeight
}
