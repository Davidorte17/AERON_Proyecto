package aeron.util;

import aeron.model.Airplane;

public interface TowerInterface {
    void registrarPeticion(Airplane avion);
    void liberarPista(Airplane avion);
}