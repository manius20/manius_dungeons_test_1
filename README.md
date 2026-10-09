# manius_dungeons — S16 SMP 2

**Etap 1 / wersja źródłowa 0.1.0-SNAPSHOT. Nie jest to jeszcze gotowy plugin dungeonów do produkcji!**

Osobny plugin Paper **Minecraft 26.2 / Java 25**. Został napisany tak, aby na obecnym etapie **nie zmieniał żadnych bloków świata**, nie ruszał folderu `S16Countries` i nie uruchamiał niegotowego generatora. GitHub Actions ma kompilować JAR, ale w tej sesji pełnej kompilacji API Paper nie udało się zweryfikować.

## Co jest zaimplementowane

- Główna komenda `/dungeon admin`, panel Minecraft Inventory GUI, pomoc i podpowiedzi TAB; uprawnienie `maniusdungeons.admin` (domyślnie OP).
- Jednorazowe i cotygodniowe terminy: konkretny motyw, data/dni tygodnia, godzina, długość, liczba pokoi.
- Nadpisanie liczby pokoi (15–100) na konkretną datę, domyślnie losowo 15–50.
- Cztery motywy wpisów: `mechowy`, `siarkowy`, `pustynny`, `mesa`.
- Trwały osobny `schedules.yml` (zapis przez plik tymczasowy), załadowanie po restarcie.
- Cztery edytowalne GUI przedmiotów: `slaby`, `dobry`, `boss`, `miniboss` — do 45 pozycji każde. Przedmioty ItemStack są zapisywane z enchantami i ilością do `loot.yml`.
- Komenda zapisująca procent przypisany do slotu danego lootpoolu.
- Osobny silnik **planowania geometrii** 15–100 pokoi: losowe warianty korytarzy zapewniające połączenie pokoi, 1–10 planowanych wejść, szanse na bossa/minibossa 5/25%; podgląd tekstowy bez jakichkolwiek zmian w świecie.
- Test algorytmu przejść dla wszystkich liczebności pokoi od 15 do 100.
- Publiczne przypomnienia o braku wydarzenia (ustawienia godzin w config.yml), nie wysyła mylącego komunikatu o otwarciu.
- Konfiguracja docelowych limitów: promień 3500 od 0,0, kwadrat ±4000, margines ochrony 50, 3 godziny przygotowania, ostrzeżenia, czas ujawnienia i stawki bossów.

## Czego ten etap JESZCZE NIE ROBI

**WAŻNE**: Samo wpisanie wydarzenia / podgląd to **NIE** jest wygenerowanie dungeona. Nie ma jeszcze:

- faktycznego tworzenia pokoi/korytarzy w świecie, wejść, magicznych barier, WorldEdit ani czterech zestawów dekoracji;
- gwarantowanego wykrywania budowli graczy, claimów `manius_gildie` oraz regionów WorldGuard;
- zapisu i przywracania całego terenu, ochrony 50 bloków, blokady wyjścia, teleportacji;
- rzeczywistych skrzyń, losowania dropów z procentów, resetów lootpooli, pułapek;
- mobów i bossów, dropu Mace, PvP, BlueMap, aktywnych harmonogramów i komunikatów zamknięcia.

Wszystkie te mechaniki są **następnymi etapami**, opisanymi w `docs/PLAN-IMPLEMENTACJI.md`. **Nie wdrażaj obecnego JAR-a jako ukończonego pluginu dungeonowego.**

## Komendy

```text
/dungeon admin
/dungeon admin pomoc
/dungeon admin status
/dungeon admin lista
/dungeon admin kalendarz
/dungeon admin zaplanuj <id> <motyw> <RRRR-MM-DD|dni:MONDAY,FRIDAY> <HH:mm> <minuty> [losowo|15-100]
/dungeon admin pokoje <id> <RRRR-MM-DD> <15-100>
/dungeon admin podglad <id> <RRRR-MM-DD>
/dungeon admin usun <id>
/dungeon admin loot <slaby|dobry|boss|miniboss>
/dungeon admin szansa <pool> <slot 1-45> <0-100>
/dungeon admin reload
```

Przykłady:

```text
/dungeon admin zaplanuj piatek_mesa mesa dni:FRIDAY 17:00 120 losowo
/dungeon admin zaplanuj sobota_mech mechowy 2026-10-17 18:00 60 80
/dungeon admin pokoje piatek_mesa 2026-10-16 100
/dungeon admin podglad piatek_mesa 2026-10-16
/dungeon admin loot slaby
/dungeon admin szansa slaby 1 10
/dungeon admin lista
```

Po edytowaniu przedmiotów w GUI zamknij ekwipunek — następuje zapis YAML. Szansa oznacza wartość zapisaną do wykorzystania przez przyszły silnik losowania; **na tym etapie jeszcze nie przyznaje nagród**. Skróty `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY` to nazwy dni tygodnia po angielsku (w wymaganiach graczy pozostał polski język wiadomości).

## Budowanie

Wymaga JDK 25 i Maven. GitHub Actions uruchamia `mvn clean verify` i publikuje JAR jako Artifact, z JDK 25. Obecnie lokalnie potwierdzono poprawność testu `DungeonLayoutPlanner` na Java 21; pełna kompilacja z Paper API 26.2 wymaga workflow.

## Dane i bezpieczeństwo

Plugin zapisuje wyłącznie do `plugins/manius_dungeons/` (`config.yml`, `schedules.yml`, `loot.yml`). Nie czyta ani nie modyfikuje danych `manius_gildie` (`plugins/S16Countries/`). **Ważne:** instalacja przyszłego generatora na świecie SMP wymaga kopii świata i testów odtwarzania po crashu.

## Instalacja

Zobacz `docs/ICEHOST-GITHUB.md`. Nie przechodź jeszcze do etapu generowania świata — najpierw pełne testy, integracja z WorldGuard i weryfikacja mapy świata.
