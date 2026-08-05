# Latihan Firestore Offline Design

## Purpose

Remove the separate Experimental menu and make **Latihan** the only entry point
for these packs. Its package grid continues to come from the bundled SQLite
database, while the questions for a selected package are refreshed from
Firestore and stored in Room before the exam begins.

## User flow

1. The home screen shows Latihan but no Experimental button.
2. Latihan always lists packages 1 through 5 from the existing SQLite-backed
   definition, so opening the list does not require network access.
3. After the user selects a package and completes the existing access gate,
   the app requests its Firestore questions.
4. The app waits until it has mapped and saved exactly 20 valid questions in
   Room, then opens the existing guide and exam screens.
5. When there is no connection or Firestore data is invalid, the app opens the
   same package from the existing Room data instead. This is the bundled
   SQLite content on first use, or the last successfully downloaded Firestore
   version after a refresh.

## Data mapping

The SQLite package number maps directly to the migration's Firestore pack ID:

- SQLite Latihan package `N` maps to Firestore `packId`
  `legacy_latihan_paket_N`.
- Firestore `questions` are queried only by that `packId`.
- A downloaded question maps to the existing Room `Question` entity with
  `type = "latihan"` and `pack = N`.

Saving uses one Room transaction: replace the existing `latihan` questions for
the selected package only after all 20 Firestore documents have been validated
and mapped. A failed download never clears usable local questions.

## Architecture

- Replace the Experimental-specific repository with a focused Firestore
  question refresher that accepts the local question type and package number.
- Keep `PackActivity` as the shared pack-selection screen. Its Latihan branch
  provides the static SQLite package list; selecting a Latihan package invokes
  the refresher before `GuideActivity` is launched.
- Delete the Experimental button, enum mode, models, tests, and code paths.
- Simulasi behavior remains unchanged.

## Errors and offline behavior

- Show a brief preparation state while a selected Latihan package refreshes.
- If Firestore cannot return a complete valid 20-question package, count the
  matching Room rows. Open the package when 20 local questions are present;
  otherwise show an error and remain on the package grid.
- Room is the exam source for every run, so the exam itself does not depend on
  network after the preparation step completes.

## Testing

- Test Firestore pack-ID construction for each SQLite package number.
- Test remote-question mapping preserves `latihan` and the chosen package
  number rather than an Experimental cache slot.
- Test a batch must contain exactly 20 valid questions before it can replace a
  Room package.
- Build the debug APK and run the full unit-test suite.

## Out of scope

- Downloading every package automatically before the user selects one.
- Changing the existing rewarded-ad access policy.
- Altering Simulasi data or timing behavior.
