package com.nexus.model.id;

/**
 * Implementação de IdStrategy que calcula o ID a partir da slug do campo
 * fornecido.
 */
public final class SlugIdStrategy implements IdStrategy {

    private static final String FALLBACK_SLUG = "item";

    @Override
    public String generate(String seed) {
        if (seed == null || seed.isBlank()) {
            return FALLBACK_SLUG;
        }

        String slug = seed.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return slug.isEmpty() ? FALLBACK_SLUG : slug;
    }
}