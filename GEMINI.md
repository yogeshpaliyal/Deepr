Deepr é un applicazione android in kotlin che ha lo scopo di salvare i link, scegliere un profilo, aggiungerci dei tag e delle note. 
l'applicazione viene sviluppata ufficialmente da Yogesh.
Io invece ho firmato il progetto in modo tale che posso contribuire tramite PR.
A causa di alcune diverse decisioni sulle feature dell'app, le differenze principali sono che io ho i profili in una schermata dedicata in basso a sinistra e mostrato in grida view, e inoltre uso il long press sul pulsante di creazione per creare una entry prepopolando dalla clipboard il field dell'URL; mentre yogesh ritiene che i profili debbano essere una feature opzionale, quindi sono accessibili da un'icona in alto a destra nella barra di ricerca della Home, e anche per la prepopolazione dalla clipboard l'applicazione rileva il link quando viene aperta e mostra un banner che se schiacciato apre la pagina di creazione e prepopola l'URL field (ma io lo trovavo fastidioso, quindi uso il long press)
# struttura della repo forkata
branch:
- YOGESH: viene mantenuto identico al master di Yogesh
- ARME: é la mia versione dell'app con le modifiche sui profili e clipboard auto population. alcuni branch prima di essere mergiati anche qua potrebbero necessitare di modifiche (da effettuare su un branch nuovo basato su quello) a causa delle modifiche tra la versione mia e quella di Yogesh
- branch che iniziano con un numero: branch vecchi ognuno basato sul precedente. sto cercando di renderli indipendenti così se Yogesh non mergia un branch può comunque mergiare quelli successivi, visto che non dipendono più da quelli precedenti. inoltre questi branch vecchi hanno più feature del necessario e quindi li sto dividendo in piú branch così che ognuno abbia la sua singola feature
- branch senza numero: sono la versione stand alone e feature specifica dei branch vecchi. per ora li sto testando prima di aprire la PR (CHE TI DIRÓ IO QUANDO APRIRE, NON FARLO DI TUA INIZIATIVA)
# Indicazioni pre creare nuovi branch
- metti un nome descrittivo e niente numeri
- deve essere basata su YOGESH
- deve contenere il file yml di build stable apk preso da ARME, così GitHub può compilare l'app quando pushi
# come aprire una PR
- quando ti dico IO di aprire una PR devi fare in modo che compaiano solo file di codice dell'app, quindi niente yml o altri file (quando pulisci il branch prima di aprire la PR fai il diff e vedi se hai scordato file inutili) 
- quando apri la PR metti nel commento principale (quello del "post") una descrizione di cosa fa quel branch, così Yogesh sa cosa aspettarsi
- se poi ti dico di allegare uno screenshot alla PR, rinomini lo screenshot in modo sensato e lo metti nel branch pr-asstes e poi scrivi un commento in cui metti lo screenshot, così Yogesh vede già l'implementazione grafica come viene
## 🏗️ Tech Stack (preso dal README)
The application is built using modern Android development practices and libraries:
- **UI:** Jetpack Compose
- **Navigation:** Jetpack Compose Navigation 3
- **ViewModel:** Android ViewModel
- **Database:** SQLDelight
- **Dependency Injection:** Koin
- **Asynchronous Operations:** Kotlin Coroutines
- **HTTP Client & Server:** Ktor