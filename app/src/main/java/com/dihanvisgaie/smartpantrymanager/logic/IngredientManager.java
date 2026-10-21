package com.dihanvisgaie.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
    IngredientManager: turns a typed ingredient name into a standard matching key.

    "Tomatoes "       -> "tomato"
    "EGGS"            -> "egg"
    "Cherries"        -> "cherry"
    "chicken fillets" -> "chicken breast"   (synonym)
    "Mielie meal"     -> "maize meal"       (synonym)

    Both the pantry and the recipe ingredients are saved with this key, so the
    strict-matching logic compares like with like instead of using a naive exact match.
 */
public final class IngredientManager {

    /** Plurals that don't follow the simple rules below. */
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    /** Different names for the same pantry ingredient -> one standard name singular form. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("loaves", "loaf");
        IRREGULAR_PLURALS.put("halves", "half");
        IRREGULAR_PLURALS.put("knives", "knife");

        // Pasta shapes
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("noodle", "pasta");
        // Cheese
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("gouda", "cheese");
        SYNONYMS.put("gouda cheese", "cheese");
        SYNONYMS.put("grated cheese", "cheese");
        // Chicken
        SYNONYMS.put("chicken fillet", "chicken breast");
        SYNONYMS.put("chicken", "chicken breast");
        // Beef mince
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("mince meat", "beef mince");
        SYNONYMS.put("mincemeat", "beef mince");
        // Peppers
        SYNONYMS.put("bell pepper", "green pepper");
        SYNONYMS.put("red pepper", "green pepper");
        SYNONYMS.put("yellow pepper", "green pepper");
        SYNONYMS.put("sweet pepper", "green pepper");
        SYNONYMS.put("capsicum", "green pepper");
        // Maize meal
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("maizemeal", "maize meal");
        SYNONYMS.put("mieliepap", "maize meal");
        // Oil
        SYNONYMS.put("oil", "cooking oil");
        SYNONYMS.put("sunflower oil", "cooking oil");
        SYNONYMS.put("vegetable oil", "cooking oil");
        SYNONYMS.put("canola oil", "cooking oil");
        // Bread
        SYNONYMS.put("white bread", "bread");
        SYNONYMS.put("brown bread", "bread");
        SYNONYMS.put("bread slice", "bread");
        SYNONYMS.put("slice of bread", "bread");
        // Other
        SYNONYMS.put("mayo", "mayonnaise");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("clove of garlic", "garlic");
        SYNONYMS.put("canned tuna", "tuna");
        SYNONYMS.put("tinned tuna", "tuna");
        SYNONYMS.put("rolled oat", "oat");
        SYNONYMS.put("stock", "stock cube");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("brown sugar", "sugar");
        SYNONYMS.put("fresh cream", "cream");
        SYNONYMS.put("cooking cream", "cream");
        SYNONYMS.put("white rice", "rice");
        SYNONYMS.put("basmati rice", "rice");
        SYNONYMS.put("full cream milk", "milk");
        SYNONYMS.put("low fat milk", "milk");
    }

    private IngredientManager() {
        // static helper only
    }

    /**
        Returns the matching key for an ingredient name.
        Steps: lowercase -> strip punctuation -> collapse spaces -> singular -> synonyms.
     */
    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }

        // 1. Lowercase; hyphens become spaces ("self-raising" -> "self raising")
        String name = rawName.toLowerCase(Locale.ROOT).replace('-', ' ');

        // 2. Remove anything that is not a letter or space (digits, apostrophes, commas)
        name = name.replaceAll("[^\\p{L} ]", "");

        // 3. Collapse multiple spaces and trim
        name = name.replaceAll("\\s+", " ").trim();
        if (name.isEmpty()) {
            return "";
        }

        // 4. Make the LAST word singular ("baked beans" -> "baked bean", "eggs" -> "egg")
        int lastSpace = name.lastIndexOf(' ');
        String prefix = lastSpace == -1 ? "" : name.substring(0, lastSpace + 1);
        String lastWord = lastSpace == -1 ? name : name.substring(lastSpace + 1);
        name = prefix + singularize(lastWord);

        // 5. Map synonyms to one standard name
        String synonym = SYNONYMS.get(name);
        return synonym != null ? synonym : name;
    }

    /** Simple English singular rules good enough for food names, not a full NLP solution. */
    static String singularize(String word) {
        if (word.length() <= 3) {
            return word;
            // "gas", "pea"...: leave short words
        }
        String irregular = IRREGULAR_PLURALS.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
            // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);
            // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
            // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
            // couscous, hummus, swiss
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
            // eggs -> egg
        }
        return word;
    }
}
