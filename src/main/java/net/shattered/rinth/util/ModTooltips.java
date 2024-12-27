package net.shattered.rinth.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ModTooltips {
    // Add tooltips for Pitchfork
    public static void addPitchforkTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal("Forged in the Nether").formatted(Formatting.RED));
        tooltip.add(Text.literal("• Fire Effect").formatted(Formatting.GRAY));
    }

    // Add tooltips for Upgraded Trident
    public static void addUpgradedTridentTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal("Nether Star Infused").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("• Auto-Targeting").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("• Fire Effect").formatted(Formatting.GRAY));
    }

    // Helper method to add a bullet point tooltip
    public static void addBulletPoint(List<Text> tooltip, String text, Formatting formatting) {
        tooltip.add(Text.literal("• " + text).formatted(formatting));
    }

    // Helper method to add a header tooltip
    public static void addHeader(List<Text> tooltip, String text, Formatting formatting) {
        tooltip.add(Text.literal(text).formatted(formatting));
    }
}