# Guía de Compilación y Configuración para Agente AI

## ⚙️ Entorno de Desarrollo y Java (JAVA_HOME)
La variable de entorno del sistema `JAVA_HOME` puede estar desconfigurada en este entorno local.

Por lo tanto, **SIEMPRE** se debe ejecutar Gradle utilizando directamente la ruta del JDK incluido en Android Studio:

### Ruta de JDK:
`C:\Program Files\Android\Android Studio\jbr`

### Comando de Compilación Estándar:
En PowerShell, ejecutar siempre:
```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat compileDebugKotlin
```

O para construir el APK debug:
```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleDebug
```

---

## 🎨 Recursos y UI
- **Splash Screen:** El componente `SplashScreen.kt` ([SplashScreen.kt](file:///c:/Users/aarro/AndroidStudioProjects/CEIReport/app/src/main/java/com/abdapps/ceireport/ui/screens/SplashScreen.kt)) utiliza el logo `logocei.png` ubicado en `app/src/main/res/drawable/logocei.png`.
