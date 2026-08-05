# Merged Latihan Pack List Design

## Purpose

Show newly published Firestore Latihan packages without sacrificing the
offline-first SQLite package list and Room-backed exam flow.

## Package list

The Latihan screen combines two sources:

1. SQLite provides packages 1 through 5. They are always shown and remain
   usable when the device has never connected to Firestore.
2. Firestore provides `packs` documents where `type` is `latihan` and
   `isPublished` is `true`. Documents with a `packNumber` greater than 5 are
   appended to the SQLite packages and ordered by `packNumber`.

Firestore documents numbered 1 through 5 do not replace or duplicate the
SQLite package entries. A newly created package must therefore use a unique
`packNumber` greater than 5 and be published before it appears in the app.

## Question download and offline behavior

Each displayed package retains its Firestore document ID. Before its guide is
opened, the app queries `questions` by this exact `packId`, validates exactly
20 questions, then atomically stores them in Room under `type = "latihan"` and
the package number. The exam always reads Room, so it continues without
network after the download completes.

For SQLite packages 1 through 5, the Firestore ID is the migration convention
`legacy_latihan_paket_N`. For appended Firestore packages, the ID is the
document ID returned from the `packs` collection.

If a new Firestore package has no usable cached Room content and its download
fails, it remains listed but the app reports the failure and does not start the
exam. SQLite packages retain their bundled questions as the fallback.

## Architecture

- Add a `LatihanPack` display model containing package number and Firestore ID.
- Extend the repository to fetch published Firestore packs without server-side
  ordering, then sort locally to avoid a composite-index requirement.
- `PackActivity` merges the fixed SQLite records with remote packages and maps
  selection to the matching Firestore ID when refreshing questions.
- The existing access gate and Simulasi flow remain unchanged.

## Testing

- Test merge ordering, deduplication of package numbers 1 through 5, and
  acceptance of a new package 6.
- Test Firestore ID resolution for SQLite and remote packages.
- Test the current 20-question validation and full Android unit/build suite.
