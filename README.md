# Backend Kasa Sklepowa - Dokumentacja API

## Test połączenia
- Endpoint: `GET /api/test`
- Opis: Sprawdza połączenie z backendem.
- Przykład odpowiedzi:
```
Połączenie z backendem działa poprawnie! 2026-03-02T12:00:00
```

## Produkty

### Pobieranie wszystkich produktów
- Endpoint: `GET /api/products`
- Opis: Zwraca listę wszystkich produktów w bazie.
- Odpowiedź: JSON tablica produktów

### Pobieranie produktu po kodzie kreskowym
- Endpoint: `GET /api/products/scan/{barcode}`
- Parametry:
    - `barcode` - kod EAN produktu
- Przykład: `GET /api/products/scan/5449000130389`
- Odpowiedź: JSON produktu lub 404 jeśli nie istnieje

### Dodawanie produktu
- Endpoint: `POST /api/products`
- Body (JSON):
```json
{
  "barcode": "5449000130389",
  "name": "Coca-Cola Napój gazowany 1,75 l",
  "description": "woda, cukier, dwutlenek węgla, barwnik E 150d, kwas: kwas fosforowy, naturalne aromaty w tym kofeina",
  "price": 12.0,
  "stockQuantity": 22
}
```
- Odpowiedź: JSON dodanego produktu

### Aktualizacja produktu
- Endpoint: `PUT /api/products/update/{barcode}`
- Parametry:
    - `barcode` - kod EAN produktu
- Body (JSON): wszystkie pola do aktualizacji oprócz kodu EAN
- Odpowiedź: JSON zaktualizowanego produktu lub 404

### Zwiększanie stanu magazynowego
- Endpoint: `PATCH /api/products/addQuantity/{ean}?amount={liczba}`
- Parametry:
    - `ean` - kod EAN produktu
    - `amount` - liczba sztuk do dodania
- Przykład: `PATCH /api/products/addQuantity/5449000130389?amount=50`
- Odpowiedź: JSON produktu z nową ilością lub 404

## Struktura encji Product

| Pole | Typ | Opis |
|------|-----|-----|
| barcode | String | Kod EAN, klucz główny |
| name | String | Nazwa produktu |
| description | String | Opis produktu |
| price | BigDecimal | Cena produktu |
| stockQuantity | int | Ilość w magazynie
