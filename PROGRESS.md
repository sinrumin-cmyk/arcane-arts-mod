# Arcane Arts — Статус проекта

**Мод:** магия для Minecraft 1.21.1 (NeoForge 21.1.252), ModDevGradle 2.0.148, Java 21
**Репо:** https://github.com/sinrumin-cmyk/arcane-arts-mod
**Сборка:** GitHub Actions (workflow `.github/workflows/build.yml`) — на телефон НЕ собирать, телефон страдает.

---

## ✅ СДЕЛАНО

### Инфраструктура
- [x] `settings.gradle`, `build.gradle`, `gradle.properties` — каркас ModDevGradle, NeoForge 21.1.252
- [x] `src/main/resources/META-INF/neoforge.mods.toml` — дескриптор мода (modid `arcanearts`)
- [x] `pack.mcmeta`
- [x] GitHub Actions workflow: JDK 21 + Gradle 8.10.2 + `gradle build` + артефакт + nightly-release на main
- [x] Репозиторий создан и подключён (origin/main)

### Java-код (atomic, 33 файла, всё общее/server-side ядро)
- [x] `ArcaneArts.java` — **ВРЕМЕННАЯ заглушка** (нужна финальная разводка реестров, см. TODO)
- [x] `registry/`:
  - `ModItems` — 3 посоха, пыль/кристалл маны, 10 томов, BlockItem-ы
  - `ModBlocks` — arcane_altar, mana_ore, mana_crystal_block
  - `ModEntities` — magic_bolt, meteor, singularity (+трекинг/апдейты)
  - `ModParticles` — arcane_spark, arcane_rune, arcane_ring (+ хелперы с цветом/скейлом)
  - `ModComponents` — `learned_spells` (List\<ResourceLocation\>), `selected_spell`
  - `ModAttachments` — `player_mana` (persistent codec + copyOnDeath)
  - `ModCreativeTabs` — вкладка Arcane Arts
- [x] `particle/` — `ArcaneParticleOptions` (r,g,b,scale + codec/writeToStream), `ArcaneParticleType` (Deserializer)
- [x] `spell/` — `Spell` (база), `SpellRegistry` (11 заклинаний), `SpellManager` (каст: мана/КД/выученность + синхронизация), `WandTier` (3 тира: power/cost/cooldown)
- [x] `spell/spells/` — все 11 заклинаний:
  - `ArcaneBoltSpell` (болт), `FireballSpell` (взрыв+огонь), `LightningSpell` (молния + цепь по 3 целям),
  - `FrostNovaSpell` (AoE заморозка), `MeteorSpell` (метеор с неба), `SingularitySpell` (чёрная дыра),
  - `BlinkSpell` (телепорт), `ManaShieldSpell` (щит), `HealSpell` (хил), `HasteSpell` (спид+хаст), `FlightSpell` (полёт через abilities)
- [x] `entity/` — `MagicBoltEntity` (болт/фаербол: синк цвета, шлейф, взрыв), `MeteorEntity` (падение, трейл, детонация + FX-тряска), `SingularityEntity` (притяжение сущностей, имплозия)
- [x] `item/` — `WandItem` (компоненты выученных/выбранного, ПКМ каст, Shift+ПКМ смена, тултип), `SpellTomeItem` (том: спелл-сапплаер, тултип)
- [x] `mana/PlayerMana` — мана/maxMana/flightTicks + Codec для персиста
- [x] `util/ParticleHelper` — ring/spiral/sphere/line/burst из server-частиц

---

## ❌ ОСТАЛОСЬ (порядок работы)

