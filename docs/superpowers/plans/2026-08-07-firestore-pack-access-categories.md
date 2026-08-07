# Firestore Pack Access and Categories Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Organize Latihan packs by non-empty category and enforce each Firestore pack's configured access type while keeping every downloaded pack uniquely available offline.

**Architecture:** Firestore metadata is mapped to a rich `LatihanPack` model, grouped into category choices, and rendered one category at a time. A `SharedPreferences`-backed remote key store assigns each Firestore document ID a stable positive Room package number so identical display numbers never collide in offline storage. `PackActivity` dispatches the selected pack through its source-specific access rule before using the existing download-to-Room flow.

**Tech Stack:** Kotlin, Android Views/View Binding, RecyclerView, Firebase Firestore, Room, SharedPreferences, JUnit 4, Gradle/AGP.

---

## File Structure

- `app/build.gradle` — Android SDK target configuration.
- `app/src/main/java/com/zulfahmi/simukomperawat/model/LatihanPack.kt` — pack metadata, access enum, category grouping, bundled-pack factory.
- `app/src/main/java/com/zulfahmi/simukomperawat/repository/RemotePackKeyStore.kt` — stable Firestore-ID-to-Room-key allocation.
- `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt` — fetch Firestore pack and category metadata and produce remote `LatihanPack` values.
- `app/src/main/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicy.kt` — select open, ad, or premium behavior from a pack.
- `app/src/main/java/com/zulfahmi/simukomperawat/adapter/LatihanPackAdapter.kt` — render named pack cards and return the selected model.
- `app/src/main/res/layout/activity_pack.xml` — category selector above the Latihan grid.
- `app/src/main/res/layout/item_latihan_pack.xml` — compact named pack card.
- `app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt` — active category state, selector dialog, and complete pending-pack state through rewarded ads.
- `app/src/test/java/com/zulfahmi/simukomperawat/model/LatihanPackTest.kt` — grouping and duplicate-number tests.
- `app/src/test/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicyTest.kt` — remote access action tests.

### Task 1: Define Pack Metadata and Category Grouping

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/model/LatihanPack.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/model/LatihanPackTest.kt`

- [ ] **Step 1: Replace the existing merge test with a failing category-and-duplicate test.**

```kotlin
@Test
fun groupsPacksByCategoryWithoutCollapsingDuplicateNumbers() {
    val groups = LatihanPack.groupByCategory(
        listOf(
            LatihanPack.remote(10_000, "jiwa-1", "Paket Jiwa Dasar", "jiwa", "Keperawatan Jiwa", 1, PackAccessType.FREE),
            LatihanPack.remote(10_001, "anak-1", "Paket Anak Dasar", "anak", "Keperawatan Anak", 1, PackAccessType.REWARDED_AD),
        ),
    )

    assertEquals(listOf("Keperawatan Anak", "Keperawatan Jiwa"), groups.map { it.name })
    assertEquals(listOf(10_001), groups[0].packs.map { it.roomPack })
    assertEquals(listOf(10_000), groups[1].packs.map { it.roomPack })
}
```

- [ ] **Step 2: Run the model test and verify it fails because the metadata API does not exist.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: FAIL at compile time for `PackAccessType`, the richer `LatihanPack.remote`, or `groupByCategory`.

- [ ] **Step 3: Implement the minimal immutable models and factories.**

```kotlin
enum class PackAccessType {
    FREE, REWARDED_AD, PREMIUM;

    companion object {
        fun fromWireValue(value: String?): PackAccessType = when (value) {
            "free" -> FREE
            "premium" -> PREMIUM
            else -> REWARDED_AD
        }
    }
}

data class LatihanCategory(val id: String, val name: String, val packs: List<LatihanPack>)

