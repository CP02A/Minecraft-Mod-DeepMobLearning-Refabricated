# Deep Mob Learning: Refabricated: port to Minecraft 26.3

Branch: `26.3`, based on `1.20` (0.6.1-BETA for Minecraft 1.20.1, Yarn mappings, Kotlin 1.8). Target: Minecraft 26.3,
Fabric Loader 0.20.1, Fabric API 0.165.2+26.3, Kotlin 2.4.21 (Fabric Language Kotlin 1.14.2), Java 25.

## Status

- All three modules (`base`, `modular-armor`, `simulacrum`) compile against 26.3. `./gradlew build` builds the jar
  (the main mod with the three modules nested).
- `./gradlew nonUiTest` (`check` + `runGametest`): the jar contains only DML classes and the nested jars bundled on
  purpose, mixin members are prefixed, and a headless game test server starts with all modules, loads every registry,
  mixin and data pack (recipes, loot tables, advancements, tags, world gen) without errors, and stops.
- `./gradlew runServer`: a real dedicated server (with REI) creates a world and starts without errors from the mod.
- `./gradlew uiTest` (`runClientGametest`): plays the mod in the game window (`src/clientGametest`) and takes
  screenshots in `runs/clientGametest/screenshots`:
  - every block placed (models, the data synthesizer, disruptions core and matter condenser renderers),
  - the System Glitch spawned (entity renderer),
  - the GUIs of the data synthesizer, loot fabricator, matter condenser, simulation chamber, deep learner (with the
    entity showcase) and glitch armor (effect list),
  - a worn glitch chestplate (equipment asset, armor value from its data model),
  - checks: the trial keystone, loot fabricator and crushing recipes load, the loot fabricator turns pristine matter
    into loot, the data synthesizer turns data into energy.
- Multiplayer with the Furcraft 1.3.0 modpack (Modrinth pack, 26.3) plus the other 26.3 ports (AE2, GuideME, Mekanism,
  EnderIO, Industrial Foregoing, MineColonies, Thaumcraft, Titanium): a real dedicated server (Loom's production
  server run) and a real client (production client run with a client game test that joins the server and plays like a
  player, through the network). Passed:
  - placing every machine from the hand, opening the GUIs, moving items into them with shift clicks,
  - the loot fabricator making loot, the data synthesizer, the simulation chamber making matter (all seen in the menus
    synced to the client),
  - killing zombies with a deep learner in the hotbar adds data to its data model,
  - attuning a trial key in a crafting table, starting a trial on the keystone, the waves, killing the System Glitch,
    "Trial Succeeded" and the rewards,
  - wearing glitch armor (armor value on the client) and its GUI, the System Glitch entity on the client,
  - the matter condenser charging a glitch helmet with pristine matter, a data model put into the helmet with its GUI,
  - every glitch armor effect, in survival, with a full set of the effect's category and tier against a set of
    another category: fire protection/immunity, auto extinguish, piglin truce, feather falling, jump boost, fall
    immunity, plenty, rot resistance, unrotten flesh, zombie/skeleton/shulker friendly, archery, endermen proof
    vision, teleport and soul vision (their keys), night vision, fly, resistance, undying, underwater haste, depth
    strider, water breathing, poseidon's bless,
  - JEI: the trial (45), loot fabricator (9) and crushing recipes and their pages,
  - a second player (another client) sees the tester's trial, glitch armor and machines.
- Not tested in game yet: world generation of disruptions (disabled in 1.20.1 too), REI categories in game (REI
  starts with the mod in `runServer`/`runClient`).

## How the port was done

1. Yarn -> Mojang names: a script mapped the 1.20.1 Yarn names to the 1.20.1 Mojang names through the official names
   (class names exactly, members where the mapping was unambiguous). Everything after that was fixed by hand against
   the 26.3 sources (`./gradlew genSources`).
2. Build: Gradle 9.8, the non-remapping Loom (`net.fabricmc.fabric-loom` 1.19.4, 26.x is unobfuscated), Kotlin 2.4.
   Kotlin lambdas are compiled as classes (`-Xlambdas=class`, `-Xsam-conversions=class`) like Kotlin 1.x did:
   invokedynamic lambdas load the classes in their signature when created, which loaded client-only LibGui classes on
   the dedicated server.

## Dependencies

| | 1.20.1 | 26.3 |
|---|---|---|
| LibGui | 8.1.1, Maven | 18.0.1+26.3-rc-2, **the official GitHub release jar** in `libs/maven` (its Maven, server.bbkr.space, is gone) |
| Team Reborn Energy | 2.3.0 | 5.0.0 (energy is a data component now) |
| REI | 12.0.684 | 26.3.824 (compile only) |
| JEI | none | 31.14.0.72 API (compile only), new integration |
| PlayerAbilityLib | 1.8.0 | 1.16.0 (Modrinth Maven) |
| KtDataTagLib | 1.6.7 | **removed**, replaced by `dev.nathanpb.dml.data.tag` (same API, see below) |
| MixinExtras | 0.1.1, bundled | part of Fabric Loader |
| ModMenu | 7.0.0 | 21.0.1 (dev runtime only) |

## API changes and how they were handled

