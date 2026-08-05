# Latihan Firestore Offline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep SQLite package selection in Latihan while replacing the chosen package's Room questions with Firestore content before an offline-capable exam starts.

**Architecture:** A generic Firestore question repository builds the migrated `legacy_<type>_paket_<number>` ID, validates 20 remote questions, then atomically writes them to the matching Room type and number. `PackActivity` retains SQLite's static Latihan package grid, invokes the refresh after the existing ad gate, and opens bundled or previously downloaded Room content when refresh fails.

**Tech Stack:** Kotlin, Firebase Firestore Android SDK, Room, JUnit 4, View Binding.

---

### Task 1: Generalize Firestore question mapping

**Files:**
- Create: `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/repository/ExperimentalQuestionMapper.kt`
- Create: `app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/repository/ExperimentalQuestionMapperTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test
fun buildsFirestoreIdForSqliteLatihanPackage() {
    assertEquals("legacy_latihan_paket_3", FirestoreQuestionRepository.firestorePackId("latihan", 3))
}

@Test
fun mapsDownloadedQuestionToSelectedLatihanPackage() {
    val question = FirestoreQuestionMapper.toRoomQuestion(validRemoteQuestion, "latihan", 3)
    assertEquals("latihan", question.type)
    assertEquals(3, question.pack)
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest --tests com.zulfahmi.simukomperawat.repository.ExperimentalQuestionMapperTest`

Expected: FAIL with unresolved generic repository or mapper symbols.

- [ ] **Step 3: Implement minimal generic contract**

```kotlin
fun firestorePackId(type: String, pack: Int): String {
    require(type == "latihan" || type == "simulasi")
    require(pack > 0)
    return "legacy_${type}_paket_$pack"
}

fun toRoomQuestion(remote: RemoteFirestoreQuestion, type: String, pack: Int): Question
```

- [ ] **Step 4: Verify GREEN**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest --tests com.zulfahmi.simukomperawat.repository.ExperimentalQuestionMapperTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/zulfahmi/simukomperawat/repository app/src/test/java/com/zulfahmi/simukomperawat/repository && git commit -m "refactor(latihan): map Firestore packages"`

### Task 2: Refresh a selected Latihan package before the guide

**Files:**
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/database/UkomDao.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt`

- [ ] **Step 1: Write the failing batch test**

```kotlin
@Test(expected = IllegalArgumentException::class)
fun rejectsRefreshWithFewerThanTwentyQuestions() {
    FirestoreQuestionMapper.toRoomQuestions(List(19) { validRemoteQuestion }, "latihan", 2)
}
```

- [ ] **Step 2: Verify RED**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest`

Expected: FAIL with an unresolved generic batch mapper.

- [ ] **Step 3: Implement atomic refresh and fallback**

```kotlin
fun refreshPackage(type: String, pack: Int, onReady: () -> Unit, onError: (String) -> Unit) {
    firestore.collection("questions").whereEqualTo("packId", firestorePackId(type, pack)).get()
    // Map exactly 20 questions, then dao.replaceQuestions(type, pack, questions).
    // If fetch or validation fails, call onReady when dao.countByTypeAndPack(type, pack) == 20.
}
```

In `PackActivity`, call `refreshPackage("latihan", selectedPack, ...)` after the current access gate. Both a successful refresh and a valid local fallback call `openGuide(selectedPack)`.

- [ ] **Step 4: Verify GREEN and compile**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepositoryTest :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/zulfahmi/simukomperawat/database/UkomDao.kt app/src/main/java/com/zulfahmi/simukomperawat/activity/PackActivity.kt app/src/main/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepository.kt app/src/test/java/com/zulfahmi/simukomperawat/repository/FirestoreQuestionRepositoryTest.kt && git commit -m "feat(latihan): refresh package for offline use"`

### Task 3: Remove Experimental and retain Simulasi

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/MainActivity.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/model/QuestionMode.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/GuideActivity.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/QuestionActivity.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/ResultActivity.kt`
- Modify: `app/src/main/java/com/zulfahmi/simukomperawat/activity/ExplanationActivity.kt`
- Delete: `app/src/main/java/com/zulfahmi/simukomperawat/model/ExperimentalPack.kt`
- Delete: `app/src/test/java/com/zulfahmi/simukomperawat/model/ExperimentalPackTest.kt`
- Modify: `app/src/test/java/com/zulfahmi/simukomperawat/model/QuestionModeTest.kt`

- [ ] **Step 1: Replace the mode test**

```kotlin
@Test fun latihanRemainsUntimedWithTwentyQuestions() {
    assertEquals(20, QuestionMode.LATIHAN.totalQuestions)
    assertFalse(QuestionMode.LATIHAN.isTimed)
}

@Test fun simulasiRemainsTimedWithOneHundredQuestions() {
    assertEquals(100, QuestionMode.SIMULASI.totalQuestions)
    assertTrue(QuestionMode.SIMULASI.isTimed)
}
```

- [ ] **Step 2: Remove Experimental UI and code**

Delete `btn_experimental`, its string, the Experimental enum value, Experimental model/repository/tests, and Experimental mode branches. Retain the static Latihan and Simulasi grids and keep the existing guide, result, and explanation behavior for those two modes.

- [ ] **Step 3: Verify mode behavior and compile**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest --tests com.zulfahmi.simukomperawat.model.QuestionModeTest :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

Run: `git add app/src/main app/src/test/java/com/zulfahmi/simukomperawat/model && git commit -m "refactor(latihan): remove experimental menu"`

### Task 4: Verify the offline flow

**Files:** Verify only.

- [ ] **Step 1: Run all unit tests and build the APK**

Run: `JAVA_HOME=/Users/zulfahmi/.sdkman/candidates/java/17.0.13-tem sh gradlew :app:testDebugUnitTest :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Manual device check**

1. Online: choose Latihan package 1; confirm 20 Firestore documents download, then the guide opens.
2. Offline: choose the same package; confirm guide and exam open from Room.
3. Offline before refresh: choose another package; confirm bundled SQLite questions open.
4. Confirm Simulasi package count and timer remain unchanged.
