# ai_pylos

Pylos Project for labs Artificial Intelligence

als van new wil zetten in previous:
Kopieren van new in previous voor (student, evaluator en searchtree) en volgende zaken aanpassen (in StudentPlayerPrevious en EvaluatorPrevious):

- SearchTreeNew ->SearchTreePrevious
- EvaluatorNew -> EvaluatorPrevious
- TreeVisualizer in commentaar (want die werkt zogezegd op de new om dingen te testen)

Ideetjes

- mss toch beter telkens evalueren tov huidige speler en dat min dan effectief minimum neemt
- Kijken welke moves bv minimax doet en zoeken wrm die dat doet
- als geen vierkant kan blokkeren en geen vierkant kan creëren dan kijken of naar hoger niveau kan, als dat kan dan direct dat doen -> kijken als dat beter werkt dan huidige
- grote methodes in meer kleine methodes die worden opgeroepen voor duidelijkheid
- In de evaluatiefunctie kijken of gewonnen na simulatie: bv checken als andere speler reserve bollen = 0 dan heb je gewonnen en dan die score
- mss voordeel als in begin zo weinig mogelijk vierkanten, dus zoveel mogelijk diagonaal ofzo

V1

- Evalutie gereduceert tot het tellen van het overschot in de reserve.

V2

- Zelfde evaluator als V1
- Pruning (deftig deze keer)
- Random zetten als score hetzelfde is.

V3
 - volledige nieuwe minimax met alfa beta pruning


V4

- static simulator
- Andere optimalisaties lijke nenkel te vertragen zoals dingen uit de pruning lus halen.

 V5
 - zelfde als V3, werd gebruikt om nieuwe optimalisaties te testen, nog geen goede gevonden
 - dingen getest: bonus bij winst of verlies, na 1 stap blokkeren/vullen vierkant
 - OPMERKING: de pruning op lijn 116 doet hij precies nooit dus mss nog eens uitzoeken of werkt

Andere:

- assertions scheelt ook +-5% tijd
