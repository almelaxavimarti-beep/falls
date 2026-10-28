# 004. Rol de pista deducido y corregible

- **Estado:** Vigente
- **Fecha:** 2026-10-28

## Contexto
Las reglas dependen de qué es cada pista: una nota "fuera del acorde" solo tiene sentido si se sabe cuál es la melodía y cuáles los acordes.

## Decisión
Cada pista tiene un rol: melodía, acordes, bajo o batería. Falls lo deduce y el usuario lo puede corregir.

## Consecuencias
- Si la deducción falla, el usuario no se queda bloqueado.
- La deducción de roles es un problema propio que habrá que probar con MIDIs reales.