data class LatihanPack(
    val roomPack: Int,
    val firestoreId: String?,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val displayNumber: Int,
    val accessType: PackAccessType,
    val isRemote: Boolean,
) {
    companion object {
        const val BUNDLED_CATEGORY_ID = "bundled"
        const val BUNDLED_CATEGORY_NAME = "Paket Bawaan"

        fun sqlite(number: Int) = LatihanPack(number, "legacy_latihan_paket_$number", "Paket $number", BUNDLED_CATEGORY_ID, BUNDLED_CATEGORY_NAME, number, PackAccessType.REWARDED_AD, false)
        fun remote(roomPack: Int, firestoreId: String, title: String, categoryId: String, categoryName: String, displayNumber: Int, accessType: PackAccessType) = LatihanPack(roomPack, firestoreId, title, categoryId, categoryName, displayNumber, accessType, true)
        fun bundled(): List<LatihanPack> = (1..5).map(::sqlite)
        fun groupByCategory(packs: List<LatihanPack>): List<LatihanCategory> = packs.groupBy { it.categoryId }
            .map { (id, values) -> LatihanCategory(id, values.first().categoryName, values.sortedBy { it.title }) }
            .sortedWith(compareBy<LatihanCategory> { it.id != BUNDLED_CATEGORY_ID }.thenBy { it.name })
    }
}
```

Keep the bundled `roomPack` values 1 through 5. This preserves all existing Room and activity callers.

- [ ] **Step 4: Run the model test and verify it passes.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: PASS.

- [ ] **Step 5: Commit the model change.**

```bash
git add app/src/main/java/com/zulfahmi/simukomperawat/model/LatihanPack.kt app/src/test/java/com/zulfahmi/simukomperawat/model/LatihanPackTest.kt
git commit -m "feat(latihan): group packs by category"
```

### Task 2: Allocate Stable Offline Keys for Remote Packs

**Files:**
- Create: `app/src/main/java/com/zulfahmi/simukomperawat/repository/RemotePackKeyStore.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt`

- [ ] **Step 1: Add a failing test for two remote packages with the same display number receiving distinct Room keys.**

Add this assertion to `LatihanPackTest.kt` after defining a pure allocator in `RemotePackKeyStore.kt`:

```kotlin
@Test
fun allocatesDifferentRoomKeysForDifferentFirestoreIds() {
    val allocator = RemotePackKeyAllocator(firstKey = 10_000)

    assertEquals(10_000, allocator.allocate("jiwa-1"))
    assertEquals(10_001, allocator.allocate("anak-1"))
    assertEquals(10_000, allocator.allocate("jiwa-1"))
}
```

- [ ] **Step 2: Run the model test and verify it fails because `RemotePackKeyAllocator` is missing.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: FAIL at compile time for `RemotePackKeyAllocator`.

- [ ] **Step 3: Implement the pure allocator and its persistent adapter.**

```kotlin
class RemotePackKeyAllocator(
    private val keys: MutableMap<String, Int> = mutableMapOf(),
    private var nextKey: Int = FIRST_REMOTE_ROOM_PACK,
) {
    fun allocate(firestoreId: String): Int = keys.getOrPut(firestoreId) { nextKey++ }

    companion object { const val FIRST_REMOTE_ROOM_PACK = 10_000 }
}

class RemotePackKeyStore(context: Context) {
    private val preferences = context.getSharedPreferences("remote_pack_keys", Context.MODE_PRIVATE)

    fun roomPackFor(firestoreId: String): Int {
        val existing = preferences.getInt(firestoreId, 0)
        if (existing != 0) return existing
        val allocated = preferences.getInt(NEXT_KEY, RemotePackKeyAllocator.FIRST_REMOTE_ROOM_PACK)
        preferences.edit().putInt(firestoreId, allocated).putInt(NEXT_KEY, allocated + 1).apply()
        return allocated
    }

    private companion object { const val NEXT_KEY = "next_key" }
}
```

Instantiate one `RemotePackKeyStore` from the repository context. Use only `roomPackFor(document.id)` for remote Room writes and activity extras; keep `document.id` for Firestore queries.

- [ ] **Step 4: Run the model test and verify it passes.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: PASS.

- [ ] **Step 5: Commit the key allocator.**

```bash
git add app/src/main/java/com/zulfahmi/simukomperawat/repository/RemotePackKeyStore.kt app/src/test/java/com/zulfahmi/simukomperawat/model/LatihanPackTest.kt
git commit -m "feat(latihan): isolate remote offline keys"
```

### Task 3: Fetch Firestore Access and Category Metadata

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt`

- [ ] **Step 1: Add a failing mapper test for Firestore metadata.**

```kotlin
@Test
fun mapsRemotePackMetadataWithFreeAccess() {
    val pack = FirestoreQuestionRepository.toLatihanPack(
        firestoreId = "jiwa-1",
        title = "Jiwa Dasar",
        categoryId = "jiwa",
        categoryName = "Keperawatan Jiwa",
        packNumber = 1,
        accessType = "free",
        roomPack = 10_000,
    )

    assertEquals(PackAccessType.FREE, pack.accessType)
    assertEquals("Keperawatan Jiwa", pack.categoryName)
    assertEquals(10_000, pack.roomPack)
}
```

