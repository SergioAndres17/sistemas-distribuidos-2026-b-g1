# How to update `08-uml/diagrams/source/c4-02-containers.drawio`

**Why this is instructions and not the file itself:** I found this file in the
project's existing content, but only in fragments returned by search — not as
one byte-exact copy. A `.drawio` file is XML: one mismatched ID or an
unbalanced tag and the file stops opening in draw.io entirely. Reassembling a
multi-hundred-line XML file from partial search snippets is exactly the kind
of guess that corrupts a file silently until someone tries to open it. These
are GUI steps instead — slower, but they can't produce a broken file.

Open the file at [app.diagrams.net](https://app.diagrams.net) (File → Open From → Device → pick the file from your clone of `synkro-docs`).

---

## 1. Split the database container

Today there is one white `ExecutionEnvironment` box labeled **synkro-db**
`[Container: PostgreSQL 16 — one shared instance per environment (ADR-009)]`,
with four or five small blue cylinders inside it, one per schema
(`auth_schema`, `customers_schema`, `products_schema`, `workflow_schema`, and
today also `sales_schema`).

1. **Find the `sales_schema` cylinder** inside that box and delete it, along
   with the edge connecting `synkro-sales-api` to it (the line is the same
   style as the other three schema edges — solid, gray, labeled "Reads /
   writes [JDBC]").
2. **Update the remaining box's label.** Double-click the white
   `synkro-db` box and change its subtitle to:
   ```
   [Container: PostgreSQL 16 — one shared instance per environment (ADR-009, ADR-012)]
   ```
3. **Copy that white box** (Ctrl+C / Ctrl+V) to make a second
   `ExecutionEnvironment` container, placed to the right of or below the
   first one. Set its properties (right-click → Edit Style, or the format
   panel):
   - `c4Name`: `synkro-mongo`
   - `c4Application`: `Container: MongoDB 7.0 — one instance per environment (ADR-010)`
4. **Copy one of the small blue schema cylinders** into this new box. Set its
   properties:
   - `c4Name`: `sales`
   - `c4Type`: `Database` (keep as-is — the shape's label format already says
     "Database" automatically)
   - `c4Description`: `synkro-sales-db`
5. **Draw one new edge** from `synkro-sales-api` to this new `sales` cylinder.
   Match the style of the existing edges from the other service boxes to
   their schema cylinders (same arrow style, same gray color). Label it:
   - `c4Technology`: `MongoDB driver`
   - `c4Description`: `Reads / writes`

## 2. Mark the Customers portal as Angular

Find the blue `Container` box labeled **synkro-customers-portal**
`[Container: React remote]` (it sits in the row of four portal boxes, same
style and color as the other three).

1. Double-click it and change its subtitle:
   ```
   [Container: Angular 21 — custom element]
   ```
2. **Optional, recommended:** give it a different fill color from the other
   three portals, so the diagram shows the framework split at a glance. The
   other containers use `fillColor=#438DD5` / `strokeColor=#3C7FC0` (C4's
   standard container blue). Pick a distinct color for this one box only —
   for example `fillColor=#C62828` / `strokeColor=#B71C1C` (a red tone,
   matching what this project's Mermaid diagrams in `05-architecture/overview.md`
   and `01-context/overview.md` now use for the same box) — via the Format
   panel's Fill and Line color pickers. Do not change any other box's color.

## 3. Rename the infrastructure note, if the diagram has one

If any text label, title block, or box anywhere in the diagram says
`synkro-infra` by itself (not `synkro-infra-mongo`), update it to
`synkro-infra-postgres`. Based on the fragments I could read, the diagram
does not appear to label a box with the infra repository's own name — the
PostgreSQL and MongoDB containers are labeled by their service name
(`synkro-db`, `synkro-mongo`), not by the repository that defines them — so
this step may be a no-op. Check the title block at the top of the page and
any legend text just in case.

## 4. Export and save

1. File → Export as → SVG.
2. Save it to `08-uml/diagrams/exports/c4-02-containers.svg`, overwriting the
   existing export (same base name as the source file, per this project's
   convention in `diagram-index.md`).
3. Save the `.drawio` source itself back to
   `08-uml/diagrams/source/c4-02-containers.drawio`.
4. Commit both files together, in the same PR as the rest of HU-DOCS-82 —
   `diagram-index.md`'s own rule says an SVG that doesn't match its source is
   a stale diagram.

---

## If this turns out to be more editing than expected

If the real file's layout doesn't match what's described here closely enough
to follow (for example if there's no single shared "white box with cylinders
inside" pattern, or the portal row looks different), that means my read of
the file's structure from search fragments was incomplete in a way these
steps don't anticipate. In that case, export the current file's XML as text
(Extras → Edit Diagram in draw.io gives you the raw XML in a text box) and
paste it back to me — with the full, exact text in hand I can give you an
exact find-and-replace instead of GUI steps.
