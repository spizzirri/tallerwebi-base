package com.tallerwebi.dominio.perfil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

    private RepositorioPerfil repositorioPerfil;

    public ServicioPerfilImpl(RepositorioPerfil repositorioPerfil) {
        this.repositorioPerfil = repositorioPerfil;
    }

    @Override
    @Transactional
    public void guardarPerfil(Perfil perfil) {
        repositorioPerfil.guardarPerfil(perfil);
    }

    @Override
    @Transactional
    public Perfil obtenerPerfilPorId(Long id) {
        return repositorioPerfil.obtenerPerfilPorId(id);
    }

    @Override
    @Transactional
    public void actualizarPerfil(Long idPerfil, Perfil perfilActualizado) {
        // Verificar si el perfil con el id proporcionado existe en la base de datos
        Perfil perfilExistente = repositorioPerfil.obtenerPerfilPorId(idPerfil);
        if (perfilExistente == null) {
            throw new IllegalArgumentException("No se encontró ningún perfil con el ID proporcionado: " + idPerfil);
        }

        // Copiar los atributos actualizados al perfil existente
        perfilExistente.setEdad(perfilActualizado.getEdad());
        perfilExistente.setPeso(perfilActualizado.getPeso());
        perfilExistente.setAltura(perfilActualizado.getAltura());
        perfilExistente.setGenero(perfilActualizado.getGenero());
        perfilExistente.setObjetivoFitness(perfilActualizado.getObjetivoFitness());
        perfilExistente.setCondicionesAlternas(perfilActualizado.getCondicionesAlternas());
        perfilExistente.setExperienciaEjercicio(perfilActualizado.getExperienciaEjercicio());
        perfilExistente.setSuplementos(perfilActualizado.getSuplementos());

        // Actualizar el perfil en la base de datos
        repositorioPerfil.actualizarPerfil(perfilExistente);
    }

    @Override
    public String generarRecomendacion(Perfil perfil) {
        StringBuilder recomendacion = new StringBuilder();

        // Obtener atributos del perfil
        String objetivoFitness = perfil.getObjetivoFitness().toUpperCase();
        String experienciaEjercicio = perfil.getExperienciaEjercicio().toLowerCase();
        String genero = perfil.getGenero().toLowerCase();
        double peso = perfil.getPeso();
        String suplementos = perfil.getSuplementos();
        int edad = perfil.getEdad();

        // Combinaciones de atributos para recomendaciones específicas
        if (objetivoFitness.equals("PERDIDA_DE_PESO") && experienciaEjercicio.equals("principiante") && edad < 20) {
            recomendacion.append("Para perder peso siendo joven y principiante, mantén una dieta balanceada y comienza con ejercicios de baja intensidad. Aumenta gradualmente la intensidad y asegúrate de consumir suficientes nutrientes. ");
        } else if (objetivoFitness.equals("GANANCIA_MUSCULAR") && experienciaEjercicio.equals("intermedio") && edad >= 20 && edad < 40) {
            recomendacion.append("Para ganar masa muscular como intermedio en tus 20s o 30s, aumenta tu ingesta calórica con proteínas y varía tus rutinas. Descansa adecuadamente para la recuperación muscular. ");
        } else if (objetivoFitness.equals("DEFINICION") && experienciaEjercicio.equals("avanzado") && edad >= 40) {
            recomendacion.append("Para definir tus músculos como avanzado y mayor de 40, sigue una dieta equilibrada y realiza rutinas intensivas. Mantén la flexibilidad con ejercicios de bajo impacto. ");
        } else {
            // Recomendaciones generales si no se encuentra una combinación específica
            switch (objetivoFitness) {
                case "PERDIDA_DE_PESO":
                    recomendacion.append("Para perder peso, mantén una dieta balanceada y un déficit calórico moderado. ");
                    break;
                case "GANANCIA_MUSCULAR":
                    recomendacion.append("Para ganar masa muscular, aumenta tu ingesta calórica con alimentos ricos en proteínas. ");
                    break;
                case "DEFINICION":
                    recomendacion.append("Para definir tus músculos, sigue una dieta equilibrada y variada. ");
                    break;
                default:
                    recomendacion.append("Establece un objetivo fitness claro para obtener recomendaciones más específicas. ");
            }

            switch (experienciaEjercicio) {
                case "principiante":
                    recomendacion.append("Como principiante, empieza con ejercicios de baja intensidad y aumenta gradualmente la carga. ");
                    break;
                case "intermedio":
                    recomendacion.append("Como intermedio, varía tus rutinas para evitar el estancamiento y aumenta la intensidad. ");
                    break;
                case "avanzado":
                    recomendacion.append("Como avanzado, desafía tus límites con rutinas intensivas y técnicas avanzadas. ");
                    break;
            }

            if (genero != null && peso > 0) {
                if (genero.equals("masculino")) {
                    if (peso < 60) {
                        recomendacion.append("Tu peso es bajo para un hombre, consume suficientes calorías y nutrientes. ");
                    } else if (peso >= 60 && peso <= 90) {
                        recomendacion.append("Tu peso está en el rango saludable para un hombre, mantén una dieta equilibrada. ");
                    } else if (peso > 90) {
                        recomendacion.append("Tu peso es alto, enfócate en una dieta balanceada y ejercicios cardiovasculares. ");
                    }
                } else if (genero.equals("femenino")) {
                    if (peso < 50) {
                        recomendacion.append("Tu peso es bajo para una mujer, consume suficientes calorías y nutrientes. ");
                    } else if (peso >= 50 && peso <= 70) {
                        recomendacion.append("Tu peso está en el rango saludable para una mujer, mantén una dieta equilibrada. ");
                    } else if (peso > 70) {
                        recomendacion.append("Tu peso es alto, enfócate en una dieta balanceada y ejercicios cardiovasculares. ");
                    }
                }
            }

            if (edad < 20) {
                recomendacion.append("A tu edad, establece buenos hábitos de ejercicio y nutrición. Incorpora actividades variadas y divertidas. ");
            } else if (edad >= 20 && edad < 40) {
                recomendacion.append("En esta etapa, enfócate en construir y mantener tu masa muscular y capacidad cardiovascular. ");
            } else if (edad >= 40 && edad < 60) {
                recomendacion.append("A partir de los 40, mantén la flexibilidad y la salud cardiovascular con ejercicios de bajo impacto. ");
            } else if (edad >= 60) {
                recomendacion.append("A los 60 y más, enfócate en ejercicios de bajo impacto que mejoren la movilidad y el equilibrio. ");
            }

            if (suplementos != null && !suplementos.isEmpty() && !suplementos.equalsIgnoreCase("ninguno")) {
                switch (suplementos) {
                    case "Proteína de suero":
                        recomendacion.append("La proteína de suero ayuda en la recuperación muscular. ");
                        break;
                    case "Creatina":
                        recomendacion.append("La creatina mejora la fuerza y potencia. ");
                        break;
                    case "Pre-entrenamientos":
                        recomendacion.append("Los pre-entrenamientos aumentan tu energía y enfoque. ");
                        break;
                    case "Recuperativos":
                        recomendacion.append("Los recuperativos aceleran la recuperación muscular. ");
                        break;
                    case "Otros":
                        recomendacion.append("Consulta con un profesional para recomendaciones específicas sobre suplementos. ");
                        break;
                    default:
                        recomendacion.append("Considera incorporar el suplemento ").append(suplementos).append(" para apoyar tus objetivos. ");
                }
            }
        }

        return recomendacion.toString().trim(); // Trim para eliminar espacios en blanco adicionales
    }


}