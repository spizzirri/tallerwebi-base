package com.tallerwebi.dominio.perfil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

    private RepositorioPerfil repositorioPerfil;

    public ServicioPerfilImpl(RepositorioPerfil repositorioPerfil){
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

        // Recomendaciones según el objetivo fitness
        switch (perfil.getObjetivoFitness().toUpperCase()) {
            case "PERDIDA_DE_PESO":
                recomendacion.append("Para perder peso, es fundamental mantener una dieta balanceada y un déficit calórico moderado. ");
                break;
            case "GANANCIA_MUSCULAR":
                recomendacion.append("Para ganar masa muscular, aumenta tu ingesta calórica con alimentos ricos en proteínas. ");
                recomendacion.append("Es crucial descansar adecuadamente para permitir la recuperación muscular. ");
                break;
            case "DEFINICION":
                recomendacion.append("Para mantenerte en forma, sigue una dieta equilibrada y variada. ");
                break;
            default:
                recomendacion.append("Establece un objetivo fitness claro para obtener recomendaciones más específicas. ");
        }

        // Recomendaciones según la experiencia en ejercicio
        switch (perfil.getExperienciaEjercicio().toLowerCase()) {
            case "principiante":
                recomendacion.append("Como principiante, empieza con ejercicios de baja intensidad y aumenta gradualmente la carga y la intensidad. Aprende la técnica adecuada para evitar lesiones. ");
                break;
            case "intermedio":
                recomendacion.append("Como intermedio, varía tus rutinas para evitar el estancamiento y aumenta progresivamente la intensidad de tus entrenamientos. ");
                break;
            case "avanzado":
                recomendacion.append("Como avanzado, desafía tus límites con rutinas intensivas y técnicas avanzadas como el entrenamiento en circuito o HIIT. ");
                break;
        }

        // Recomendaciones según el peso y género
        if (perfil.getGenero() != null && perfil.getPeso() > 0) {
            if (perfil.getGenero().equalsIgnoreCase("masculino")) {
                if (perfil.getPeso() < 60) {
                    recomendacion.append("Tu peso es más bajo de lo habitual para un hombre. Asegúrate de consumir suficientes calorías y nutrientes para mantener tu salud y energía. ");
                } else if (perfil.getPeso() >= 60 && perfil.getPeso() <= 90) {
                    recomendacion.append("Tu peso está dentro del rango saludable para un hombre. Mantén una dieta equilibrada y ajusta tu ingesta calórica según tu nivel de actividad y objetivos fitness. ");
                } else if (perfil.getPeso() > 90) {
                    recomendacion.append("Tu peso es relativamente alto. Enfócate en una dieta balanceada y ejercicios que promuevan la salud cardiovascular y la fuerza muscular. ");
                }
            } else if (perfil.getGenero().equalsIgnoreCase("femenino")) {
                if (perfil.getPeso() < 50) {
                    recomendacion.append("Tu peso es más bajo de lo habitual para una mujer. Asegúrate de consumir suficientes calorías y nutrientes para mantener tu salud y energía. ");
                } else if (perfil.getPeso() >= 50 && perfil.getPeso() <= 70) {
                    recomendacion.append("Tu peso está dentro del rango saludable para una mujer. Mantén una dieta equilibrada y ajusta tu ingesta calórica según tu nivel de actividad y objetivos fitness. ");
                } else if (perfil.getPeso() > 70) {
                    recomendacion.append("Tu peso es relativamente alto. Enfócate en una dieta balanceada y ejercicios que promuevan la salud cardiovascular y la fuerza muscular. ");
                }
            } else {
                recomendacion.append("Tu peso está dentro de un rango saludable. Asegúrate de mantener una dieta equilibrada y ajustar tu actividad física según tus objetivos personales. ");
            }
        }

        // Recomendaciones según los suplementos seleccionados
        if (perfil.getSuplementos() != null && !perfil.getSuplementos().isEmpty() && !perfil.getSuplementos().equalsIgnoreCase("ninguno")) {
            switch (perfil.getSuplementos()) {
                case "Proteína de suero":
                    recomendacion.append("La proteína de suero ayuda en la recuperación muscular después de los entrenamientos intensos. ");
                    break;
                case "Creatina":
                    recomendacion.append("La creatina puede ser beneficiosa para mejorar la fuerza y la potencia durante los entrenamientos de alta intensidad. ");
                    break;
                case "Pre-entrenamientos":
                    recomendacion.append("Los pre-entrenamientos pueden aumentar tu energía y mejorar el enfoque durante tus sesiones de ejercicio. ");
                    break;
                case "Recuperativos":
                    recomendacion.append("Los suplementos recuperativos pueden acelerar la recuperación muscular y reducir el dolor post-entrenamiento. ");
                    break;
                case "Otros":
                    recomendacion.append("Consulte con un profesional de la salud o un nutricionista para obtener recomendaciones específicas sobre el suplemento seleccionado. ");
                    break;
                default:
                    recomendacion.append("Considera incorporar el suplemento ").append(perfil.getSuplementos());
                    recomendacion.append(" para complementar tu alimentación y apoyar tus objetivos de fitness. ");
            }
        }

        // Recomendaciones adicionales según la edad
        if (perfil.getEdad() < 20) {
            recomendacion.append("A tu edad, es importante establecer buenos hábitos de ejercicio y nutrición desde temprano. Incorpora actividades variadas y divertidas para mantener el interés. ");
        } else if (perfil.getEdad() >= 20 && perfil.getEdad() < 40) {
            recomendacion.append("En esta etapa, enfócate en construir y mantener tu masa muscular y capacidad cardiovascular. Una rutina equilibrada con ejercicios de fuerza y cardio es ideal. ");
        } else if (perfil.getEdad() >= 40 && perfil.getEdad() < 60) {
            recomendacion.append("A partir de los 40, es crucial mantener la flexibilidad y la salud cardiovascular. Considera incluir ejercicios de bajo impacto y técnicas de recuperación como el yoga o el pilates. ");
        } else if (perfil.getEdad() >= 60) {
            recomendacion.append("Para mantenerte saludable a los 60 y más, enfócate en ejercicios de bajo impacto que mejoren la movilidad, el equilibrio y la fuerza. Consulta con un profesional de la salud antes de comenzar un nuevo régimen de ejercicio. ");
        }

        return recomendacion.toString();
    }


}
