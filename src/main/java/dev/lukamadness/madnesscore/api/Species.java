package dev.lukamadness.madnesscore.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record Species(Identifier id, Text displayName, double weight) {
}