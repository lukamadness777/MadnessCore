// dev/lukamadness/madnesscore/util/RaceText.java
package dev.lukamadness.madnesscore.util;

import dev.lukamadness.madnesscore.api.MadnessCoreAPI;
import net.minecraft.text.Text;

/**
 * Arma el texto de identidad de un jugador (especie [+ sangre] [+ familia]),
 * saltando automáticamente los niveles que no existan.
 */
public final class RaceText {

    private RaceText() {}

    public static Text identity(MadnessCoreAPI.RaceProfile profile) {
        Text speciesText = Text.literal(profile.species().displayName());

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

    /** Saca los códigos de color (§x) para poder comparar/mostrar nombres "limpios". */
    public static String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}