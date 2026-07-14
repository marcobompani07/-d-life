# {d}life

## DESIGN 

- ### percezione ###
    il campo sara diviso in quadrati logici, questi quadrati locici verranno utlizzati per la percezzione delle creature, ogni creatura dopo ogni suo movimento si posizionera nei quadrati logici che tocca. per vedere le creature nel range dei suoi sensi la creatura controllera i quandrati in range.
- ### movment ###
    le crature saranno controllate da tanti worker thread e saranno posizionate in una coda, nella coda sara anche presente un elemento separatore che separera a vari round di movimento, e conterra il tempo dell'unltima colta cheè stato incontrato che permettera di capire qunto tempo è passato durante l'esecuzione del turno, con questo sistema sara possibile capire quanto tempo è passato durante l'esecuzione del round in modo da poter settare il timeout tra i round per far si che il tempo tra l'inizio di un round e l'inizio del round successivo sia costante.

- ### damage camculation ###
    tutti i dnni saranno inflitti alla fine del movimento

- ### [mutation list](/doc/mutation_list.md) ###
## tecnolologies
- ### java fx camvas ###