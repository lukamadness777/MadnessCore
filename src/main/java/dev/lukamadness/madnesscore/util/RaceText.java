package dev.lukamadness.madnesscore.util;

import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import net.minecraft.text.Text;

public final class RaceText {

    private RaceText() {}

    public static Text identity(MadnessCoreAPI.RaceProfile profile) {
        Text speciesText = profile.species().displayName();

        if (profile.bloodline() == null) {
            return Text.translatable(MadnessLang.IDENTITY_SPECIES_ONLY, speciesText);
        }
        Text bloodlineText = Text.literal(profile.bloodline().displayName());

        if (profile.family() == null) {
            return Text.translatable(MadnessLang.IDENTITY_SPECIES_BLOODLINE, speciesText, bloodlineText);
        }
        Text familyText = Text.literal(profile.family().displayName());

        return Text.translatable(MadnessLang.IDENTITY_SPECIES_BLOODLINE_FAMILY, speciesText, bloodlineText, familyText);
    }

    public static String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}