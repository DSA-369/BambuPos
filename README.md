# 🛒 Bambú POS — Sistema Punto de Venta Android Multimoneda

Aplicación nativa para Android (Tablets Landscape) diseñada bajo arquitectura moderna para la gestión de ventas, inventario y facturación en tiempo real para entornos económicos con doble moneda ($ USD / Bs. VES).

---

## 🛠️ Stack Tecnológico & Arquitectura

- **Lenguaje:** Kotlin (v2.4.10)
- **UI & Layout:** Jetpack Compose (Material 3) con soporte para diseños adaptativos (Grids dinámicos / Split Screen).
- **Arquitectura:** Clean Architecture + MVVM + Repository Pattern.
- **Persistencia Local:** Room Database (v2.8.4) con procesador KSP (Kotlin Symbol Processing).
- **Asincronía & Estado:** StateFlow, Kotlin Coroutines y Unidirectional Data Flow (UDF).
- **Backend & Integraciones:** Supabase (Auth, Postgrest, Realtime), Ktor Client, Deep Linking (WhatsApp API).

---

## 🚀 Funcionalidades Clave

- **Terminal TPV Adaptativo:** Panel dividido para tablets con catálogo de productos en tiempo real y gestión dinámica de orden.
- **Motor Multimoneda Dinámico:** Conversión instantánea entre USD y VES con recálculo automático de subtotales.
- **Desglose Complejo de Pagos:** Cobro mixto simultáneo (Efectivo USD/VES, Punto de Venta, Pago Móvil) con algoritmo de sugerencia inteligente para saldos pendientes.
- **Comprobantes Digitales:** Integración directa con WhatsApp mediante Deep Links sanitizados para envío de recibos.

---

## 📂 Estructura del Proyecto

```text
com.bambu.pos/
├── core/         # Configuración global y base de datos
├── data/         # Room DB, Mappers e Implementación de Repositorios
├── domain/       # Modelos puros de negocio y Contratos
└── presentation/ # ViewModels, UI State y Pantallas Jetpack Compose
