package com.nothing.card.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontRes
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.nothing.card.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val SpaceMonoFont = GoogleFont("Space Mono")
val SpaceMonoFamily = FontFamily(
    GoogleFontRes(googleFont = SpaceMonoFont, fontProvider = provider, weight = FontWeight.Normal),
    GoogleFontRes(googleFont = SpaceMonoFont, fontProvider = provider, weight = FontWeight.Bold),
)

private val OutfitFont = GoogleFont("Outfit")
val OutfitFamily = FontFamily(
    GoogleFontRes(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Light),
    GoogleFontRes(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Normal),
    GoogleFontRes(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Medium),
    GoogleFontRes(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.SemiBold),
    GoogleFontRes(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Bold),
)

val Ndot55Family = FontFamily(
    Font(R.font.ndot55, FontWeight.Normal)
)
val Ndot57Family = FontFamily(
    Font(R.font.ndot57, FontWeight.Normal)
)
val NType82Family = FontFamily(
    Font(R.font.ntype82, FontWeight.Normal)
)
val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal)
)
val NothingSerifFamily = androidx.compose.ui.text.font.FontFamily.Serif

val NothingTypography = Typography(
    // ── Display: Branding (Space Mono for technical impact) ──────────────────
    displayLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // ── Headline: Section Headers (Space Grotesk Bold) ──────────────────────
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.sp
    ),

    // ── Title: Content Titles (Space Grotesk Medium) ─────────────────────────
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body: General Text (Space Grotesk Regular) ──────────────────────────
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // ── Label: Technical info (Space Mono) ──────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
)
