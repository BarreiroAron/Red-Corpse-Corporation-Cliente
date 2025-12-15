package red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.badlogic.gdx.Gdx;

import Entidades.Jugador;
import Pantallas.JuegoPantalla;
import juegos.Juego;
import menues.MenuPrincipal;
import menues.SalaDeEspera;

public class HiloCliente extends Thread implements ClienteAPI{
	
	private DatagramSocket conexion;
	private InetAddress ipServer;
	private int puerto = 8999;
	private boolean fin = false;
	
	public HiloCliente() {
	    try {
	        ipServer = InetAddress.getByName("255.255.255.255");
	        conexion = new DatagramSocket(); 
	        enviarMensaje("Conexion");
	    } catch (SocketException | UnknownHostException e) {
	        e.printStackTrace();
	    }
	}
	
	private ClienteListener listener;

    

	public void enviarMensaje(String msg) {
		System.out.println("Enviando mensaje a server");
		byte[] data=  msg.getBytes();
		DatagramPacket dp = new DatagramPacket(data, data.length,ipServer,puerto);
		try {
			conexion.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override 
	public void run(){
		System.out.println("Esperando respuesta");
		do {
			byte [] data = new byte[1024];
			DatagramPacket dp = new DatagramPacket(data,data.length);
			try {
				conexion.receive(dp);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			procesarMensaje(dp);
		}while(!fin);
	}

	private void procesarMensaje(DatagramPacket dp) {
		String msg = new String(dp.getData(), 0, dp.getLength()).trim();
		if(msg.equals("OK")) {
			System.out.println("El server recibio el mensaje");
			ipServer = dp.getAddress();
		}  else if (msg.startsWith("Jugadores:")) {
	        String numero = msg.substring("Jugadores:".length());
	        try {
	            int cant = Integer.parseInt(numero);
	            SalaDeEspera.setJugadoresConectados(cant);
	        } catch (NumberFormatException e) {
	            e.printStackTrace();
	        }
	    } else if(msg.equals("Empieza")) {
	        MenuPrincipal.pasarAConectado();
	        System.out.println("Ya empezo el juego");
	    }else if (msg.startsWith("INIT;")) {
	        // Formato: INIT;playerIndex;nombreEntidad;HAB1,HAB2,HAB3,...
	        String[] partes = msg.split(";");
	        int playerIndex = Integer.parseInt(partes[1]);
	        String nombreEntidad = partes[2];
	        String cartasStr = partes[3];
	        String[] cartasCodigos = cartasStr.split(",");

	        // Guardamos en red.Cliente para usarlo después
	        Cliente.setDatosInit(playerIndex, nombreEntidad, cartasCodigos);
	        System.out.println("INIT recibido: playerIndex=" + playerIndex +
	                ", entidad=" + nombreEntidad +
	                ", mano=" + cartasStr);

	    	}else if (msg.startsWith("JUGADORES;")) {
	    		String[] partes = msg.split(";");
	    		
	    		String cantJugadores = (partes[1]);
	    		for(int i=2; i< (Integer.parseInt(cantJugadores)*3)+2;i+=3) {
	    			String nombreEntidad = (partes[i]);
			        String puntos = partes[i+1];
			        String Idpersonaje = partes[i+2];
			        System.out.println("id personaje: " + Idpersonaje);     
			        Cliente.setIdPersonajesJugadores(Idpersonaje);
			        Cliente.agregarJugador(new Jugador(nombreEntidad, Integer.parseInt(puntos), null));
	    		}
	    	}else if (msg.startsWith("TURN;")) {
	    	    String[] partes = msg.split(";");
	    	    int turno = Integer.parseInt(partes[1]);

	    	    Cliente.setTurnoActual(turno);

	    	    System.out.println("[CLIENTE] Turno actualizado desde servidor: " + turno);
	    	}else if (msg.startsWith("CARTA_GLOBAL;")) {
	    	    String[] p = msg.split(";");
	    	    final String cartaId = p[1];
	    	    System.out.println("[CLIENTE] CARTA_GLOBAL recibida :" + cartaId);
	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onCartaGlobalRecibida(cartaId);
	    	        }
	    	    });
	    	}else if (msg.startsWith("PUNTOS;")) {
	    	    String[] p = msg.split(";");

	    	    final int indiceJugador = Integer.parseInt(p[1]);
	    	    final int puntos = Integer.parseInt(p[2]);
	    	    final boolean esPorcentual = p[3].equals("1");

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onModificacionDePuntos(indiceJugador, puntos, esPorcentual);
	    	        }
	    	    });
	    	}else if (msg.startsWith("CARTA_JUGADA;")) {
	    	    String[] p = msg.split(";");
	    	    final int jugadorIndex = Integer.parseInt(p[1]);
	    	    final String cartaId = p[2];

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onCartaJugada(jugadorIndex, cartaId);
	    	        }
	    	    });
	    	}else if (msg.startsWith("DRAW_SELF;")) {
	    	    String[] p = msg.split(";");
	    	    final String cartaId = p[1];

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) listener.onRoboCartaSelf(cartaId);
	    	    });
	    	}
	    	else if (msg.startsWith("DRAW_RIVAL;")) {
	    	    String[] p = msg.split(";");
	    	    final int jugadorIndex = Integer.parseInt(p[1]);

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) listener.onRoboCartaRival(jugadorIndex);
	    	    });
	    	}
	    	else if (msg.startsWith("DRAW_DENY;")) {
	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) listener.onRoboDenegado(msg);
	    	    });
	    	}else if (msg.startsWith("REMOVE_CARD;")) {
	    	    String[] p = msg.split(";");

	    	    final int jugadorIndex = Integer.parseInt(p[1]);
	    	    final int indiceCarta = Integer.parseInt(p[2]);

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onEliminarCartaJugador(jugadorIndex, indiceCarta);
	    	        }
	    	    });
	    	}else if (msg.startsWith("TIEMPO_DE_PPROGRESO;")) {
	    	    String[] p = msg.split(";");

	    	    final float tiempoJuego = Float.parseFloat(p[1]);

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onActualizaTiempo(tiempoJuego);
	    	        }
	    	    });
	    	}else if (msg.startsWith("JUGADOR_ELIMINADO;")) {
	    	    String[] p = msg.split(";");

	    	    final int idxJugador = Integer.parseInt(p[1]);

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onEliminarJugador(idxJugador);
	    	        }
	    	    });
	    	}else if (msg.startsWith("JUGADOR_GANADOR;")) {

	    	    Gdx.app.postRunnable(() -> {
	    	        if (listener != null) {
	    	            listener.onjugadorGanador();
	    	        }
	    	    });
	    	}else if (msg.startsWith("AJUSTAR_INDEX;")) {
	    	    String[] p = msg.split(";");
	    	    int indexEliminado = Integer.parseInt(p[1]);

	    	    Gdx.app.postRunnable(() -> {
	    	        Cliente.ajustarPlayerIndex(indexEliminado);
	    	    });
	    	}

	    }
	
	public void enviarJugarCarta(int indiceCarta) {
	    enviarMensaje("PLAY;" + indiceCarta);
	}
	
	public void cerrarConexion() {
	    if (conexion != null && !conexion.isClosed()) {
	        conexion.close();
	        System.out.println("Socket cerrado correctamente");
	    }
	}
	
	public void enviarListo() {
	    enviarMensaje("Listo");
	}


	@Override
	public void solicitarCartaMazo() {
		
	}

	@Override
	public void enviarFinDeTurno() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setListener(ClienteListener listener) {
        this.listener = listener;
    }

	@Override
	public void solicitarRoboCarta() {
	    enviarMensaje("DRAW"); // o "DRAW;" si preferís siempre con ;
	}
}

