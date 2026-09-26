<div align="center">

# 📒 Libro Mayor
### Control de Compras &amp; Ventas — App Android

*Tu Libro Mayor, empaquetado como app nativa de Android y compilado automáticamente en la nube.*

![Plataforma](https://img.shields.io/badge/plataforma-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Compilado con](https://img.shields.io/badge/compilado%20con-Capacitor-119EFF?style=for-the-badge&logo=capacitor&logoColor=white)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Licencia](https://img.shields.io/badge/uso-privado-6b7280?style=for-the-badge)

</div>

<br>

> [!IMPORTANT]
> **No subas este proyecto usando el editor en el navegador (`vscode.dev` / `github.dev`, el botón "." de GitHub).**
> Ese editor corre 100% en el navegador y **no puede confirmar (commit) archivos binarios** como los
> íconos `.png` o `gradle-wrapper.jar`, ni cambios grandes de una sola vez. Por eso viste el error
> **"No se puede confirmar · Origen: Repositorios remotos"**. No es un error tuyo — es una limitación
> conocida de esa herramienta. Usa cualquiera de los dos métodos de la sección 1 y funcionará sin problema.

<br>

## 📑 Contenido

1. [Subir el proyecto a GitHub](#1--subir-el-proyecto-a-github-forma-confiable)
2. [Descargar el APK ya compilado](#2--descargar-el-apk-ya-compilado)
3. [Qué trae la app](#3--qué-trae-la-app)
4. [Personalizar nombre, ícono y color](#4--personalizar-nombre-ícono-y-color)
5. [Solución de problemas](#5--solución-de-problemas)
6. [Firmar para Google Play (opcional)](#6--firmar-para-google-play-opcional)

<br>

## 1 · Subir el proyecto a GitHub (forma confiable)

Elige **una** de estas dos vías. Ambas evitan por completo el editor del navegador.

### 🖥️ Opción A — GitHub Desktop (la más fácil, recomendada)

| Paso | Qué hacer |
|---|---|
| 1 | Instala **[GitHub Desktop](https://desktop.github.com/)** (gratis) e inicia sesión con tu cuenta de GitHub. |
| 2 | En GitHub Desktop: **File → Add local repository** → selecciona esta carpeta (`libro-mayor-app`). |
| 3 | Si te pregunta si quieres crear un repositorio aquí, acepta ("create a repository"). |
| 4 | Verás en el panel izquierdo la lista de archivos nuevos (los mismos ~70 que viste en `vscode.dev`, incluyendo los íconos). Escribe un mensaje como `App lista para compilar` y pulsa **Commit to main**. |
| 5 | Pulsa **Publish repository** (arriba). Puedes marcarlo como privado si prefieres. Listo — ya está en GitHub. |

GitHub Desktop maneja binarios y proyectos grandes sin problema; es la herramienta oficial de GitHub para esto.

### 💻 Opción B — Terminal / Git (si ya tienes Git instalado)

Abre una terminal **en tu computadora** (no en el navegador), dentro de esta carpeta:

```bash
git init
git add .
git commit -m "App Libro Mayor lista para compilar"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
git push -u origin main
```

> Reemplaza `TU_USUARIO/TU_REPO` por tu repositorio real (por ejemplo,
> `activo5408-blip/VENTASS`). Si el repo ya tiene un `README.md` remoto y el
> push es rechazado, usa `git push -u origin main --force` la primera vez.

<br>

## 2 · Descargar el APK ya compilado

1. Apenas el `push` llegue a GitHub, la pestaña **Actions** del repositorio arranca sola
   el workflow **"Build Android APK"** (o lánzalo a mano: Actions → el workflow → **Run workflow**).
2. Espera el ✅ verde (3–6 minutos).
3. Abre esa ejecución → baja hasta **Artifacts** → descarga **`libro-mayor-debug-apk`**
   (es un `.zip` que contiene `app-debug.apk`).
4. Pasa el `.apk` a tu celular (USB, correo, Drive...) y ábrelo. Android pedirá permitir
   "instalar apps de orígenes desconocidos" la primera vez — es normal, acéptalo.

<br>

## 3 · Qué trae la app

- ✅ Todo tu Libro Mayor (Panel, Compras, Lotes, Configuración, Respaldo), igual que en el
  navegador, pero como app instalada con su propio ícono.
- ✅ Los datos se guardan en el propio dispositivo, dentro de la app.
- ✅ **"Guardar respaldo (.json)"** y **"Guardar inventario (.csv)"** abren el selector nativo
  de Android para elegir dónde guardar (una carpeta, Google Drive, WhatsApp, etc.).
- ✅ **Doble toque** sobre una fila o tarjeta editable abre la edición directamente
  (además del botón "Editar" de siempre).
- ✅ Pantalla adaptada a cualquier celular: menú inferior, formularios en una columna,
  modales a pantalla completa.

<br>

## 4 · Personalizar nombre, ícono y color

| Qué cambiar | Dónde |
|---|---|
| Nombre visible de la app | `appName` en `capacitor.config.ts` |
| Id del paquete (`com.negocio.libromayor`) | `appId` en `capacitor.config.ts` — cámbialo **antes** del primer build |
| Ícono / splash | Reemplaza los `.png` dentro de `android/app/src/main/res/mipmap-*` y `drawable-*`, o genera unos nuevos con `npx @capacitor/assets generate` a partir de tu propio logo |
| Color de la barra de estado | `android/app/src/main/res/values/styles.xml` |

Después de cualquier cambio en `www/index.html` o en el ícono, vuelve a subir con
GitHub Desktop (o `git add . && git commit -m "..." && git push`) y el workflow genera
un APK nuevo automáticamente.

<br>

## 5 · Solución de problemas

<details>
<summary><b>"No se puede confirmar · Origen: Repositorios remotos" en vscode.dev</b></summary>
<br>

Es la limitación explicada arriba: el editor del navegador no soporta bien archivos
binarios ni commits grandes. Usa **GitHub Desktop** (Opción A de la sección 1) — soluciona
el problema por completo.
</details>

<details>
<summary><b>El workflow de Actions falla (❌ rojo)</b></summary>
<br>

Abre el log del paso que falló. Las causas más comunes:
- Olvidaste subir la carpeta `android/` completa (revisa que esté en el repo).
- El archivo `www/index.html` tiene un error de sintaxis JavaScript.
- Problema temporal de GitHub Actions — vuelve a correr el workflow ("Re-run jobs").
</details>

<details>
<summary><b>Android dice "app no instalada" o "paquete dañado"</b></summary>
<br>

Verifica que descargaste y descomprimiste el `.zip` de "Artifacts" para obtener el
`.apk` real (no intentes abrir el `.zip` directamente). Si sigue fallando, borra la
versión anterior de la app antes de instalar la nueva.
</details>

<br>

## 6 · Firmar para Google Play (opcional)

El workflow actual genera un APK de **depuración** (`assembleDebug`) — se instala sin
problema en cualquier celular, pero no sirve para publicar en Play Store. Para un
release firmado, más adelante:

1. Genera una keystore (`keytool -genkey ...`).
2. Guarda la keystore y su contraseña como **Secrets** del repositorio
   (Settings → Secrets and variables → Actions).
3. Cambia el paso final del workflow a `./gradlew bundleRelease` con la firma
   configurada en `android/app/build.gradle`.

Avísame cuando quieras dar ese paso y te preparo esa parte.

<br>

<div align="center">

*Hecho con Capacitor + GitHub Actions · Tus datos siempre se quedan en tu dispositivo.*

</div>
