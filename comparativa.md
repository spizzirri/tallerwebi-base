# Comparativa: VS Code + GitHub Copilot vs. Antigravity IDE

Datos verificados en la documentación oficial al 29/10/2026. El detalle de uso está en las guías del proyecto:
[vscode-copilot.md](vscode-copilot.md) y [antigravity.md](antigravity.md).

## 1. Modos de trabajo
| Necesidad | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Consultar sin modificar archivos | Agente **Ask** | Modo **Fast**, pidiendo solo análisis (no tiene modo de solo lectura) |
| Planificar antes de implementar | Agente **Plan** o `/plan` | Modo **Planning** o `/plan` |
| Implementar con autonomía | Agente **Agent** | Ejecución del plan aprobado (**Proceed**) o modo **Fast** |
| Edición puntual en el archivo abierto | Chat en línea (`Ctrl+I`) | Modo **Fast** |
| Modo Edit | Eliminado en VS Code 1.126 | No existe |

Detalle: [vscode-copilot.md](vscode-copilot.md) §2, [antigravity.md](antigravity.md) §2.

## 2. Planificación
| Aspecto | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Resultado | Plan en la memoria de la sesión | Artefactos: **Implementation Plan** y lista de tareas |
| Revisión | Se corrige conversando en el chat | Comentarios en línea sobre el texto o los pasos del plan |
| Aprobación | **Start Implementation** pasa el plan a Agent | **Proceed** |
| Cierre | Resumen en el chat | Artefacto **Walkthrough** con lo hecho y su verificación |

## 3. Modelos
| Aspecto | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Selección | Selector de modelos del chat; **Auto** elige según la tarea | Selector de modelos del panel del agente |
| Proveedores | Anthropic (Claude Haiku, Sonnet, Opus), OpenAI (GPT), Google (Gemini), xAI (Grok), otros | Google (Gemini Pro y Flash), Anthropic (Claude Sonnet y Opus), GPT-OSS |
| Modelos de Claude | Haiku 4.5 y 5.5, Sonnet 4.6, 5 y 5.5, Opus 4.8, 5 y 5.5 | Sonnet 5.5 y Opus 5.5 (Google AI Pro pago o superior); Sonnet 4.6 y Opus 4.6 se retiran el 2/11/2026 |
| Modelo base | Auto | Gemini 3.8 Flash, disponible en todos los planes |
| Restricción en planes gratuitos | Free y Student: solo **Auto**, sin elegir modelo | Sin Pro/Ultra: cuota semanal; modelos de terceros sujetos a plan |
| API key propia | No | No |

Detalle: [vscode-copilot.md](vscode-copilot.md) §3, [antigravity.md](antigravity.md) §3.

## 4. Planes y costo
| Plan | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Gratuito | **Free**: agent mode, 2000 completados/mes, cupo de GitHub AI Credits, solo Auto | Sin plan: cuota que se renueva semanalmente |
| Estudiantes | **Student** (gratis, estudiantes verificados): agent mode, cupo de AI Credits, solo Auto | Sin plan específico en la documentación |
| Individual pago | **Pro** USD 10/mes (1.000 AI Credits), **Pro+** USD 39/mes (3.900), **Max** USD 100/mes (10.000) | **Google AI Pro**: cuota cada 5 horas con tope semanal; **Google AI Ultra**: más cuota y acceso a modelos de terceros |
| Exceso de uso | Créditos adicionales (flex) | Compra de AI credits (solo Pro y Ultra) |
| Organizaciones | **Business** USD 19 y **Enterprise** USD 39 por usuario/mes | Gemini Enterprise |

## 5. Contexto y reglas del proyecto
| Aspecto | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Archivo común (este proyecto) | `AGENTS.md` (`chat.useAgentsMdFile`, activo por defecto) | `AGENTS.md` |
| Archivo propio | `.github/copilot-instructions.md` | `GEMINI.md` |
| Reglas por archivos | `.github/instructions/*.instructions.md` con `applyTo` | `.agents/rules/*.md` con `trigger: glob` |
| Reglas a demanda | `.instructions.md` con `description` | `.agents/rules/*.md` con `trigger: model_decision` o `manual` |
| Procedimientos reutilizables | Prompts `.github/prompts/*.prompt.md` (`/nombre`) y agentes `.github/agents/*.agent.md` | Skills `.agents/skills/<nombre>/SKILL.md` (los workflows dejaron de funcionar el 19/10/2026) |

Detalle: [vscode-copilot.md](vscode-copilot.md) §1, [antigravity.md](antigravity.md) §1.

## 6. Permisos y seguridad
| Aspecto | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Comandos de terminal | Confirmación antes de ejecutar herramientas y comandos | Preset **Request Review**, Default, Auto o Turbo, con listas de comandos permitidos y bloqueados |
| Revisión de planes | Manual en el chat | **Plan Review Policy**: **Always Ask** o Always Proceed |

Detalle: [vscode-copilot.md](vscode-copilot.md) §4, [antigravity.md](antigravity.md) §4.

## 7. Verificación en la UI
| Aspecto | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Navegador | Manual, fuera del agente | **Navegador integrado**: el agente usa Chrome con un perfil separado, interactúa con la página y guarda capturas y grabaciones |

Detalle: [antigravity.md](antigravity.md) §5.

## Fuentes
- [VS Code – Planning](https://code.visualstudio.com/docs/copilot/agents/planning)
- [VS Code 1.126 – Edit mode eliminado](https://code.visualstudio.com/updates/v1_126)
- [VS Code 1.142 – Ask como agente integrado](https://code.visualstudio.com/updates/v1_142)
- [VS Code – Custom instructions](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)
- [VS Code – Custom agents](https://code.visualstudio.com/docs/copilot/chat/chat-modes)
- [GitHub Copilot – Plans](https://docs.github.com/en/copilot/get-started/plans)
- [GitHub Copilot – Supported models](https://docs.github.com/en/copilot/reference/ai-models/supported-models)
- [Antigravity – Models](https://antigravity.google/docs/models)
- [Antigravity – Plans](https://antigravity.google/docs/plans)
- [Antigravity – Plan](https://antigravity.google/docs/plan)
- [Antigravity – Agent settings](https://antigravity.google/docs/agent-settings)
- [Antigravity – Rules](https://antigravity.google/docs/rules)
- [Antigravity – Workflows → Skills](https://antigravity.google/docs/migration/workflows-to-skills)
- [Antigravity – Browser](https://antigravity.google/docs/ide/browser)
