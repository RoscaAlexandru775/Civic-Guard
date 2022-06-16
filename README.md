# Civic-Guard

## Echipa:
- [Iordache Ovidiu](https://github.com/OvidiuIordache1)
- [Nume Prenume]()
- [Nume Prenume]()
- [Nume Prenume]()
- [Nume Prenume]()

## Demo:
Demo-ul aplicatiei poate fi gasit [aici]() 
//TODO

## Description:
Proiectul este o aplicatie in Android folosind Kotlin.
1) Ce face aplicatia?
  Aplicatia faciliteaza reclamarea problemelor ce pot apărea în viața de zi cu zi într-un oras. Un user poate semnala astfel de probleme (de exemplu semafoare defecte, gunoi aruncat în spatiul public etc.), iar o institutie (Firma apa canalizare / Primarie) poate modifica status-ul unei reclamatii (solved / unsolved). De asemenea, un user poate sa vada si reclamatiile facute de alti utilizatori, precum si locatia in care acestea au fost facute.
2) Cine o sa o folosească?
  Aplicatia poate fi folosita de orice utilizator de telefon Android (versiune minimă 4.6) care vrea să se implice activ în rezolvarea problemelor orasului sau.
3) De ce o să o o folosească?
  Pentru a semnala neregulile din oras.
4) Când o să o folosească?
  De cate ori observa probleme pe care le poate trimite către autoritățile competente spre solutionare.

## User Stories:
//TODO

## Backlog Creation:
Pentru a ne organiza task-urile in timpul dezvoltarii aplicatiei am folosit [Trello](https://trello.com/b/XMGkaVW6/civic-guard).
![alt text](https://github.com/RoscaAlexandru775/Civic-Guard/blob/create_complaint/images/trello.png)

## UML Use Case Diagram:
![alt text](https://github.com/RoscaAlexandru775/Civic-Guard/blob/create_complaint/images/UML_Diagram.jpg)

## Bug reporting:
1) Dupa ce un utilizator adauga o reclamatie, afisarea se realiza gresit (fiecare reclamatie era afisata de doua ori, deoarece se afisa atat lista veche, cat si lista actualizata).
   Problema a aparut deoarece, in loc sa stergem lista veche si sa o afisam pe cea actualizata, noi adaugam lista actulizata peste cea veche.
   Solutia: in loc sa asignam RecyclerView.adapter = adapter(updatedList), am folosit recyclerView.adapter?.notifyDataSetChanged().
2) Cand utilizatorul dorea sa adauge o reclamatie, aplicatia se inchidea inainte sa ceara permisiunile pentru camera si locatie,
   dar la a doua incercare aplicatie functia normal. Problema a fost felul in care ceream permisiunile - le ceream la crearea de noi activitati,
   cererea de permisiuni se efectueaza asincron iar functia getLocation() se apela inaintea sa avem permisiunile.
   Solutia a fost sa suprascriem functia onRequestPermissionsResult().

   /**get Permission*/
   //    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED
   //    && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED)
   
## Build Tool:
Am dezvoltat aplicatia in IntellIJ IDEA, iar pentru build tool am folosit Gradle, un sistem open-source pentru automatizarea procesului de construire al unei aplicatii.


## Source control:
//TODO

## Refactoring si teste automate:
//TODO