### 1. Критично для компиляции (код ссылается, а файлов нет)
- [ ] `network/ModNetwork.java` — `RegisterPayloadHandlersEvent`, registrar("1"), `sendTo(ServerPlayer, payload)` через vanilla `ClientboundCustomPayloadPacket`, `sendFx(level,x,y,z,type,range)` → ближайшим игрокам. Константы `FX_METEOR=0`, `FX_SINGULARITY=1`, `FX_NOVA=2`
- [ ] `network/` пакеты (records impl `CustomPacketPayload` + `StreamCodec.ofMember`):
  - `CastSpellPacket(ResourceLocation spell)` — C2S, каст выбранного
  - `SelectSpellPacket(ResourceLocation spell)` — C2S, смена выбранного на посохе (mainhand→offhand поиск)
  - `ManaSyncPacket(int mana, int maxMana)` — S2C
  - `CooldownsPacket(List<Entry(spell,endTick)>)` — S2C (+ метод `active(now, map)`)
  - `ClientFXPacket(int type, x, y, z)` — S2C (тряска камеры)
  - Клиентские хендлеры пушить в `ClientData` через лямбды (ленивая загрузка клиентских классов — ок для выделенного сервера)
- [ ] `block/ArcaneAltarBlock.java` — ПКМ(том): проверка XP≥3 lvl, посох в другой руке/хотбаре, учим спелл в посох, shrink тома, FX+звук; ПКМ(4× mana_crystal): +20 maxMana; `animateTick` руны/искры; VoxelShape пьедестала; `useItemOn` (1.20.5+ API)
- [ ] `block/ManaOreBlock.java` — просто Block + `animateTick` искры
- [ ] `mana/ManaManager.java` — тики: реген маны (1/4 тика), полёт (tick flight, отключение mayfly по истечении + SLOW_FALLING), sync dirty-маны, PlayerLoggedIn/LoggedOut (полн. синк + очистка КД SpellManager)
- [ ] `command/ArcaneCommands.java` — `/arcanearts learn all|<id>`, `mana set`, `max set` (RegisterCommandsEvent)
- [ ] `ArcaneArts.java` — финальная разводка: register всех DeferredRegister, `modBus.addListener(ModNetwork::register)`, NeoForge.EVENT_BUS листенеры ManaManager + commands, `id()` хелпер

### 2. Клиент (весь пакет `client/`)
- [ ] `client/ArcaneClient.java` — mod-bus: RegisterRenderers (3 entity-рендера), RegisterParticleProvidersEvent (3 спрайт-набора), RegisterKeyMappingsEvent, RegisterGuiLayersEvent
- [ ] `client/KeyBindings.java` — R = колесо (hold), G = быстрый каст
- [ ] `client/ClientData.java` — кэш маны/КД (из пакетов)
- [ ] `client/ClientEvents.java` — game-bus: ClientTick (открытие колеса/quickcast, ambient частицы щита/полёта), `ViewportEvent.ComputeCameraAngles` → тряска
- [ ] `client/gui/ManaHud.java` — орб маны (справа-снизу, scissor-заполнение, число), полоса иконок заклинаний над хотбаром (выбор рамкой, КД затемнение+секунды)
- [ ] `client/gui/WheelScreen.java` — радиальное колесо: сегменты по кругу из выученных спеллов, hover мышкой, вращающиеся рунные кольца (_pose rotate + текстуры), ЛКМ/отпускание R = выбор+каст (CastSpellPacket), имя/сто в центре, красным при нехватке маны
- [ ] `client/particle/` — 3 текстурных партикла extends TextureSheetParticle (spark/rune/ring, fadeIn/fadeOut, тинт из options)
- [ ] `client/render/` — `MagicBoltRenderer` (billboard glow-квад по RenderType.LIGHTNING + ядро), `MeteorRenderer` (большой оранж glow + halo), `SingularityRenderer` (тёмное ядро + пурпур glow). `getTextureLocation` → любой валидный RL

