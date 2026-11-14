#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "=== Сборка универсального template.apk v2 ==="
mkdir -p build/gen build/obj build/apk/assets/www res/drawable

# Создаем валидный PNG 192x192 через python если установлен, иначе используем встроенный base64
python3 -c "
import zlib, struct
w, h = 192, 192
raw = b''.join(b'\x00' + b'\x25\x63\xeb\xff' * w for _ in range(h))
def chunk(t, d):
    return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')
open('res/drawable/icon.png', 'wb').write(png)
" 2>/dev/null || echo "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAABmJLR0QA/wD/AP+gvaeTAAAAWklEQVR4nO3BMQEAAADCoPVPbQ0PoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIB3A8YAAAFx709hAAAAAElFTkSuQmCC" | base64 -d > res/drawable/icon.png

ANDROID_JAR=$(find $PREFIX/share -name "android.jar" 2>/dev/null | head -n 1)
if [ -z "$ANDROID_JAR" ]; then
    echo "Скачиваем компактный android.jar..."
    curl -sL "https://github.com/Sable/android-platforms/raw/master/android-33/android.jar" -o build/android.jar
    ANDROID_JAR="build/android.jar"
fi

echo "1. Компиляция ресурсов и манифеста (aapt)..."
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR" -F build/resources.ap_

echo "2. Компиляция Java кода (javac)..."
javac -source 1.8 -target 1.8 -bootclasspath "$ANDROID_JAR" -d build/obj build/gen/com/html/app/R.java src/com/html/app/MainActivity.java

echo "3. Конвертация в DEX (d8)..."
d8 --lib "$ANDROID_JAR" --output build/apk build/obj/com/html/app/*.class

echo "4. Упаковка в APK..."
cp build/resources.ap_ build/unsigned.apk
cp assets/www/index.html build/apk/assets/www/index.html
cd build/apk
zip -r ../unsigned.apk classes.dex assets/
cd ../..

echo "5. Подпись APK..."
if [ ! -f debug.keystore ]; then
    keytool -genkeypair -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi
apksigner sign --ks debug.keystore --ks-pass pass:android --out template.apk build/unsigned.apk

cp template.apk ~/storage/downloads/template.apk
echo "=== ГОТОВО! Новый template.apk сохранен в папку Загрузки (Download) ==="
