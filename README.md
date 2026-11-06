# SqueezeStudio — Universal Image, Document & Dual-PDF Compression Studio

An Android utility application built with Kotlin, Jetpack Compose, Material 3, and Room for on-device image, document, e-book, archive compression, and multi-format conversion with zero cloud uploads.

---

## Architecture Design Flow Chart

### High-Level System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER (UI)                         │
│                                                                        │
│    ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐    │
│    │ DashboardScreen  │  │ Studio & 2-PDF   │  │  HistoryScreen   │    │
│    │ • Savings Hero   │  │ • % Slider / DPI │  │ • Filter Chips   │    │
│    │ • Pastel Tools   │  │ • Compress/Convert│ │ • Re-Download    │    │
│    │ • Format Catalog │  │ • Before/After   │  │ • Share/Delete   │    │
│    │ • Sample Sandbox │  │ • PDF #1 & PDF #2│  │                  │    │
│    └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘    │
│             │                     │                     │              │
│             └─────────────────────┼─────────────────────┘              │
│                                   │ User Actions                       │
│                                   ▼ State Updates                      │
│                  ┌─────────────────────────────────┐                   │
│                  │      CompressionViewModel       │                   │
│                  │ • StateFlow<StudioUiState>      │                   │
│                  │ • StateFlow<List<Record>>       │                   │
│                  │ • Auto-Detect Suggestions       │                   │
│                  └────────────────┬────────────────┘                   │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ Calls Coroutine APIs
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          DATA & DOMAIN LAYER                           │
│                                                                        │
│                 ┌───────────────────────────────────┐                  │
│                 │       CompressionRepository       │                  │
│                 │ • URI resolution / File cache     │                  │
│                 │ • Sample dispatching              │                  │
│                 │ • History coordination            │                  │
│                 └───────┬───────────────────┬───────┘                  │
│                         │                   │                          │
│            ┌────────────┴───────┐   ┌───────┴────────────┐             │
│            ▼                    │   │                    ▼             │
│  ┌────────────────────┐         │   │         ┌────────────────────┐   │
│  │CompressionDatabase │         │   │         │UniversalCompression│   │
│  │ • Room SQLite DB   │         │   │         │       Engine       │   │
│  │ • CompressionDao   │         │   │         │ • Image Engine     │   │
│  │ • History records  │         │   │         │ • PDF Merger/Scale │   │
│  └────────────────────┘         │   │         │ • OpenXML/ZIP Media│   │
│                                 │   │         │ • Text/Doc-to-PDF  │   │
│                                 ▼   ▼         └────────────────────┘   │
│                   ┌───────────────────────┐                            │
│                   │ Android System I/O    │                            │
│                   │ • MediaStore.Downloads│                            │
│                   │ • FileProvider (Share)│                            │
│                   └───────────────────────┘                            │
└────────────────────────────────────────────────────────────────────────┘
```

---

### End-to-End User Operation Flow Chart

```
                        [ START APP ]
                              │
                              ▼
                    ┌──────────────────┐
                    │ Dashboard Screen │
                    └─────────┬────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
 [ Pick Any File ]    [ Select Tool Card ]   [ Try Built-in Sample ]
        │             (Image / PDF / Office)        │
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                              ▼
                ┌───────────────────────────┐
                │ File-Type Auto-Detection  │
                │  • Inspect format & size  │
                │  • Auto-pick target format│
                │  • Auto-apply preset      │
                │  • Display Smart Badge    │
                └─────────────┬─────────────┘
                              │
                              ▼
                  ┌───────────────────────┐
                  │    Studio Screen      │
                  └───────────┬───────────┘
                              │
                ┌─────────────┴─────────────┐
                │ Select Operation Mode:    │
                ├─────────────┬─────────────┤
                ▼                           ▼
        [ COMPRESS MODE ]           [ CONVERT MODE ]
        • Dial % intensity          • Pick target format
        • Adjust DPI scale            (PNG, JPG, WEBP,
        • Choose preset:              PDF, ZIP, GZIP...)
          - Max Squeeze             • Live Size Estimator
          - Balanced                          │
          - High Fidelity                     │
                │                             │
                └─────────────┬───────────────┘
                              │
                              ▼
                  [ Execute Action Button ]
                              │
              ┌───────────────┴───────────────┐
              │ Background Coroutine Engine:  │
              │ • Decodes & rescales raster   │
              │ • Merges PDF pages & streams  │
              │ • Compacts container media    │
              │ • Writes to app private cache │
              │ • Inserts Room history record │
              └───────────────┬───────────────┘
                              │
                              ▼
                 ┌─────────────────────────┐
                 │ Before & After Preview  │
                 │ • Size savings badge    │
                 │ • Side-by-side inspect  │
                 └────────────┬────────────┘
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
            [ Download File ]     [ Share File ]
            Saves to device       Launches system
            Downloads folder      share sheet via
            via MediaStore        FileProvider
                    │                   │
                    └─────────┬─────────┘
                              │
                              ▼
                 ┌─────────────────────────┐
                 │     History Screen      │
                 │ • Filter by category    │
                 │ • Re-download anytime   │
                 │ • Share or delete       │
                 └─────────────────────────┘
