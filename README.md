# TinyTimeTracker (Resurrected)

This project was recovered from a [CVS export of the original TinyTimeTracker](https://sourceforge.net/projects/tinytimetracker/) by Russell Black from 2004 which was rendered non-functional around 2014.

> ⚠️ No guarantees of functionality, security, or bug-free operation. This is a legacy resurrection effort — proceed with curiosity and caution.

---

## 🕒 What It Does

TinyTimeTracker is a **minimally intrusive floating time tracker** for desktop environments. It:

- Tracks time spent on your current project
- Logs entries into `.xls` files:
  - One sheet per day
  - One workbook per week
- Runs on **Windows**, **Linux**, and likely any GUI that supports **Java 25**

To stop tracking, simply enter a null project name like `off`.

---

## 💡 Why Use This?

Because it's:

- Lightweight and unobtrusive
- Easy to run — no setup wizard, no accounts, no cloud
- Free and open source
- Surprisingly effective for solo time tracking

---

## 📦 Binaries?

None provided (yet). But you can build it yourself — it's fast and easy.

---

## 🛠️ How to Build and Run

### Option 1: Ant (original method)

```bash
ant run
```

### Option 2: Gradle (modernized)

```bash
cd tinytimetracker
./gradlew run &
```

Output files (your data) will appear in your home directory under:

```
~/timecards/
```

---

## 🧹 Notes

- The original Java Web Start and `deploy` targets have been removed — they no longer work.
- This version compiles and runs on **Java 25**.
- Compatibility with Java 8+ has not yet been tested.

---

## 🤝 Get Involved

If this project resonates with you, feel free to:

- Fork it
- File bug reports
- Suggest improvements
- Help test on modern Java versions (or a Mac)

This is a living resurrection — and you’re invited to shape its next arc.

