Agora Load Testing
====================
#### Test du backend de l'application Agora à l'aide de l'outil de load testing open source Gatling

### Jouer les scénarios de test en ligne de commande
Les fichiers de simulations sont dans le répertoire src / test / kotlin / agora

Les deux tests de simulation disponibles sont: 
- QaGSimulation
- ConsultationSimulation

```
mvn gatling:test -Dgatling.simulationClass=agora.NomDuTest
```

### Environnement

- java 17.0.7
- maven 3.6.3