### 3. Ресурсы (`src/main/resources`)
- [ ] `assets/arcanearts/lang/ru_ru.json` + `en_us.json` (~120 ключей: спеллы/предметы/интерфейс/messages/tooltips)
- [ ] `models/item/` — посохи (`generated`), dust/crystal, 10 томов; `models/block/` + `blockstates/` — altar (кубическая модель: top/side/bottom), ore, crystal_block
- [ ] `data/arcanearts/loot_table/blocks/` — altar dropSelf, ore→dust 1-3 (ore_drops+fortune), crystal_block dropSelf
- [ ] `data/arcanearts/recipe/` — ~15 рецептов (посохи: stick+dust/crystals/nether_star; тома: book+crystal+ингредиент; block 9×crystal и обратно)
- [ ] Worldgen руды: `configured_feature/mana_ore.json`, `placed_feature/mana_ore.json` (оба target: stone+deepslate → mana_ore), `neoforge/biome_modifier/add_mana_ore.json` (is_overworld, underground_ores)
- [ ] `data/minecraft/tags/block/mineable/pickaxe.json` + `needs_iron_tool.json`

### 4. Текстуры — генератор `tools/gen_textures.py` (python3, PNG вручную через zlib)
- [ ] items 16×16: 3 посоха (палочка атласная: deprecated→stick diagonal + гем по тиру), mana_dust (пыль кучкой), mana_crystal (кристалл), 10 томов (книга + цветной глиф)
- [ ] blocks 16×16: mana_ore (камень с кристалл-жилами светящимися), mana_crystal_block, altar_{top,side,bottom}
- [ ] particles 16×16 маски (белые для тинта): arcane_spark (мягкая точка с glow), arcane_rune (глиф), arcane_ring (кольцо)
- [ ] gui: `orb_empty/orb_fill` 32×32, `spell/<path>.png` 11 штук 32×32 (болт/пламя/молния/снежинка/комета/дыра/жемчуг-стрелка/щит/сердце-крылья/перо), `wheel_ring1/wheel_ring2` 256×256 (руны по кругу, полупрозрачно)

### 5. Сборка и фиксы
- [ ] Push → GitHub Actions → **кстати прочитать лог через `gh run view --log-failed`** — ожидаю расхождения API 1.21.1 (ParticleOptions deserializer, attachment `.persistent()`, `TickEvent.ServerTickEvent.Post`, blit RenderType, etc.) — фиксить по ошибкам компилятора
- [ ] После зелёного билда: jar из Actions artifacts / nightly release
- [ ] (опц.) записать гайд в README.md

---

## ⚠️ ИЗВЕСТНЫЕ РИСКИ АПИ (проверить по логам CI)
1. `ParticleOptions.writeToStream(RegistryFriendlyByteBuf)` — в 1.21.1 может быть `FriendlyByteBuf`
2. `AttachmentType.Builder.persistent(Codec)` — сигнатра/название может отличаться
3. `ParticleOptions.Deserializer.fromNetwork(RegistryFriendlyByteBuf)` — см. п.1
4. `TickEvent.ServerTickEvent.Post/Pre` — вложенные классы в 1.21 NeoForge
5. `RegisterGuiLayersEvent` + `LayeredDraw.Layer render(GuiGraphics, DeltaTracker)` — сигнатура
6. `guiGraphics.blit(RenderType::guiTextured, ...)` — 1.20.5+ стиль
7. `Entity`.`addAdditionalSaveData` — имя, в свежих версиях `saveAdditional`?
8. `Level.explode(...)` и custom payload-отправка через `connection.send(new ClientboundCustomPayloadPacket(...))`

## 🎮 ПЛАНИРУЕМЫЙ ГЕЙМПЛЕЙ (уже отражено в коде)
- Мана: 100 базово (+20 за 4 кристалла на алтаре), реген 5/сек
- Заклинания вешаются НА ПОСОХ (data components), выучиваются томами на алтаре за 3 XP-уровня
- Тиры посохов: ученик ×1.0 / мистик ×1.35 сила −15% мана −30% КД / архимаг (nether star) ×1.8 −30% мана −55% КД
- Колесо: удерживать **R**, быстрый каст **G**, Shift+ПКМ — след. заклинание
