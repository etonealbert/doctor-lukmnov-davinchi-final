package ar.edu.doctorlukmanov.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ColeccionesUtil {

    private ColeccionesUtil() {
    }

    public static <T> List<T> filtrar(List<T> origen, Predicate<? super T> criterio) {
        Objects.requireNonNull(origen, "La lista de origen es obligatoria.");
        Objects.requireNonNull(criterio, "El criterio es obligatorio.");
        List<T> resultado = new ArrayList<>();
        for (T elemento : origen) {
            if (criterio.test(elemento)) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    public static <T, K> Map<K, T> indexarPor(
            Collection<T> elementos, Function<? super T, ? extends K> extractorClave) {
        Objects.requireNonNull(elementos, "La colección es obligatoria.");
        Objects.requireNonNull(extractorClave, "El extractor de clave es obligatorio.");
        Map<K, T> resultado = new LinkedHashMap<>();
        for (T elemento : elementos) {
            K clave = extractorClave.apply(elemento);
            if (resultado.putIfAbsent(clave, elemento) != null) {
                throw new IllegalArgumentException("La clave " + clave + " está duplicada.");
            }
        }
        return resultado;
    }
}