- [ ] **Step 2: Run the repository test and verify it fails because the mapper is missing.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest`

Expected: FAIL at compile time for `toLatihanPack`.

- [ ] **Step 3: Fetch category documents and map Firestore packs.**

In `fetchPublishedLatihanPacks`, retrieve the published `latihan` pack query and `categories` collection. Once both tasks succeed, build `Map<String, String>` from category ID to category `name`, then call a test-visible pure `toLatihanPack` mapper for each pack document. Use these exact defaults:

```kotlin
val title = document.getString("title").orEmpty().ifBlank { "Paket $packNumber" }
val categoryId = document.getString("categoryId").orEmpty().ifBlank { "uncategorized" }
val categoryName = categoryNames[categoryId].orEmpty().ifBlank { "Kategori Lainnya" }
val accessType = PackAccessType.fromWireValue(document.getString("accessType"))
```

Pass `remotePackKeyStore.roomPackFor(document.id)` to the mapper. Do not filter remote packs by `packNumber`; duplicate numbers are valid. If either metadata request fails, invoke `onError` so `PackActivity` renders bundled packs only.

- [ ] **Step 4: Run the repository test and verify it passes.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest`

Expected: PASS.

- [ ] **Step 5: Commit the Firestore metadata mapping.**

```bash
git add app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt
git commit -m "feat(latihan): read pack access metadata"
```

### Task 4: Select Access Behavior from the Pack Type

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicy.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicyTest.kt`

- [ ] **Step 1: Add failing tests for each remote access type.**

```kotlin
@Test
fun remoteFreePackOpensWithoutAnAd() = assertEquals(
    PackOpenAction.OPEN,
    policy.actionFor(LatihanPack.remote(10_000, "free", "Gratis", "jiwa", "Jiwa", 1, PackAccessType.FREE)),
)

@Test
fun remoteAdPackRequiresAnAd() = assertEquals(
    PackOpenAction.SHOW_REWARDED_AD,
    policy.actionFor(LatihanPack.remote(10_001, "ad", "Iklan", "jiwa", "Jiwa", 2, PackAccessType.REWARDED_AD)),
)

@Test
fun remotePremiumPackShowsAvailabilityMessage() = assertEquals(
    PackOpenAction.SHOW_PREMIUM_MESSAGE,
    policy.actionFor(LatihanPack.remote(10_002, "premium", "Premium", "jiwa", "Jiwa", 3, PackAccessType.PREMIUM)),
)
```

- [ ] **Step 2: Run the policy test and verify it fails because `PackOpenAction` and `actionFor` are missing.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.ads.QuestionPackAccessPolicyTest`

Expected: FAIL at compile time for the new action API.

- [ ] **Step 3: Implement the remote policy while preserving SQLite behavior.**

```kotlin
enum class PackOpenAction { OPEN, SHOW_REWARDED_AD, SHOW_PREMIUM_MESSAGE }

fun actionFor(pack: LatihanPack): PackOpenAction = when {
    !pack.isRemote -> if (requiresRewardedAdForPack(pack.roomPack)) PackOpenAction.SHOW_REWARDED_AD else PackOpenAction.OPEN
    pack.accessType == PackAccessType.FREE -> PackOpenAction.OPEN
    pack.accessType == PackAccessType.PREMIUM -> PackOpenAction.SHOW_PREMIUM_MESSAGE
    else -> PackOpenAction.SHOW_REWARDED_AD
}
```

Keep `canOpenAfterRewardedAdClosed` and `canOpenAfterRewardedAdShowFailed` unchanged so current ad safety behavior remains intact.

- [ ] **Step 4: Run the policy test and verify it passes.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.ads.QuestionPackAccessPolicyTest`

Expected: PASS.

- [ ] **Step 5: Commit the access policy.**

```bash
git add app/src/main/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicy.kt app/src/test/java/com/zulfahmi/simukomperawat/ads/QuestionPackAccessPolicyTest.kt
git commit -m "feat(latihan): honor remote pack access"
```

### Task 5: Build the Category-First Pack UI

**Files:**
- Create: `app/src/main/res/layout/item_latihan_pack.xml`
- Create: `app/src/main/java/com/zulfahmi/simukomperawat/adapter/LatihanPackAdapter.kt`
- Modify: `app/src/main/res/layout/activity_pack.xml`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt`

