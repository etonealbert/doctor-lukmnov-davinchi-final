package ar.edu.doctorlukmanov.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ColeccionesUtilTest {

    @Test
    void filtraSinModificarLaColeccionOriginal() {
        List<Integer> origen = new ArrayList<>(List.of(1, 2, 3, 4));

        List<Integer> pares = ColeccionesUtil.filtrar(origen, numero -> numero % 2 == 0);

        assertEquals(List.of(2, 4), pares);
        assertEquals(List.of(1, 2, 3, 4), origen);
    }

    @Test
    void indexaPorClaveYRechazaDuplicados() {
        Map<Integer, String> indice = ColeccionesUtil.indexarPor(List.of("uno", "tres"), String::length);

        assertEquals("uno", indice.get(3));
        assertEquals("tres", indice.get(4));
        assertThrows(IllegalArgumentException.class,
                () -> ColeccionesUtil.indexarPor(List.of("uno", "dos"), String::length));
    }
}