```

---

### Dual-PDF Merge & Compress Flow Chart

```
[ Tap 'Merge 2 PDFs' ]
          │
          ▼
┌─────────────────────────────────────────┐
│       Dual-PDF Merger Interface         │
├─────────────────────────────────────────┤
│  [ PDF Slot #1 ] ──> Select First PDF   │
│  [   Swap ⇅    ] ──> Invert Page Order  │
│  [ PDF Slot #2 ] ──> Select Second PDF  │
│  [ + Add More  ] ──> Optional 3rd+ PDF  │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│ Configure Compression:                  │
│ • Slider (10% - 95%)                    │
│ • DPI scaling                           │
│ • Custom output filename                │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│ Single-Pass Sequential Page Merger:     │
│ • Iterates through all PDF pages        │
│ • Re-renders pages at target DPI        │
│ • Compresses stream into single PDF     │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│ Combined Result Preview:                │
│ • Total original size vs final size     │
│ • Combined page count & savings badge   │
│ • One-tap Download / Share              │
└─────────────────────────────────────────┘
```

---

## How to Use SqueezeStudio

### 1. Compressing Any Image, Document, or Archive
1. Open the app to the **Dashboard**.
2. Tap **"Pick File to Compress"** or select a color tool card:
   - **Universal Images** (*Pastel Mint*): JPG, PNG, WEBP, BMP, HEIC, GIF, SVG.
   - **PDF & 2-PDF Merge** (*Pastel Sky*): PDF documents and multi-PDF merging.
   - **Office & E-Books** (*Pastel Lavender*): DOCX, PPTX, XLSX, ODT, ODS, ODP, EPUB.
   - **Archives & Data** (*Pastel Peach*): ZIP, GZIP, JSON, XML, CSV, TXT, MD.
3. Once the file loads, the **File-Type Auto-Detection Engine** evaluates your file and sets a recommended preset and target format.
4. Adjust the **Compression Intensity Slider** (10% to 95%) or pick a **Preset** (*Max Squeeze*, *Balanced*, *High Fidelity*).
5. Watch the **Estimated Target Size** update live.
6. Tap **"Compress File Now"**.
7. In the result card, view the **Before & After Preview** and tap **"Download File"** to save directly into your device's `Downloads/SqueezeStudio/` folder.

---

### 2. Converting File Formats (e.g., PNG to JPEG, TXT to PDF)
1. On the **Studio** screen, tap the top mode button: **"Convert Format"**.
2. Pick any file from your device or test with a built-in sample.
3. The app auto-detects the format and displays all compatible target formats:
   - **Images**: Convert between `PNG`, `JPEG`, `WEBP`, `BMP`, and `PDF`.
   - **Documents**: Convert `TXT`, `MD`, `RTF`, and `DOCX` into multi-page formatted `PDF`.
   - **Data**: Convert structured data into `ZIP`, `GZIP`, or `PDF`.
4. Tap your desired **Target Output Format** chip (e.g., tap `JPEG` if converting from PNG).
5. Tap **"Convert & Optimize File Now"**.
6. Preview the converted file and tap **"Download File"** or **"Share"**.

---

### 3. Combining 2 PDFs and Compressing into One
1. Tap the **"Merge 2 PDFs"** tab on the navigation bar (or the callout on the Dashboard).
2. Tap **"Select PDF #1"** to choose your first document.
3. Tap **"Select PDF #2"** to choose your second document.
4. *(Optional)* Tap **"Swap PDF #1 & PDF #2 Order"** if you need to reorder the pages.
5. *(Optional)* Tap **"Add Another PDF"** to append a 3rd or 4th document.
6. Enter an output filename (or keep the default).
7. Dial in your desired compression percentage.
8. Tap **"Merge PDFs & Compress Now"**.
9. The app combines all pages sequentially, applies stream compression, and outputs a single lightweight PDF ready for download.

---

### 4. Viewing History & Re-Downloading Files
1. Tap the **"History"** tab on the bottom navigation bar.
2. Filter previously processed files using the category chips (*All*, *Images*, *PDF & 2-PDF Merge*, *Office & E-Books*, *Archives & Data*).
3. Tap **"Download Again"** on any card to export another copy to your device.
4. Tap **"Share"** to send the file via Gmail, WhatsApp, Drive, or nearby share.
5. Tap the trash icon to remove a record, or tap **"Clear All"** to clean up cache and history.

---

### 5. Testing with Built-In Samples (No Device Files Needed)
1. On the **Dashboard**, scroll to the **"Instant Multi-Format Sample Playground"**.
2. Tap any format chip (e.g., *Sample PNG Photo*, *Single 4-Page PDF*, *Word DOCX*, *Archive ZIP*).
3. Or tap **"Try 2 Sample PDFs"** in the 2-PDF Merge card to immediately test dual-PDF merging.
4. A high-resolution test file is generated on the fly and opened in the Studio for instant experimentation.

---

## Supported Formats Matrix

| Category | Supported Formats | Optimization / Conversion Method |
|---|---|---|
| **Images** | `PNG`, `JPG`, `JPEG`, `WEBP`, `BMP`, `HEIC`, `GIF`, `SVG` | Memory-safe bitmap rescaling, chroma subsampling, WebP/JPEG lossy/lossless re-encoding, vector XML minification |
| **PDFs** | `PDF`, `2-PDF Merge` | Render-to-print DPI downscaling, JPEG stream re-compression, multi-document sequential page unification |
| **Office & Docs** | `DOCX`, `PPTX`, `XLSX`, `ODT`, `ODS`, `ODP`, `EPUB` | Container unpack, embedded media downscale & re-encode, XML whitespace compaction, Level-9 DEFLATE repacking |
| **Text & Data** | `TXT`, `RTF`, `MD`, `HTML`, `XML`, `JSON`, `CSV` | Whitespace & comment stripping, formatting, direct conversion to multi-page PDF, GZIP stream or ZIP container compaction |
| **Archives** | `ZIP`, `GZIP` (`.gz`), `JAR` | Recursive media squeeze, high-ratio LZ77 + Huffman stream compression |

---

## Technical Specifications

- **Operating System**: Android 7.0+ (API Level 24 to 36)
- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Local Database**: Android Jetpack Room 2.7.0 (SQLite)
- **Typography**: Locally bundled Google Fonts (*Plus Jakarta Sans* & *Inter*)
- **Storage Target**: Private app sandbox cache + Android `MediaStore.Downloads` (`Downloads/SqueezeStudio/`)
- **Sharing**: Android `FileProvider` (`${applicationId}.fileprovider`)
- **Network Access**: 0% external cloud uploads (100% offline, privacy-safe)

---

## Building and Installing the APK

### Build from Terminal
```bash
gradle assembleDebug
```

The APK is generated in:
- `/apk/SqueezeStudio-debug.apk` *(direct project download folder)*
- `/app/build/outputs/apk/debug/app-debug.apk`

You can download `SqueezeStudio-debug.apk` directly from the AI Studio file explorer or install it on any Android device via ADB:
```bash
adb install -r apk/SqueezeStudio-debug.apk
```
