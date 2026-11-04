# SqueezeStudio — Image & Document Conversion Tool with File-Type Auto-Detection

SqueezeStudio is being enhanced with an integrated **Conversion Tool** and **Intelligent File-Type Auto-Detection Engine** that automatically determines the best output format and compression preset upon file selection, while preserving all existing compression and 2-PDF merge functionality without altering previous behavior.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product specifications were confirmed during interactive clarification:
> - **Navigation & Mode Placement**: An integrated **Compress vs Convert** mode toggle inside the Studio screen so users can effortlessly switch between pure compression and format conversion for any loaded file.
> - **Essential Conversion Formats**:
>   - **Images**: Seamless bidirectional conversion between `PNG`, `JPEG / JPG`, `WEBP`, `BMP`, `GIF`, and `SVG`.
>   - **Documents to PDF**: Conversion of text, markup, and office files (`TXT`, `RTF`, `MD`, `DOCX`) into crisp multi-page or single-page `PDF` documents with customizable layout rendering.
>   - **Data & Text Conversion**: Structured conversion between `JSON`, `CSV`, `TXT`, `HTML`, `XML`, and archive bundling (`ZIP`, `GZIP`).
> - **File-Type Auto-Detection & Smart Recommendations**:
>   - Automatically inspects the mime-type, header signature, and extension of any uploaded file.
>   - Automatically pre-selects the optimal target format (e.g. huge `PNG` or `BMP` auto-suggests `WEBP` or `JPEG`; heavy `TXT`/`DOCX`/`MD` auto-suggests `PDF`) and preset.
>   - Displays a prominent **Smart Suggestion** chip/badge explaining the rationale (e.g. *"Auto-Detected PNG: WEBP recommended for 70%+ smaller size with lossless transparency"*).
> - **Keep All Previous Features**: Existing dashboard, 2-PDF merger, custom compression percentage sliders, preset chips, before/after comparisons, and download to device remain completely intact.
> - **Continuous APK Generation**: Generates and updates `apk/SqueezeStudio-debug.apk` upon task completion so you can easily download and test the app.

---

## 1. Overview & Core Concept

- **What It Adds**:
  1. **Mode Switcher (Compress ↔ Convert)**: Toggling into "Convert" reveals a dedicated format selection grid with all valid conversion targets for the selected file type.
  2. **File-Type Auto-Detection Engine**: Analyzes file attributes and automatically selects the most space-saving or standards-compliant target format along with recommended quality presets.
  3. **Universal Document-to-PDF & Cross-Image Converter**: Native on-device conversion without third-party cloud uploads.
  4. **Dedicated APK Export**: Pre-compiled APK placed in `/apk/SqueezeStudio-debug.apk`.

---

## 2. Technical Architecture & Data Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        StudioAndPdfMergeScreen                         │
│           [ Mode Switcher: 🗜️ Compress  |  🔄 Convert & Format ]          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┴────────────────────────────┐
       ▼                                                         ▼
┌──────────────────────────────┐        ┌──────────────────────────────────┐
│     Auto-Detection Engine    │        │      Conversion Target Matrix    │
│ • Detects mime & ext         │        │ • Images: PNG, JPG, WEBP, BMP    │
│ • Selects optimal target     │───────>│ • Docs to PDF: TXT, MD, RTF, DOCX│
│ • Badges "Smart Suggestion"  │        │ • Data/Archives: ZIP, GZIP, JSON │
└──────────────────────────────┘        └──────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       UniversalCompressionEngine                       │
│ • Image Converter (Bitmap decode + target format re-encode)            │
│ • Document to PDF Renderer (TextPaint / Canvas / PdfDocument)          │
│ • Data / Archive Packer (Deflater / GZIPOutputStream)                  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Key Implementation Details

1. **`FormatConverterEngine`**:
   - Extends the data layer with dedicated format conversion logic:
     - `PNG` / `BMP` / `HEIC` / `GIF` ➔ `WEBP`, `JPEG`, `PNG`, `BMP`
     - `TXT` / `MD` / `RTF` / `HTML` ➔ `PDF` (paginated PDF document generation via `android.graphics.pdf.PdfDocument`)
     - `JSON` ➔ `CSV` / `TXT` / `GZIP` / `ZIP`
2. **`SmartRecommendationAdvisor`**:
   - Encapsulates auto-detection logic:
     - Large uncompressed images (`PNG`, `BMP`) ➔ Suggests `WEBP` (Balanced)
     - Photos (`JPG`) ➔ Suggests `WEBP` or High-Fidelity `JPG`
     - Text & Docs (`TXT`, `MD`, `DOCX`) ➔ Suggests `PDF`
     - Bundles ➔ Suggests `ZIP` or `GZIP`
3. **Studio UI Integration**:
   - Adds a sleek Material 3 segment/pill toggle at the top of the Studio screen (`Compress` vs `Convert`).
   - When `Convert` is selected, shows a format picker with auto-selected recommendation and a **"Smart Suggestion"** badge.
   - Preserves headline-only aesthetics as previously requested.
4. **APK Build**:
   - Executes `gradle assembleDebug` and copies the output to `/apk/SqueezeStudio-debug.apk`.
