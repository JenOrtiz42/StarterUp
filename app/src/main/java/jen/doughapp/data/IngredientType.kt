package jen.doughapp.data

enum class IngredientType {
    FLOUR, HYDRATION, SALT, YEAST, STARTER
}

//todo, look into localization
// could simplify this into an extension like "toLocalizedString" or something?
fun getIngredientTypeName(type: IngredientType?): String {
    if (type == IngredientType.FLOUR) return "Flour"
    if (type == IngredientType.HYDRATION) return "Hydration"
    if (type == IngredientType.SALT) return "Salt"
    if (type == IngredientType.YEAST) return "Yeast"
    if (type == IngredientType.STARTER) return "Starter"
    return ""
}
