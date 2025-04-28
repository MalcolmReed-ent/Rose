## Changes

# Rose EPUB Reader Changelog

This update introduces significant improvements to the original Mizumi fork, focusing on enhanced functionality, user experience, and EPUB parsing capabilities.

---

## **Added Features**

### **Settings Screen and Functionality**
- **Font Customization:**
  - Adjustable font size (12sp to 24sp).
  - Font family cycling (serif, sans-serif, monospace).
- **Library Preferences:**
  - Sort chapters in ascending or descending order.
- **Persistent Settings:**
  - Implemented DataStore for saving user preferences.

---

## **Removed Features**

- **Deprecated Functionality:**
  - Text-to-Speech and translation capabilities.
  - Search and filter buttons from the top bar.
  - Incognito mode, stats, and settings from the dropdown hamburger menu.
  - Navigation bar sections: Explore and History (reverted to v1.0 NovelDokusha style).
  - Networking dependencies from `build.gradle.kts`.
- **UI Cleanup:**
  - Removed filter button, hamburger menu, and tracking button from `bookscreen.kt`.
  - Removed book tracking buttons and features from `bookactionbar.kt`.
  - Removed Profile/More screen from the navbar and main screen, leaving only Library and Settings.

---

## **UI/UX Improvements**

- **Custom Branding:**
  - Added a custom app logo created using ImageMagick.
- **Reader Screen Enhancements:**
  - Single tap now displays both the reader top bar and device navigation bar for quick access.
  - Improved overall readability and interaction.
- **Settings Screen:**
  - Intuitive controls for font size (slider) and font family (cycling button).
  - Toggle switches for library preferences.

---

## **EPUB Parser Enhancements**

- **Improved Metadata Handling:**
  - Robust description parsing from multiple metadata sources.
  - Enhanced cover image extraction.
- **Chapter and TOC Parsing:**
  - Simplified parsing for both NCX and non-NCX EPUBs.
  - Better chapter title detection and ordering.
  - More reliable content extraction for unusual EPUBs with non-standard TOC specifications.
- **Library Import:**
  - Improved parsing for better compatibility with a wider range of EPUB files.

---

## **Data Management**

- **Persistent Storage:**
  - Implemented DataStore for managing app settings:
    - Reader preferences (font size, font family).
    - Library preferences (chapter sort order).
    - App state persistence.

---

This update focuses on refining the user experience, improving EPUB parsing, and streamlining the app’s functionality. Let us know your feedback!
