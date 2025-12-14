package red;

public interface ClienteListener {
    // Cuando el server avisa que hay carta en mesa
    void onMesaRecibida(int jugadorIndex, String cartaId);

    // Cuando robo carta yo
    void onCartaRobadaLocal(String cartaId);

    // Cuando roba el rival
    void onRivalRoboCarta();

    //turno actualizado
    void onTurnoActualizado(int turno);

	void onCartaGlobalRecibida(String cartaId);

	void onModificacionDePuntos(int indiceJugador, int puntos, boolean esPorcentual);

	void onCartaJugada(int jugadorIndex, String cartaId);
	
	void onRoboCartaSelf(String cartaId);
	void onRoboCartaRival(int jugadorIndex);
	void onRoboDenegado(String razon);

	void onEliminarCartaJugador(int jugadorIndex, int indiceCarta);

}