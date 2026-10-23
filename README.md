# Smart Pantry Manager

A Java Android app that helps reduce food waste. You record the ingredients you already have at home, and the app suggests **only** the recipes you can cook right now with those ingredients. No shopping trip is needed, and no recipe is suggested unless you have everything it needs.

Built for the **Mobile App Development 700** practical assignment (Richfield Graduate Institute of Technology).

---

## Features

| Feature | Description |
|---|---|
| **Pantry management (CRUD)** | Add, view, edit and delete ingredients, each with a name, quantity, unit and optional expiry date |
| **Input validation** | Name is required (letters only, max 40 characters), quantity must be a number greater than 0, and past expiry dates are blocked. Errors show under each field |
| **Recipe collection** | 20 recipes are loaded into the database automatically on first launch, including South African dishes such as Chakalaka and Pap |
| **Suggested Recipes (strict matching)** | Shows a recipe only if **every** ingredient is in the pantry in **at least** the required quantity |
| **Recipe detail** | Full ingredient list with amounts and a numbered method |
| **Settings** | Expiring-soon alerts on or off, warning period (1, 3, 7 or 14 days), default unit for new ingredients, and a Clear Pantry option |
| **Expiry alerts** | A banner on the pantry screen counts items that are expired or expiring soon, and expired items are shown in red |
| **Empty states** | Helpful messages instead of blank screens, e.g. "No recipes match your pantry yet – add more ingredients" |
| **Navigation** | Bottom navigation bar (Pantry · Recipes · Settings), plus a toolbar back arrow on the detail screens |

---

## Database: SQLite

The app uses **SQLite** through Android's `SQLiteOpenHelper`, stored locally on the device.

### Why SQLite?

- **The data is personal and local.** A pantry belongs to one person on one phone, so there's nothing to sync between devices or users.
- **It works offline.** No internet, account, server or backend is needed, and nothing can go down or cost money.
- **It's built into Android.** There's no extra library or setup, and it's the database approach covered in the module's persistent-data chapter.
- **The data truly persists.** It's stored in a database file in the app's private storage, so it survives closing and reopening the app and restarting the phone.

### Tables

| Table | Columns | Purpose |
|---|---|---|
| `pantry_items` | `_id` (PK), `name`, `normalized_name`, `quantity`, `unit`, `expiry_date` | Ingredients the user has (full CRUD) |
| `recipes` | `_id` (PK), `name` (UNIQUE), `steps` | Recipe name and method (loaded once on first run) |
| `recipe_ingredients` | `_id` (PK), `recipe_id` (FK), `name`, `normalized_name`, `quantity`, `unit` | The ingredients each recipe needs |

### Relationships

- **`recipes` 1 ──< ∞ `recipe_ingredients`:** one recipe has many ingredients. This is enforced with a foreign key and `ON DELETE CASCADE`. Foreign keys are switched on in `DatabaseHelper.onConfigure()`.
- **`pantry_items` ↔ `recipe_ingredients`:** these are matched **at runtime** on `normalized_name` and unit category by `RecipeMatcher`. This is intentionally not a foreign key, so a recipe can require an ingredient the user doesn't have.
- **Constraints:** `quantity > 0` (CHECK), `NOT NULL` on required columns, and unique recipe names.

App settings are stored separately in **SharedPreferences**, because they are simple key-value pairs.

---

## How strict matching works

The core logic is in `logic/RecipeMatcher.java`:

1. **Normalise names** with `IngredientManager`. Each name is lowercased and trimmed, made singular, and mapped through synonyms:
   - "Tomatoes" → "tomato"
   - "EGGS" → "egg"
   - "Mielie meal" → "maize meal"
   - "Spaghetti" → "pasta"
2. **Convert units** with `UnitManager`. Every amount is converted to a base unit per category:
   - mass to grams (g, kg)
   - volume to millilitres (ml, L, tsp, tbsp, cup)
   - countable items to pieces (pcs)

   For example, 1 kg becomes 1000 g. Amounts in different categories (e.g. grams vs cups) are never compared.
3. **Build a stock table** from the pantry: `name|category → total amount`. Duplicate pantry entries are added together.
4. **Check every ingredient.** A recipe is suggested **only if every ingredient** is found and the pantry amount is greater than or equal to the required amount. If even one is missing or short, the recipe is excluded.

**Example:** "Cheese Omelette" needs Egg 3 pcs, Cheese 50 g, Butter 10 g and Milk 30 ml. With all four in the pantry it's suggested. Delete the milk and it disappears.

---

## Project structure

Package: `com.dihanvisgaie.smartpantrymanager`

