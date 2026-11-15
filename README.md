# HTML to APK Сборщик / HTML to APK Builder

Веб-инструмент для упаковки HTML, CSS и JS файлов в подписанное Android-приложение (APK) прямо в браузере.

### Основные файлы репозитория
- **index.html** — основной инструмент сборки и подписи.
- **template.apk** — базовый APK-шаблон.
- **template-source/** и **pro-template-source-v2.zip** — исходные коды шаблона и их архив для настройки через Termux.
- **llms.txt** — технические заметки для ИИ.

### Возможности и использование
- **Функции:** Локальная сборка в памяти браузера, поддержка RSA-2048, кастомизация иконки и имени, нативные диалоги взамен стандартных.
- **Как запустить:** Скачайте архив репозитория, откройте `index.html` в браузере, выберите `template.apk` вместе со своими веб-файлами, задайте параметры и нажмите **«Собрать и подписать APK»**.

### JavaScript-мост `window.AndroidBridge`
Внутри приложения доступен объект `window.AndroidBridge` с методами управления: `getInstalledBrowsers()`, `setPreferredBrowser(pkg)`, `openUrlInBrowser(url, pkg)`, `getCurrentWebViewInfo()`, `openSystemWebViewPicker()` и `setUserAgent(ua)`. Полный перечень описан в оригинальных файлах репозитория.

---

A browser-based tool for packing HTML, CSS, and JS files into a signed Android application (APK) directly in the browser.

### Repository Contents
- **index.html** — main builder and signer tool.
- **template.apk** — base template APK.
- **template-source/** & **pro-template-source-v2.zip** — template source code and backup for advanced customization via Termux.
- **llms.txt** — technical guidelines for AI assistants.

### Features & How to Use
- **Features:** In-browser bundling and RSA-2048 signing, custom icons/labels, and native Android dialogs.
- **Usage:** Download the archive, open `index.html`, select `template.apk` and your web files, configure settings, and click **Build & Sign APK**.

### Built-in `window.AndroidBridge` API
The app exposes `window.AndroidBridge` to JavaScript, providing methods such as `getInstalledBrowsers()`, `setPreferredBrowser(pkg)`, `openUrlInBrowser(url, pkg)`, `getCurrentWebViewInfo()`, `openSystemWebViewPicker()`, and `setUserAgent(ua)`. Refer to the repository files for detailed specs.

## License
Copyright (C) 2026 Denis Artuck (internet-off). Distributed under the **GNU Affero General Public License v3.0** (AGPL-3.0). See the LICENSE file for details.
