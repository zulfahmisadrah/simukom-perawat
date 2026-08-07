# Firestore Pack Access and Categories Design

## Goal

Make the Latihan screen use the Firestore pack access policy and organize
available packs by category without allowing duplicate pack numbers to overwrite
offline data.

## Scope

- Set `compileSdkVersion` and `targetSdkVersion` to 36.
- Read Firestore pack `title`, `categoryId`, and `accessType` values.
- Show only categories that contain at least one visible Latihan pack.
- Let the user choose one active category from a compact, scrollable selector.
- Show only the active category's packs in the existing grid.
- Keep SQLite packs 1-5 available under the `Paket Bawaan` category.
- Apply Firestore access policy to remote packs:
  - `free`: download and open directly.
  - `rewarded_ad`: require a completed rewarded ad.
  - `premium`: show `Paket premium belum tersedia.`
- Preserve the existing SQLite access policy: packs above 1 require a rewarded
  ad.

## Data Model

`LatihanPack` becomes a display and access model with:

- `roomPack`: unique positive integer used by Room and all existing exam
  activities.
- `firestoreId`: Firestore pack document ID for remote question downloads.
- `title`: text rendered on the pack card.
- `categoryId` and `categoryName`: grouping fields.
- `accessType`: `free`, `rewarded_ad`, or `premium`.
- `isRemote`: distinguishes Firestore packs from bundled SQLite packs.

Firestore package metadata is joined with the `categories` collection by
`categoryId`. Categories without any published package are omitted.

## Offline Identity

Firestore permits the same `packNumber` in different categories, while the
existing Room table keys cached questions by an integer package number. The app
will allocate a stable, positive local Room package number for every Firestore
document ID and persist that mapping in a dedicated `SharedPreferences` file.

The allocation starts above the SQLite range, so remote cache rows cannot
replace bundled SQLite questions. The Firestore ID remains the network key;
the allocated Room number is only an internal cache key. Reinstalling the app
clears both the mapping and the cached database, so no stale mapping remains.

## User Experience

The Latihan screen has a tappable category selector above the package grid:

`Kategori: Keperawatan Jiwa (3 paket) ▾`

It opens a single-choice, scrollable category list. The first visible category
is selected on load. Choosing a category updates only the grid, avoiding a long
vertical page when many categories or packs exist. Pack cards render their
Firestore title so duplicate numbers are unambiguous; the number is secondary
metadata where shown.

`Paket Bawaan` is present because it contains the five SQLite packs. It follows
the legacy access gate so existing behavior is unchanged.

## Access Flow

1. The user chooses a pack from the active category.
2. A SQLite pack uses the legacy number-based gate.
3. A remote `free` pack starts offline preparation immediately.
4. A remote `rewarded_ad` pack stores its complete selected-pack object, then
   opens only after a reward is earned.
5. A remote `premium` pack stays closed and shows the availability message.
6. Remote preparation downloads questions using Firestore `packId`, writes them
   to Room with the unique local package key, then opens the existing guide and
   exam activities with that local key.

## Error Handling

- If package metadata or categories cannot be fetched, render the bundled
  `Paket Bawaan` category only.
- A missing or unknown remote `accessType` falls back to `rewarded_ad`.
- Incomplete question data continues to surface the existing explicit
  validation message and does not replace a complete local cache.

## Verification

Unit tests cover category grouping, duplicate Firestore pack numbers receiving
different Room keys, and all remote access actions. The complete Android unit
test suite and debug APK assembly must pass after the SDK update.