- **Item NBT -> components.** The item data classes (`DataModelData`, `TrialKeyData`, `DeepLearnerData`,
  `ModularArmorData`) keep their property API and store their tags in `minecraft:custom_data`, which is also where the
  vanilla data fixers put the NBT of old items, so items from 1.20.1 worlds keep their data. Stacks inside those tags
  (the Deep Learner's data models, the armor's data model) are written with the item stack codec; old ones in the 1.20
  NBT format don't convert. Energy uses TR Energy's component (the glitch armor's old `energy` NBT key is moved to it
  when first read).
- **Items and blocks** get their id in their properties before construction (`utils/Registration.kt`): items and
  blocks are created and registered in one step. Item models need item definitions (`assets/*/items/*.json`); item
  colors are tint sources there (`dml-refabricated:battery_charge` for the batteries).
- **Glitch sword:** netherite tool material without durability/enchanting components; without energy it hits like an
  empty hand (`getAttackDamageBonus`) instead of losing its attribute modifiers.
- **Glitch armor:** `ArmorItem` and `ArmorMaterial` are gone: `Item.Properties.humanoidArmor` with a record material and
  the equipment asset `assets/dml-refabricated/equipment/glitch.json` (textures moved from
  `minecraft:textures/models/armor`). Attribute modifiers are a stack component now, so the armor rebuilds them from
  its data model in `inventoryTick` (also runs for worn armor). Modifier ids are per armor slot (they used to be random
  UUIDs, and modifiers with the same id replace each other now). Mending is refused through `canBeEnchantedWith`.
- **Block entities** save through `ValueInput`/`ValueOutput` with the same keys; inventories are dropped in
  `preRemoveSideEffects`. `ClientSyncedBlockEntity` keeps the client tag (`#c`) mechanism.
- **Recipes** are codec based and exist only on the server: recipe lookups go through `recipesOf`/`recipeById`; the
  loot fabricator's input filter accepts all pristine matter on the client. REI displays are made on the server
  (`ReiCommonPlugin`, display serializers) and the categories on the client (`ReiPlugin`). The crushing, loot
  fabricator and trial keystone recipes are also sent to the clients with Fabric's recipe synchronization
  (`RecipeSynchronization`) for JEI, whose plugin (`compat/jei`, entrypoint `jei_mod_plugin`) reads them on the client
  (`ClientRecipes`).
- **Menus** use Fabric's `ExtendedMenuType`/`ExtendedMenuProvider` (block position or hand as opening data). LibGui 18
  draws through `GuiGraphicsExtractor`; `SimpleContainer` has no listeners any more (`ListeningContainer`).
- **Rendering:** block entity renderers extract a render state and submit it. The data synthesizer grid and the matter
  condenser core were extra block models; they are drawn as custom geometry now. The disruptions core uses a copy of
  the shulker box model. GUI entity previews render render states (`drawEntity`), and their fake entities get an id.
- **Networking:** typed payloads with stream codecs (same ids).
- **World gen:** features carry their configuration since 26.1 (`DisruptionFeature` is the feature, its codec is
  registered in `FEATURE_TYPE`, `worldgen/configured_feature` became `worldgen/feature`).
- **Loot functions** are codecs; loot tables use `modifier`/`condition`.
- **Data files** were converted by a script: folder names (`recipes` -> `recipe`, `loot_tables` -> `loot_table`, ...,
  `tags/items` -> `tags/item`, ...), recipe ingredients/results, loot tables, advancements.

## Deviations from 1.20.1

- Depth Strider effect: an attribute modifier on the effect (0.25 water movement efficiency per level). Before, a mixin
  added 0.25 per amplifier, so level I did nothing.
- The data synthesizer's comparator output reads its inventory (it read nothing before: the block entity wasn't a
  container).
- The Emeritus Hat is equipped through the equippable component (the mixin for its preferred slot is gone).
- Undying cooldown HUD: a Fabric HUD element after the vanilla HUD (it was a mixin into the status bars).
- Fixed on the way: the matter condenser's pickaxe tag was at `data/minecraft/blocks/...` (never applied), the
  simulacrum pickaxe tag had `"replace": "false"` (a string), the loot fabricator model referenced `#material#material`,
  the first-join advancement rewarded a loot table that doesn't exist (the Patchouli guide), `minecraft:grass` is
  `minecraft:short_grass`, and the convention biome tags are `c:is_*`.
- Fixed: killing a mob didn't add data to the data model in the deep learner (the models are decoded copies of the
  learner's stored stacks; the changed inventory is written back now).
- Fixed: zombie/skeleton/shulker friendly crashed the server with a stack overflow (a port mistake: the check whether a
  mob attacks players called `canAttack`, which the effect hooks).
- Fixed (also broken in 1.20.1): rot resistance never applied (its instance was looked up before it was registered),
  and shift clicking pristine matter into the matter condenser put it in the output slot, where it isn't used.
- Not changed on purpose: after its first item, the loot fabricator processes the following pristine matter without
  waiting again (its progress isn't reset), as in 1.20.1.
- CurseForge/Modrinth/Maven publishing was removed from the build (the plugins don't support Gradle 9, and the
  credentials were the original authors').

## Isolation rules

See `docs/Isolation-rules.md`. Mixin-added members are prefixed with `dml$` (`verifyMixinMembers`), no `@Redirect` or
`@Overwrite` (MixinExtras instead), energy goes through Team Reborn Energy, payload ids and registries use the mod's
namespace, and `verifyJarContents` checks the jars. `check` runs both.
