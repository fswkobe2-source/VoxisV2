# Custom maps

Drop-in replacements for whole map squares (terrain + object placements) that get packed into
the game cache (server) and js5 cache (client) by `gradlew packCache` / `gradlew install`.

Currently registered squares:

| Square  | Terrain file        | Loc file            | Builder                          |
|---------|---------------------|---------------------|----------------------------------|
| `48_54` | `map/m48_54`        | `map/l48_54`        | `EdgevilleTiles`, `EdgevilleLocs` |

Files live in `src/main/resources/map/`. A square is only replaced if its file is present; a
missing file makes `packCache` fail with `Terrain resource file not found` / `Loc resource file not
found`, so either ship both files or remove the matching `resourceFile(...)` line from the builder.

## File format (OSRS revision 233)

* `m[x]_[z]` – terrain (heights, underlay, overlay, tile flags). Raw, **unencrypted,
  uncompressed** archive-5 group data, exactly as the client decodes it. Packed byte-for-byte.
* `l[x]_[z]` – object (loc) placements. Raw, **decrypted**, uncompressed archive-5 group data.
  `packCache` decodes it, re-encodes it and re-encrypts it with the square's existing XTEA key from
  `.data/cache/xteas.json`, so the file itself must not be encrypted.
* No file extension. `x`/`z` are the map-square coordinates (tile / 64), e.g. tile `3087,3496` is
  in `m48_54` / `l48_54`.
* Loc IDs must exist in the rev 233 cache (or be added through a `LocBuilder` in this repo).

## Exporting a square from RSPSi

1. Point RSPSi at an OSRS cache of the same revision. The simplest source is this repo's vanilla
   cache in `.data/cache/vanilla` (created by `gradlew install`) together with
   `.data/cache/xteas.json` as the XTEA key file. Using a different revision will produce object
   IDs and overlay/underlay IDs that do not match rev 233.
2. Open region `48_54` (region id `12342`, i.e. `48 << 8 | 54`) and make your edits.
3. Export the region as *raw map files* (in RSPSi this is the "Export"/"Dump map" option that
   writes the decoded `m`/`l` pair, **not** "Save to cache"). Saving directly into a cache is not
   useful here: `packCache` deletes and rebuilds `.data/cache/game` and `.data/cache/js5` every run.
4. You should get two files. Name them exactly `m48_54` and `l48_54` (strip any `.dat`/`.gz`
   extension; if the exporter wrote gzip data, gunzip it first) and copy them over the files in
   `src/main/resources/map/`.
5. Run `gradlew packCache` (or `gradlew install`), then `gradlew run`. The client picks up the new
   square through js5 on next login; delete the client's local cache if it shows stale terrain.

## Adding another square

1. Export `m[x]_[z]` / `l[x]_[z]` as above into `src/main/resources/map/`.
2. Add `resourceFile<EdgevilleTiles>("/map/m[x]_[z]")` to `EdgevilleTiles.onPackMapTask()` and
   `resourceFile<EdgevilleLocs>("/map/l[x]_[z]")` to `EdgevilleLocs.onPackMapTask()`.
   Each square may only be registered once across the whole project.

Edgeville neighbours, for reference: `48_55` (north / wilderness ditch), `47_54` (west),
`49_54` (east, Grand Exchange side).

## Verifying a round trip

Packing an unmodified export must leave the caches byte-identical to vanilla. Both `m48_54` and
`l48_54` from `.data/cache/vanilla` were packed through this module and read back identical from
`.data/cache/game` and `.data/cache/js5`, so any difference you see after packing is your edit.
