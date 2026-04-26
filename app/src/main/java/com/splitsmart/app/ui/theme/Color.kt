package com.splitsmart.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette ────────────────────────────────────────────────────────────
val Primary       = Color(0xFF6C63FF)   // Vivid indigo-violet
val PrimaryDark   = Color(0xFF4B44CC)
val Secondary     = Color(0xFF00D4AA)   // Teal accent
val SecondaryDark = Color(0xFF00A884)

// ── Surface / background ────────────────────────────────────────────────────
val Background      = Color(0xFFF5F6FF)
val BackgroundDark  = Color(0xFF0D0D1A)
val Surface         = Color(0xFFFFFFFF)
val SurfaceDark     = Color(0xFF1A1A2E)
val SurfaceVariant  = Color(0xFFEEEFF8)
val SurfaceVariantDark = Color(0xFF252540)

// ── Semantic colours ─────────────────────────────────────────────────────────
val PositiveGreen = Color(0xFF00C896)   // money to receive
val NegativeRed   = Color(0xFFFF5F7E)  // money owed
val NeutralGray   = Color(0xFF9E9EAF)

// ── Avatar palette — deterministically assigned per user ────────────────────
val AvatarColors = listOf(
    Color(0xFF6C63FF), Color(0xFF00D4AA), Color(0xFFFF8C69),
    Color(0xFFFFD166), Color(0xFF06D6A0), Color(0xFFEF476F),
    Color(0xFF118AB2), Color(0xFFFF9F1C), Color(0xFF8338EC)
)

fun avatarColorForName(name: String): Color {
    val index = name.hashCode().let { if (it < 0) -it else it } % AvatarColors.size
    return AvatarColors[index]
}
