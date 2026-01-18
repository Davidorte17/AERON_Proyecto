# ✈️ AERON - Sistema de Simulación Aeroportuaria Distribuida

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)
![Concurrency](https://img.shields.io/badge/Pattern-Producer%20Consumer-blue?style=for-the-badge)

**AERON** es un simulador avanzado de tráfico aéreo en tiempo real. Modela las operaciones críticas de un aeropuerto (aterrizaje, embarque, despegue) gestionando recursos limitados mediante programación concurrente y ofreciendo monitorización remota a través de Sockets TCP.

---

## 📸 Demo Visual

| Torre de Control (Servidor) | Panel de Vuelos (Cliente Remoto) |
|:---:|:---:|
| ![Vista Torre](docs/img/captura_torre.png) | ![Vista Panel](docs/img/captura_panel.png) |
> *El sistema sincroniza el estado de 20 aviones y 5 operarios en tiempo real.*

---

## 🚀 Características Principales

### 🧠 Núcleo Concurrente
* **Gestión de Recursos:** Uso de **Semáforos** para controlar la cola de peticiones y **Monitores** para la asignación atómica de Pistas y Puertas (solución al problema de los Filósofos).
* **Arquitectura:** Modelo Productor-Consumidor.
    * *Productores:* 20 hilos `Airplane`.
    * *Consumidores:* 5 hilos `Operario`.
* **Prevención de Deadlocks:** Algoritmos de asignación "todo o nada".

### 📡 Sistema Distribuido (Práctica 7)
* **Arquitectura Cliente-Servidor:** Comunicación vía **Sockets TCP** (Puerto 9999).
* **Broadcast en tiempo real:** El servidor notifica instantáneamente los cambios de estado al `RemotePanel` (proceso independiente).

### 📊 Interfaz y Persistencia
* **GUI Avanzada:** Interfaz Swing con modo oscuro estilo "Terminal de Radar".
* **Logs Detallados:** Registro completo en `logs/concurrent/`.
* **Estadísticas:** Generación automática de `resumen_simulacion.csv` con tiempos de ciclo y ranking.

---

## 🛠️ Requisitos e Instalación

* **Java JDK 8** o superior.
* IDE recomendado: IntelliJ IDEA o Eclipse.

1.  Clonar el repositorio:
    ```bash
    git clone [https://github.com/tu-usuario/aeron-project.git](https://github.com/tu-usuario/aeron-project.git)
    ```
2.  Abrir el proyecto en tu IDE y esperar a que se indexe.

---

## ▶️ Guía de Ejecución

El proyecto está configurado para un arranque sencillo, pero consta de dos partes:

### Paso 1: Iniciar la Simulación (Servidor)
Ejecuta la clase principal:
`src/main/java/aeron/main/Simulation.java`

1.  El servidor arrancará en el puerto **9999**.
2.  El sistema intentará lanzar automáticamente el cliente remoto.
3.  Aparecerá un diálogo: **"Pulse OK para iniciar"**. (NO PULSAR TODAVÍA).

### Paso 2: Verificar el Cliente (Panel Remoto)
Si el cliente no se ha abierto automáticamente:
Ejecuta manualmente la clase: `src/main/java/aeron/net/RemotePanel.java`

### Paso 3: ¡Despegue!
Una vez ambas ventanas estén abiertas y alineadas, pulsa **OK** en el diálogo de la simulación.
> *Observarás cómo los aviones nacen, solicitan pista y los datos se replican en el panel remoto.*

---

## 📂 Estructura del Proyecto

```text
aeron/
├── concurrent/       # Lógica multihilo (Torre, Operarios)
├── model/            # Entidades (Avión, Estados)
├── net/              # Sockets (Server, RemotePanel)
├── sequential/       # Implementación legacy (Práctica 2)
├── util/             # Helpers (Logger, Json, Stats, GUI)
└── exceptions/       # Excepciones personalizadas
