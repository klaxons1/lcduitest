# M3G Tester — тестер реализаций JSR-184 в эмуляторе

Этот проект — аналог `lcduitest`, но для проверки **M3G (JSR-184, `javax.microedition.m3g`)** реализаций в эмуляторах J2ME (MicroEmulator, KEmulator, FreeJ2ME, J2ME Loader и т.д.).

Сделан в том же стиле, что и LCDUI-тестер:

- Тот же харнесс: `TestCase`, `TestSuite`, `TestRunner`, `Assert`, `Results`, `Ui`, `Env`, `ExitHook`
- Тот же UI: главное меню с категориями, прогресс-бар, детальный просмотр теста, сохранение отчёта в `RecordStore`, вывод машиночитаемого лога в `stdout` с тегом `[M3GTEST]`
- Тот же CI-режим: `m3gtest.autorun=true` + `m3gtest.exit=true` + `m3gtest.store=true`
- Визуальные демо на Canvas с использованием `Graphics3D`

## Что тестируется

Пакет `javax.microedition.m3g` по https://nikita36078.github.io/J2ME_Docs/docs/jsr184/javax/microedition/m3g/package-summary.html :

**core:**
- `Object3D` — userID, userObject, duplicate (включая копирование потомков у Group), find, getReferences, animation tracks
- `Transform` — identity, set/get, invert, transpose, postMultiply, postTranslate/Rotate/Scale, transform векторов и VertexArray
- `Graphics3D` — singleton, getProperties, bindTarget (Graphics и Image2D), releaseTarget, setViewport, clear, render World, render Node с камерой и светом, camera/light management, depthRange, double bind / release без bind
- `Loader` — null checks, offset checks, загрузка битого массива

**data:**
- `VertexArray` — конструктор, set/get byte/short, проверки типов, границ, нулевая инициализация
- `VertexBuffer` — positions, normals, colors, texCoords, scale/bias (scaleBias[0]=scale), defaultColor, консистентность vertex count
- `TriangleStripArray / IndexBuffer` — конструкторы, getIndices, getStripLengths, implicit индексы, duplicate, instanceof
- `Image2D` — конструкторы из размера и из lcdui Image, форматы RGB/RGBA/ALPHA/LUMINANCE/LUMINANCE_ALPHA, isMutable, set(byte[]) и проверки границ/immutable
- `Texture2D` — image, filtering (NEAREST/LINEAR/BASE_LEVEL), wrapping (CLAMP/REPEAT), blending (MODULATE/REPLACE/BLEND/DECAL/ADD), blendColor, transformable методы (setOrientation, setScale, setTranslation, getCompositeTransform)

**appearance:**
- `Appearance` — material, compositingMode, polygonMode, fog, texture по индексам, layer
- `Material` — цвета AMBIENT/DIFFUSE/SPECULAR/EMISSIVE, shininess, vertexColorTrackingEnable
- `PolygonMode` — culling (BACK/FRONT/NONE), winding (CW/CCW), shading (FLAT/SMOOTH), perspective, two-sided, local camera lighting
- `CompositingMode` — blending (REPLACE/ALPHA/ALPHA_ADD/MODULATE/MODULATE_X2), alphaThreshold, depth test/write, color/alpha write, depthOffset
- `Fog` — mode LINEAR/EXPONENTIAL, color, density, near/far (setLinear)
- `Background` — color, image, imageModeX/Y (BORDER/REPEAT), crop, clear flags

**scene graph:**
- `Node` — enable, parent, transform, scope, alphaFactor, alignment (Z_AXIS/Y_AXIS, NONE/ORIGIN), getTransformTo
- `Group` — addChild/getChild/childCount/removeChild, pick (оба варианта), проверки на циклы и World как child
- `World` — activeCamera, background, наследование от Group
- `Camera` — perspective/parallel/generic, getProjection, проверки аргументов
- `Light` — mode AMBIENT/DIRECTIONAL/OMNI/SPOT, color, intensity, spotAngle/Exponent, attenuation
- `Mesh` — конструкторы с одним и несколькими сабмешами, getVertexBuffer/IndexBuffer/Appearance/SubmeshCount, setAppearance
- `MorphingMesh` — base + targets, weights, getMorphTarget
- `SkinnedMesh` — skeleton, getSkeleton, getBoneTransform, getBoneVertices, addTransform
- `Sprite3D` — scaled flag, image, appearance, crop

**animation:**
- `KeyframeSequence` — конструктор, setKeyframe/getKeyframe, interpolation (LINEAR/SPLINE/SLERP/STEP/CONSTANT), repeatMode LOOP/CLAMP, validRange, duration
- `AnimationController` — speed, weight, activeInterval, position, refWorldTime
- `AnimationTrack` — конструкторы с и без контроллера, setController/setKeyframeSequence, property константы (ALPHA/COLOR/CROP/DENSITY/DIFFUSE_COLOR/EMISSIVE_COLOR/FAR_DISTANCE/FIELD_OF_VIEW/INTENSITY/MORPH_WEIGHTS/NEAR_DISTANCE/ORIENTATION/PICKABILITY/SCALE/SHININESS/SPECULAR_COLOR/SPOT_ANGLE/SPOT_EXPONENT/TRANSLATION/VISIBILITY)

