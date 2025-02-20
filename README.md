# 📋 Clipboard Manager

<div style="display:inline-block;">
  <img src="screenshots/1.png" width="250" height="300" alt="Screenshot1" />
  <img src="screenshots/2.png" width="250" height="300" alt="Screenshot2" />
</div>

📝 A desktop application written in `Java 21` for managing clipboard content. The application listens to the clipboard, stores clipboard items in an SQLite database, and allows you to manage and organize clipboard entries based on content type `STRING`, `FILE`, `IMAGE`.

## 🚀 Features

- **Clipboard Monitoring:** Continuously monitors and stores clipboard content (STRING, FILE, IMAGE) in an SQLite database.
- **Configurable Storage Limit:** You can configure how many clipboard entries to store in the database. Older entries are automatically deleted when the limit is exceeded.
- **Graphical Interface:** Press the configured shortcut (e.g., **Ctrl + Alt + X** or **F5**) to open a graphical interface to view, delete, or re-add clipboard entries. By default, the shortcut is set to **F7**, but it can be customized in the **ui\_shortcut** field of the configuration.
- **Content Management:** Supports **STRING**, **FILE**, and **IMAGE** clipboard types.
- **Single/Multiple Item Deletion:** You can delete individual clipboard entries or remove all items at once.

> 💡**Note**:
> The configuration file and SQLite database will be automatically created on the first run.

## ⚙️ Configuration

You can set the number of clipboard entries to store in the config file. The program will delete older clipboard entries once the limit is exceeded. An example config file in JSON format:

```json
{
  "clipboard_types": [
    "STRING",
    "FILE",
    "IMAGE"
  ],
  "shortcuts": {
    "restart": "Ctrl+Alt+F11",
    "close_app": "Ctrl+Alt+O",
    "open_ui": "F7",
    "clear_all_clipboard": "Ctrl+Alt+F8",
    "delete_last_data": "Alt+F8",
    "clear_system_clipboard": "Ctrl+Alt+F9"
  },
  "theme": "MAC_DARK",
  "clipboard_save_count": 50,
  "clipboard_handler_period": {
    "clipboard_handler_mills": 500,
    "clipboard_image_handler_sec": 10
  }
}
```

### 📋 Clipboard Types

The clipboard manager supports the following clipboard types:

- `STRING`
- `FILE`
- `IMAGE`

### ⌨️ Shortcuts

All the shortcuts are customizable, and you can change them as per your preference in the settings. Here's a list of the available shortcuts:

| Function               | Default Shortcut   |
|------------------------|--------------------|
| Open UI                | `F7`               |
| Restart Application    | `Ctrl + Alt + F11` |
| Close Application      | `Ctrl + Alt + O`   |
| Clear All Clipboard    | `Ctrl + Alt + F8`  |
| Delete Last Data       | `Alt + F8`         |
| Clear System Clipboard | `Ctrl + Alt + F9`  |

### 🎨 Themes

The clipboard manager supports multiple themes for the UI:

| Theme Name  | Description            |
|-------------|------------------------|
| `INTELLIJ`  | IntelliJ Look and Feel |
| `DARK`      | Dark Theme             |
| `LIGHT`     | Light Theme            |
| `MAC_DARK`  | macOS Dark Theme       |
| `MAC_LIGHT` | macOS Light Theme      |
| `DARCULA`   | Darcula Theme          |

You can configure the theme in the settings under the `theme` field. The default theme is `MAC_DARK`.

### 💾 Storage Settings

The clipboard manager allows you to configure how frequently clipboard data is saved:

```json
{
  "clipboard_save_count": 50,
  "clipboard_handler_period": {
    "clipboard_handler_mills": 500,
    "clipboard_image_handler_sec": 10
  }
}
```

- `clipboard_save_count`: The maximum number of clipboard entries stored.
- `clipboard_handler_mills`: The frequency (in milliseconds) at which clipboard text and file content is checked.
- `clipboard_image_handler_sec`: The frequency (in seconds) at which clipboard image content is checked.

## ⚡ Requirements

- Java 21 or higher

## 🔧 Installation

### Build the Application

1. Clone the repository:

```bash
   git clone https://github.com/bardiademon/ClipboardManager
```

```bash
   cd ClipboardManager
```

#### Compile and package the application using Maven:

```bash
  mvn clean install package
```

#### Run the packaged JAR file:

```bash
  java -jar target/JARNAME.jar
```

#### Run Background on Windows

```shell
  javaw -jar target/JARNAME.jar
```

<hr/>

## 🛠️ Usage

Run the application using the command above.
The program will listen to the clipboard and store entries in the SQLite database.
Press the configured shortcut (e.g., **Ctrl + Alt + X** or **F5**) to open the graphical interface where you can:

View the number of stored clipboard entries.
Delete entries (either one by one or all at once).
Re-add older clipboard entries to your current clipboard.
By default, the shortcut is set to **F7**, but it can be customized in the **ui_shortcut** field of the configuration.

## 💻 Technologies

![Java](screenshots/java.png)

## 📢 Spread the word!

If you want to say thank you:

- Add a GitHub Star to the project!
- Follow my GitHub [bardiademon](https://github.com/bardiademon)

## ⚠️ License & 📝 Credits

by bardiademon [https://bardiademon.com](https://www.bardiademon.com)
