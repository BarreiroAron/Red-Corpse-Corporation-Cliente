 package red;

import java.util.ArrayList;

import Entidades.Entidad;

public class Cliente {

    private static int playerIndex;
    private static String nombreEntidad;
    private static String[] cartasIniciales;
    private static ArrayList<Entidad> jugadores = new ArrayList<Entidad>();
    private static ArrayList<String> idPersonajesJugadores = new ArrayList<String>();
    private static int turnoActual = 0;
    
    public static void setDatosInit(int index, String entidad, String[] cartas) {
        playerIndex = index;
        nombreEntidad = entidad;
        cartasIniciales = cartas;
    }

    public static void setTurnoActual(int turno) {
        turnoActual = turno;
        System.out.println("El turno actual cambio es :" + turno);
        System.out.println("El turno del jugador es a :" + playerIndex);
    }

    public static int getTurnoActual() {
        return turnoActual;
    }
    
    public static int getPlayerIndex() {
        return playerIndex;
    }

    public static String getNombreEntidad() {
        return nombreEntidad;
    }

    public static String[] getCartasIniciales() {
        return cartasIniciales;
    }

	public static void agregarJugador(Entidad jugador) {
		jugadores.add(jugador);
	}

	public static ArrayList<Entidad> getJugadores() {
		return jugadores;
	}

	public static ArrayList<String> getIdPersonajesJugadores() {
		return idPersonajesJugadores;
	}

	public static void setIdPersonajesJugadores(String idPersonajesJugadores) {
		Cliente.idPersonajesJugadores.add(idPersonajesJugadores);
	}
	
	public static void ajustarPlayerIndex(int indexEliminado) {

	    // Si yo estaba después del eliminado, corro uno a la izquierda
	    if (playerIndex > indexEliminado) {
	        playerIndex--;
	    }

	    // Si yo soy el eliminado
	    if (playerIndex == indexEliminado) {
	        System.out.println("[CLIENTE] Fui eliminado");
	        // opcional: marcar estado, bloquear input, etc.
	    }

	    // Clamp de seguridad (por si quedó fuera de rango)
	    if (playerIndex < 0) {
	        playerIndex = 0;
	    }

	    System.out.println("[CLIENTE] PlayerIndex ajustado a: " + playerIndex);
	}

	
}

