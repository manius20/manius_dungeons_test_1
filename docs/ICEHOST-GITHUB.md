# GitHub + IceHost — manius_dungeons (ETAP 1)

## GitHub

1. Pobierz `manius_dungeons-etap1-projekt.zip`, rozpakuj na swoim komputerze.
2. GitHub > **New repository** > `manius_dungeons`. Nie musisz usuwać starego repozytorium `manius_gildie`.
3. Przejdź do repozytorium > **Add file** > **Upload files**. Przeciągnij ZAWARTOŚĆ rozpakowanego folderu (nie sam ZIP); `pom.xml`, `src`, `.github`, `docs` powinny znaleźć się w katalogu głównym. Jeśli przeglądarka nie przyjmie ukrytego folderu `.github`, dodaj go oddzielnie (ścieżka `.github/workflows/build.yml`).
4. **Commit changes**.
5. Zakładka **Actions** > `manius_dungeons - buduj JAR` > **Run workflow**.
6. Jeśli przebieg jest zielony, wejdź w niego i pobierz Artifact `manius_dungeons-JAR`; w ZIP będzie `.jar`.
7. Jeśli czerwony, otwórz nieudany krok i prześlij log — kompilacja Paper 26.2 nie była lokalnie testowana.

## IceHost — TYLKO opcjonalny test na kopii serwera

**Nie wgrywaj jeszcze na produkcyjny świat SMP jako gotowego pluginu dungeonów.** Etap 1 nie generuje struktur. Można go przetestować na osobnym serwerze Paper 26.2 z Java 25:

1. Utwórz kopię testową serwera, zatrzymaj ją.
2. Skopiuj zbudowany JAR do `plugins/`. Nie usuwaj folderu `S16Countries` ani żadnego pliku `manius_gildie`.
3. Uruchom testowy serwer. W `plugins/manius_dungeons/config.yml` zmień `settings.world`, jeżeli nazwa świata jest inna niż `S16_wojny_1`.
4. `/dungeon admin status` pokaże etap 1. Sprawdź `/dungeon admin`, `/dungeon admin pomoc`, `/dungeon admin zaplanuj`, `/dungeon admin podglad`, `/dungeon admin loot slaby`.
5. Zrestartuj testowy serwer i sprawdź, czy `schedules.yml` oraz `loot.yml` zachowują dane.

## UWAGA

Plugin w tej wersji **nie zawiera kompletnego generatora, regionów, resetu terenu ani integracji BlueMap**. Nie zapewnia więc jeszcze dungeonów do gry. Przed wprowadzeniem tych funkcji potrzebne będą testy obciążenia i niezależny backup całego świata.
