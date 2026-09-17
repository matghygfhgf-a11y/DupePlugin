# DupePlugin — Paper 1.21.5

Plugin do kopiowania przedmiotu trzymanego w rece. Domyslnie moze go uzywac **kazdy gracz**.

## Komendy

| Komenda | Opis |
|---|---|
| `/dupe` | kopiuje przedmiot z reki (aliasy: `/kopiuj`, `/duplikuj`) |
| `/dupe 16` | daje 16 sztuk tego przedmiotu (dziala tez 1, 32, 64...) |
| `/dupe stack` | daje pelny stack |
| `/dupe reload` | przeladowuje config (tylko OP) |

Ilosc wieksza niz `max-ilosc` zostaje przycieta do limitu. Przedmioty, ktore nie stackuja sie do 64 (np. miecze, perly endu), sa automatycznie dzielone na poprawne stacki, a to co nie miesci sie w ekwipunku wypada na ziemie.

## Permisje

- `dupe.use` — domyslnie **true** (kazdy). Sprawdzana tylko gdy w configu `wymagaj-permisji: true`.
- `dupe.bypass` — omija cooldown i limit ilosci (do 2304 sztuk naraz) (OP).
- `dupe.admin` — `/dupe reload` (OP).

## config.yml

```yml
cooldown-sekundy: 3
max-ilosc: 64
wymagaj-permisji: false
zabronione-przedmioty:
  - BEDROCK
  - COMMAND_BLOCK
  - BARRIER
  - STRUCTURE_BLOCK
```

Po zmianie pliku: `/dupe reload`.

## Jak zbudowac plik .jar

### Sposob A — GitHub (nic nie instalujesz)

1. Zaloz darmowe konto na github.com i stworz nowe repozytorium.
2. Wrzuc tam wszystkie pliki z tego folderu (przycisk "uploading an existing file" — mozna przeciagnac cala zawartosc).
3. Wejdz w zakladke **Actions** -> workflow "Build plugin" -> poczekaj na zielony haczyk.
4. Na dole strony buildu pobierz artefakt **DupePlugin** — w srodku jest `DupePlugin.jar`.

### Sposob B — na swoim komputerze

1. Zainstaluj **JDK 21** (np. Adoptium Temurin) i **Maven**.
2. W folderze projektu uruchom: `mvn package`
3. Gotowy plik: `target/DupePlugin.jar`

## Instalacja na Aternos

1. Panel Aternos -> **Software** -> upewnij sie, ze masz **Paper 1.21.5**.
2. **Pliki** -> folder `plugins` -> **Wgraj** -> wrzuc `DupePlugin.jar`.
3. Wystartuj serwer. Po pierwszym uruchomieniu pojawi sie `plugins/DupePlugin/config.yml`.

## Uwaga

Kopiowanie przedmiotow dla wszystkich graczy potrafi szybko zepsuc ekonomie i survival na serwerze. Jesli to serwer survivalowy, warto zostawic cooldown albo ustawic `wymagaj-permisji: true` i dawac permisje wybranym graczom.