```
app/src/main/java/com/dihanvisgaie/smartpantrymanager/
├── activities/
│   ├── PantryListActivity.java          Launcher screen – pantry list (Read, Delete)
│   ├── AddEditIngredientActivity.java   Add / edit form with validation (Create, Update)
│   ├── SuggestedRecipesActivity.java    Strictly matched recipes
│   ├── RecipeDetailActivity.java        Ingredients and method for one recipe
│   └── SettingsActivity.java            User preferences
├── adapters/
│   ├── PantryAdapter.java               RecyclerView adapter for pantry items
│   ├── RecipeAdapter.java               RecyclerView adapter for recipe cards
│   └── IngredientAdapter.java           RecyclerView adapter for recipe ingredients
├── database/
│   ├── DatabaseSchema.java              Table/column names and CREATE TABLE statements
│   ├── DatabaseHelper.java              SQLiteOpenHelper (singleton) – creates the database
│   ├── PantryDao.java                   CRUD operations for pantry items
│   ├── RecipeDao.java                   Reads recipes with their ingredients
│   └── RecipePreLouder.java             Loads the 20 starter recipes on first run
├── logic/
│   ├── RecipeMatcher.java               Strict-matching rule
│   ├── IngredientManager.java           Ingredient name normalisation
│   └── UnitManager.java                 Unit conversion
├── models/
│   ├── PantryItem.java
│   ├── Recipe.java
│   └── RecipeIngredient.java
└── utils/
    ├── NavigationHelper.java            Bottom navigation (Intents)
    ├── PreferencesManager.java          SharedPreferences wrapper
    ├── DateUtils.java                   Expiry date calculations
    └── FormatUtils.java                 Quantity display formatting

app/src/main/res/
├── layout/     5 screen layouts + 3 list-row layouts
├── menu/       bottom_nav_menu.xml
├── drawable/   bg_warning_banner.xml
└── values/     strings.xml, arrays.xml, colors.xml, themes.xml

app/src/test/java/com/dihanvisgaie/smartpantrymanager/logic/
└── RecipeMatcherTest.java               11 unit tests for strict matching
```

### Key Android concepts used

- **Activities and lifecycle:** lists reload in `onResume()`, so they're always up to date after adding, editing or switching tabs. Form state survives screen rotation via `onSaveInstanceState()`.
- **Intents:** explicit Intents move between screens and pass data with extras. Examples are `EXTRA_ITEM_ID` for edit mode and `EXTRA_RECIPE_ID` for the detail screen.
- **RecyclerView and custom Adapters:** three adapters, each with a ViewHolder. Click events are passed back to the Activity through listener interfaces.
- **Layouts:** LinearLayout and FrameLayout with Material 3 components (MaterialToolbar, TextInputLayout, MaterialCardView, BottomNavigationView, MaterialSwitch).

---

## Tech stack

| | |
|---|---|
| Language | Java 11 (no Kotlin) |
| UI | XML layouts, Material Components 1.14 (Material 3 theme) |
| Database | SQLite via `SQLiteOpenHelper` |
| Preferences | SharedPreferences |
| Build | Gradle 9.8, Android Gradle Plugin 9.4.1, Kotlin DSL build scripts |
| SDK | minSdk 24 (Android 7.0), targetSdk / compileSdk 37 |
| Testing | JUnit 4 |
| IDE | Android Studio |

---

## Setup and run

### Requirements

- Android Studio (latest stable). It includes the Android SDK and JDK.
- An Android emulator, or a phone running **Android 7.0 (API 24) or newer** with USB debugging on.
- No database server, internet connection or API keys are needed.

### Steps

1. **Clone the repository:**
   ```
   git clone https://github.com/<your-username>/<your-repo-name>.git
   ```
2. **Open it** in Android Studio: **File → Open**, then select the project folder.
3. **Wait for Gradle sync** to finish. Watch the progress bar at the bottom.
4. **Create an emulator** if you don't have one: **Tools → Device Manager → Create Device**, e.g. a Pixel phone with a recent system image.
5. **Select the `app` run configuration** and your device, then press **Run ▶**.
6. On first launch, the database is created and the 20 recipes are loaded automatically. The pantry starts empty.

### Try the strict-matching rule

1. On the **Pantry** tab, tap **+** and add:
   - Eggs, 3 pcs
   - Cheese, 50 g
   - Butter, 10 g
   - Milk, 30 ml
2. Open the **Recipes** tab. **Cheese Omelette** appears.
3. Go back to **Pantry**, long-press **Milk** and delete it.
4. Open **Recipes** again. The omelette has disappeared, because one ingredient is missing.
5. Close the app completely and reopen it. Your pantry items are still there, so the data persists.

### Run the unit tests

In the Project panel, right-click `app/src/test/.../logic/RecipeMatcherTest.java` and choose **Run 'RecipeMatcherTest'**. All 11 tests should pass.

Or, from the terminal:

```
gradlew test
```

### Troubleshooting

- **The app crashes after changing the database code or seed recipes.** Recipes are only loaded when the database is first created. Uninstall the app from the emulator (long-press the icon → App info → Uninstall), or increase `DATABASE_VERSION` in `DatabaseSchema.java`, then run again.
- **The app crashes on launch.** Open **Logcat**, search for `FATAL EXCEPTION`, and read the `Caused by:` line.

---

## Tips for best results

Recipes store dry foods in **grams**, liquids in **millilitres** and countable items in **pieces**. Enter pantry items the same way (e.g. Flour 2 kg, Milk 1 L, Eggs 6 pcs) so amounts can be compared. Salt, pepper and water are not listed as required ingredients.

---

## Out of scope

As required by the brief, the app has:

- no maps, GPS or location features (the app requests no permissions at all)
- no payments
- no Play Store publishing

---

## Author

**Name:** `<Your Name>`
**Student number:** `<Your Student Number>`
**Module:** Mobile App Development 700
**Repository:** `<GitHub link>`
