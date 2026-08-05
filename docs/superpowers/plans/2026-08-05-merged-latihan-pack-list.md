# Merged Latihan Pack List Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display published Firestore Latihan packages alongside the static SQLite packages while preserving Room-backed offline exams.

**Architecture:** Create a pure `LatihanPack` model and merger for SQLite package numbers 1–5 plus remote packages above 5. Fetch published Firestore pack metadata without server ordering, keep each remote document ID for its question query, and make the question refresher accept that ID rather than assuming the legacy migration convention.

**Tech Stack:** Kotlin, Firebase Firestore Android SDK, Room, JUnit 4.

---

### Task 1: Define and test merged package-list behavior

**Files:**
- Create: `app/src/main/java/com/zulfahmi/simukomperawat/model/LatihanPack.kt`
- Create: `app/src/test/java/com/zulfahmi/simukomperawat/model/LatihanPackTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test fun keepsSqlitePackagesAndAppendsNewFirestorePackage() {
    val merged = LatihanPack.merge(listOf(LatihanPack.remote(6, "remote-6")))
    assertEquals(listOf(1, 2, 3, 4, 5, 6), merged.map { it.number })
    assertEquals("remote-6", merged.last().firestoreId)
}

@Test fun ignoresRemoteDuplicateOfSqlitePackage() {
    val merged = LatihanPack.merge(listOf(LatihanPack.remote(3, "remote-3")))
    assertEquals(5, merged.size)
    assertEquals("legacy_latihan_paket_3", merged[2].firestoreId)
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: FAIL because `LatihanPack` is absent.

- [ ] **Step 3: Implement `LatihanPack`**

```kotlin
data class LatihanPack(val number: Int, val firestoreId: String) {
    companion object {
        fun sqlite(number: Int) = LatihanPack(number, "legacy_latihan_paket_$number")
        fun remote(number: Int, firestoreId: String) = LatihanPack(number, firestoreId)
        fun merge(remotePacks: List<LatihanPack>): List<LatihanPack> =
            (1..5).map(::sqlite) + remotePacks.filter { it.number > 5 }.sortedBy { it.number }
    }
}
```

- [ ] **Step 4: Verify GREEN**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.LatihanPackTest`

Expected: `BUILD SUCCESSFUL`.

### Task 2: Fetch published remote packs and refresh by their document ID

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt`

- [ ] **Step 1: Write the failing Firestore-ID test**

```kotlin
@Test fun usesSuppliedFirestorePackId() {
    assertEquals("new-pack", FirestoreQuestionRepository.resolvedPackId("latihan", 6, "new-pack"))
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest`

Expected: FAIL because `resolvedPackId` is absent.

- [ ] **Step 3: Implement metadata fetch and ID-aware refresh**

```kotlin
fun fetchPublishedLatihanPacks(onSuccess: (List<LatihanPack>) -> Unit, onError: (String) -> Unit)
fun refreshPackage(type: String, pack: Int, firestorePackId: String, onReady: () -> Unit, onError: (String) -> Unit)
```

`fetchPublishedLatihanPacks` filters `packs` by `type == "latihan"` and `isPublished == true`, discards invalid rows, then sorts locally. `refreshPackage` uses `firestorePackId` exactly; it retains the existing 20-question atomic Room replacement and fallback behavior.

- [ ] **Step 4: Verify GREEN and compile**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

### Task 3: Render and select merged packages in Latihan

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt`

- [ ] **Step 1: Implement merged rendering**

Use `LatihanPack.merge` after remote metadata succeeds; if it fails, render its SQLite base. Display package numbers through the current adapter. Keep the selected `LatihanPack` in the click handler, retain the existing ad gate, and pass both its number and Firestore ID to `refreshPackage` before calling `openGuide(number)`.

- [ ] **Step 2: Compile and verify all tests**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

Run: `git add app/src/main/java app/src/test/java && git commit -m "feat(latihan): show published remote packs"`
