package jen.doughapp.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import jen.doughapp.R

private val defaultTypography = Typography()

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

//todo -- decide fonts
val PlusJakartaSansFont = GoogleFont("Plus Jakarta Sans")
val NotoSerifFont = GoogleFont("Noto Serif")
val SoraFont = GoogleFont("Sora")
val MontserratFont = GoogleFont("Montserrat")
val NeutonFont = GoogleFont("Neuton")

val PlusJakartaSans = FontFamily(
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

val NotoSerif = FontFamily(
    Font(googleFont = NotoSerifFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NotoSerifFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NotoSerifFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NotoSerifFont, fontProvider = provider, weight = FontWeight.Bold)
)
val Sora = FontFamily(
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Montserrat = FontFamily(
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Neuton = FontFamily(
    Font(googleFont = NeutonFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NeutonFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NeutonFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NeutonFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    /* headlineLarge used for app title, recipe name on detail, "build your levain" */
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = Sora,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 40.sp,
        color = Purple40
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = PlusJakartaSans
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = PlusJakartaSans
    ),

    /* using titleMedium for top app bar
    * titleSmall for sectionheader*/
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        color = Purple20
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        color = BrownGray30
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold
    ),

    /* bodyLarge is general default */
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = PlusJakartaSans,
        color = BrownGray30
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = PlusJakartaSans,
        color = BrownGray30
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = PlusJakartaSans,
        color = BrownGray30
    ),

    /*
    primary button labelLarge
    filter chips labelSmall
    */
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = BrownGray30
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = BrownGray30
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = BrownGray30
    ),
)