**utils:**
- `RayIntersection` — поля после pick, getIntersected/distance/normal/textureS/T/submeshIndex
- `Constants` — все значения констант, определённые спецификацией (Image2D форматы, Texture2D фильтры/функции/wrap, Material таргеты, PolygonMode, CompositingMode, Fog, Background, Camera, Light, KeyframeSequence, AnimationTrack, Node оси)
- `Properties` — `microedition.m3g.version`, `Graphics3D.getProperties()` (maxTextureDimension, maxViewportWidth/Height, numTextureUnits, maxLights и т.д.), флаг поддержки M3G

**self test:**
- Framework — проверка работы assert'ов и доступности M3G

## Визуальные демо

В `m3gtest.demos` 7 демо на `Canvas` с `Graphics3D`:

1. **Rotating cube - immediate mode** — куб с вращением, камера perspective, directional light
2. **World + Camera + Light - retained mode** — рендер `World`
3. **Texture + Material + Fog** — текстурированный куб с шахматной текстурой, созданной из lcdui Image
4. **Sprite3D** — спрайт, движущийся по синусоиде
5. **MorphingMesh** — морфинг треугольника
6. **Transparency & CompositingMode** — два полупрозрачных куба с ALPHA blending
7. **Picking - RayIntersection** — демонстрация `Group.pick`

Каждое демо крутится в отдельном потоке (анимация через `frame++` и `repaint()`), как и в оригинальном `lcduitest`.

## Запуск

### В эмуляторе

- Собери jar как обычно (IntelliJ J2ME SDK, WTK, или ant). В `META-INF/MANIFEST.MF` уже есть:
  ```
  MIDlet-1: LCDUITester,,lcduitester
  MIDlet-2: M3GTester,,m3gtester
  ```
- Запусти `M3GTester` MIDlet. В меню:
  - Run ALL — все тесты
  - Категории — core, data, appearance, scene, animation, utils, self test
  - Visual demos — визуальная проверка рендера
  - Environment — системные свойства, включая `microedition.m3g.version` и таблицу `Graphics3D.getProperties()`
  - Last stored run — последний сохранённый отчёт из RMS
  - Help / about

### CI / автоматический режим

Добавь в JAD или как system property:

```
m3gtest.autorun=true
m3gtest.exit=true
m3gtest.store=true
```

(поддерживаются также старые ключи `lcduitest.*` для совместимости)

Тогда при старте MIDlet автоматически прогонит все тесты, выведет в stdout строки вида:

```
[M3GTEST] BEGIN version=1 total=0 platform=...
[M3GTEST] TEST suite=Object3D name=user_id_default_and_set status=PASS severity=MUST ms=2 message=...
[M3GTEST] SUITE id=Object3D total=8 passed=8 failed=0 errors=0 info=0 ms=15
[M3GTEST] END tests=200 passed=195 failed=3 errors=2 info=5 ms=1234
```

Этот лог парсится внешним харнессом. Краткий отчёт сохраняется в RecordStore `m3gtest-results`.

## Структура проекта

```
src/
  m3gtester.java              — MIDlet entry point (как lcduitester.java)
  m3gtest/
    Assert.java, TestCase.java, TestSuite.java, TestRunner.java, TestResult.java
    Results.java, Env.java, Ui.java, ExitHook.java, Suites.java
    tests/                    — 31 suite, покрывающих все классы M3G
    demos/                    — Demo.java, Demos.java, CanvasDemos.java (7 демо)
    ui/App.java               — меню, прогресс, результаты (копия lcduitest.ui.App)
  lcduitest/                  — оригинальный LCDUI тестер (сохранён)
META-INF/MANIFEST.MF          — два MIDlet'а, MicroEdition-Requirements: M3G 1.1
```

## Чем отличается от LCDUI тестера

- Тот же харнесс и UI, но все тесты переписаны под M3G API
- Добавлены проверки специфичные для 3D: bind/release target, viewport, depthRange, трансформации, pick, дублирование графа сцены, анимация
- В `Ui` добавлены M3G-хелперы `isM3GAvailable()` и `testGraphics3DBind()`
- В `Env` добавлен парсинг `microedition.m3g.version` и вывод `Graphics3D.getProperties()`
- Демо используют `Graphics3D` вместо `Graphics` примитивов
- Константы проверяют значения из JSR-184 (например `Image2D.RGB=99`, `Texture2D.FILTER_NEAREST=210` и т.д.)

## Для чего полезно

- Сравнение эмуляторов: MicroEmulator vs KEmulator vs FreeJ2ME vs J2ME Loader — у каждого своя реализация M3G, часто с багами в `Transform.invert()`, `VertexBuffer.setPositions`, `Image2D.isMutable`, `Background.setImageMode`, `Group.removeChild` (должен игнорировать null, а не кидать NPE) и т.д.
- Регрессионные тесты при доработке эмулятора
- Документация того, что ожидает реальное J2ME приложение от M3G

## Дальнейшее развитие

- Добавить тесты на загрузку реального `.m3g` файла (сгенерировать минимальный файл в `res/`)
- Добавить тесты на `Loader.load(String)` с ресурсами из jar
- Добавить бенчмарки (fps для вращающегося куба)
- Добавить тесты на многопоточность (bind из разных потоков должен кидать IllegalStateException)

---

Автор M3G тестера: сделан по образцу LCDUITester, покрывает весь `javax.microedition.m3g` пакет.
