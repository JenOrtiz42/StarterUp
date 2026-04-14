package jen.doughapp.ui.utils

fun Double.formatBakersPercentage(): String {
    // Display without trailing 0s, and without a decimal if it's a whole number
    val formattedNumber = "%.2f".format(this)
        .trimEnd('0')
        .trimEnd('.')

    return "${formattedNumber}%"
}

fun Double?.formatMultiplier(): String {
    // Display without trailing 0s, and without a decimal if it's a whole number
    val formattedNumber = "%.2f".format(this)
        .trimEnd('0')
        .trimEnd('.')

    return formattedNumber
}

fun Double?.formatAmount() : String {
    // Display without trailing 0s, and without a decimal if it's a whole number
    val displayValue = "%.2f".format(this)
        .trimEnd('0')
        .trimEnd('.')

    return displayValue
}

fun Double?.formatHydration() : String {
    // Display without trailing 0s, and without a decimal if it's a whole number
    val displayValue = "%.1f".format(this)
        .trimEnd('0')
        .trimEnd('.')

    return "${displayValue}%"
}