- [ ] **Step 1: Add the layout and adapter before changing the screen controller.**

Create `item_latihan_pack.xml` as a fixed-width card with a two-line title (`TextView` with `maxLines="2"`), a smaller `Paket %d` label, and a click target covering the whole card. Create a typed adapter:

```kotlin
class LatihanPackAdapter(
    private var packs: List<LatihanPack>,
    private val onSelected: (LatihanPack) -> Unit,
) : RecyclerView.Adapter<LatihanPackAdapter.ViewHolder>() {
    fun submitList(value: List<LatihanPack>) { packs = value; notifyDataSetChanged() }
    // bind title, displayNumber, and root click to onSelected(packs[position])
}
```

Add a full-width `MaterialButton` with ID `btn_select_category` between the subtitle and `recyclerview` in `activity_pack.xml`. Its text is the active category name and package count; it is hidden for Simulasi.

- [ ] **Step 2: Wire category state and render only the active group.**

In `PackActivity`, replace `pendingQuestionPack` and `pendingFirestorePackId` with `pendingLatihanPack: LatihanPack?`. On Latihan metadata success, append `LatihanPack.bundled()`, group with `LatihanPack.groupByCategory`, select the first group, and render only `activeCategory.packs` through `LatihanPackAdapter`.

On category-button click, call:

```kotlin
AlertDialog.Builder(this)
    .setTitle("Pilih kategori")
    .setSingleChoiceItems(categoryLabels, activeIndex) { dialog, selectedIndex ->
        activeCategory = categories[selectedIndex]
        renderActiveLatihanCategory()
        dialog.dismiss()
    }
    .show()
```

The dialog list scrolls natively. On Firestore error, create the single bundled category and render it. Never render a category with an empty `packs` list.

- [ ] **Step 3: Route selection through the complete selected pack.**

```kotlin
private fun onLatihanPackSelected(pack: LatihanPack) {
    when (questionPackAccessPolicy.actionFor(pack)) {
        PackOpenAction.OPEN -> preparePackageAndOpenGuide(pack.roomPack, pack.firestoreId)
        PackOpenAction.SHOW_REWARDED_AD -> {
            pendingLatihanPack = pack
            confirmRewardedAdBeforeOpeningPack(pack.title)
        }
        PackOpenAction.SHOW_PREMIUM_MESSAGE ->
            Toast.makeText(this, "Paket premium belum tersedia.", Toast.LENGTH_SHORT).show()
    }
}
```

Make the rewarded-ad callback retrieve `pendingLatihanPack ?: return`, then call `preparePackageAndOpenGuide(pack.roomPack, pack.firestoreId)`. Keep static Simulasi on `RvAdapter` and its existing number-based behavior.

- [ ] **Step 4: Assemble the debug APK and manually verify UI behavior.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL.

Install the APK. Open Latihan and verify: category selector appears; only non-empty categories are listed; switching category replaces the grid; same displayed number in two categories opens different cached content; free opens immediately; rewarded ad requires completion; premium only shows the availability message.

- [ ] **Step 5: Commit the UI and activity changes.**

```bash
git add app/src/main/res/layout/activity_pack.xml app/src/main/res/layout/item_latihan_pack.xml app/src/main/java/com/zulfahmi/simukomperawat/adapter/LatihanPackAdapter.kt app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt
git commit -m "feat(latihan): select packs by category"
```

### Task 6: Raise Android SDK Targets and Run Full Verification

**Files:**
- Modify: `app/build.gradle`

- [ ] **Step 1: Set both Android SDK values to 36.**

```groovy
android {
    compileSdkVersion 36

    defaultConfig {
        targetSdkVersion 36
    }
}
```

- [ ] **Step 2: Run the complete test suite and build.**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest :app:assembleDebug`

Expected: BUILD SUCCESSFUL with all unit tests passing.

- [ ] **Step 3: Commit the SDK update.**

```bash
git add app/build.gradle
git commit -m "build: target Android SDK 36"
```

- [ ] **Step 4: Inspect the final working tree.**

Run: `git status --short`

Expected: only pre-existing `.idea` changes remain; do not stage them.
