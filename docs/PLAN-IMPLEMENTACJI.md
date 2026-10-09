# Zweryfikowany zakres i kolejność etapów manius_dungeons

## Etap 1 — obecna paczka

Samodzielny projekt Paper 26.2, GUI administratora, terminy jednorazowe i cotygodniowe, YAML, edycja i ważenie lootpooli, matematyczny planer podziemnych układów, TAB-completion, GitHub Actions. Nie generuje świata ani fałszywie nie komunikuje aktywnego dungeona.

## Etap 2 — bezpieczne odczyty świata i zatwierdzanie terenu

1. Losowanie miejsca przy wejściu w promieniu ≤3500 od (0,0), poza wodą, na płaskim lądzie, w naturalnych biomach. Planowany ślad całej konstrukcji w ±4000 (z bezpieczeństwem na margines 50).
2. Przegląd powierzchni i głębokości, kontrola przecięcia jaskiń, rur wodnych, budowli, WorldGuard oraz claimów `manius_gildie`. Sam biome check nie wystarczy.
3. Zanim ruszy jakikolwiek blok: weryfikowalna trwała kopia fragmentu świata, integralność, zapis przed każdą mutacją i możliwość wznowienia/rollbacku po crashu. Należy zachować również block entities, inventory, tile NBT, biomy i płyny. Przywracanie nie może kasować zmian innych pluginów poza obszarem zdarzenia.
4. Opt-in na generację, limit jednoczesnych zmian oraz blokada przed resetem obszarów, na których aktualnie znajdują się gracze.
5. Testy z 15, 50, 100 pokojami i wieloma dungeonami jednocześnie na osobnej kopii świata.

## Etap 3 — rzeczywiste dungeony i cykl wydarzenia

- Pokoje/korytarze pod ziemią, 1–10 wejść na powierzchni; cztery motywy mechowy/siarkowy/pustynny/mesa.
- Generator proceduralny i możliwość szablonów WorldEdit, zgodność z istniejącymi jaskiniami. Żadnych wejść w wodzie; dopuszczalny przebieg pod jeziorem.
- Dopuszczone kopanie i stawianie przy otwarciu, ale zachowana normalna trasa do ważnych celów; ochrona 50 bloków od bryły po zamknięciu.
- Okres przygotowania 3h przed startem; późne zlecenie próbuje generować natychmiast, a jeśli nie zdąży — anulowanie, bez częściowo otwartego dungeona.
- 30 min przed startem: współrzędne wejść + BlueMap. Warn przed zamknięciem 30/15/10/5/1 minut; teleportacja wszystkich na zewnątrz i rollback.
- Niezaplanowane wydarzenia nie startują. Zatwierdzone cotygodniowe wydarzenia działają automatycznie.

## Etap 4 — walka, skarby, dropy

- Globalne skrzynie: wspólna zawartość, bez duplikowania lootów, reset przy nowej instancji dungeona.
- Co najmniej 60% słabych nagród, różne prawdopodobieństwa i zawartości. Skrzynie w większości pokoi, czasami dwie.
- Sloty i szanse edytowalne w GUI i YAML; aktywność lootpoolu przez N rzeczywistych dni, ostrzeżenia adminów o końcu.
- Moby boost 1.2x, losowa zbroja i enchanty; boss 5%, miniboss 25% na CAŁY dungeon.
- Boss 3 losowania dropów; Mace szansa 0.01% NA JEDNO losowanie (w razie 3 losowań około 0.03% na bossa, jeśli dopuszczona w każdym); drop globalny podnoszony przez pierwszego gracza.
- Pułapki: przede wszystkim nieśmiertelne, rzadko zabójcze; komunikaty `S16 SMP 2 »`.

## Niepomijalne warunki bezpieczeństwa

Generowanie struktur w normalnym świecie survival z możliwością kopania i codziennym wycofywaniem modyfikacji jest operacją ryzykowną; **nie można zapewnić bezstratnego rollbacku bez implementacji testów i kopii zapasowej realnego świata**. Snapshot wszystkich zmienianych bloków i NBT musi zostać zapisany NA DYSK przed pierwszą zmianą. Konieczne są limity dla ticków, zapisu, pamięci i liczby chunków.
