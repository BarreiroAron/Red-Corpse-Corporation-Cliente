package Pantallas;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.badlogic.gdx.graphics.Color;
import Utiles.PersonalizarTexto;

import Entidades.Entidad;
import cartas.Carta;
import Utiles.Animaciones;
import Utiles.Imagen;
import Utiles.Recursos;
import Utiles.Render;
import Utiles.Util;
import cartasNormales.ThanksForPlaying;
import juegos.Juego;
import menues.MenuFinPartida;
import menues.MenuOpciones;
import menues.MenuPrincipal;
import red.Cliente;
import red.ClienteAPI;
import red.ClienteListener;
import red.HiloCliente;
import sonidos.SonidoManager;

public class JuegoPantalla implements Screen, ClienteListener {

    private BitmapFont bitmapFont;

    private Imagen Mesa;
    private Imagen Cartel;
    private Imagen Enemigo;
    private Imagen cartaEspalda;
    
    private Imagen botonIzquierda;
    private Imagen botonDerecha;

    private Texture BarraDeTiempo;

    private Carta tfp;

    private long cooldownMs = 500;
    private long ultimoClickTime = 0;
    private boolean esperandoRobo = false;

    private final Game game;   // para cambiar pantalla
    private final Juego juego; // tu lógica

    // Cámara y viewport
    private OrthographicCamera camera;
    private Viewport viewport;

    // variables para detectar el mouse (en coords virtuales)
    float mouseX;
    float mouseY;

    private Sound CartaTirada;

    final float ANCHOCARTA = 150f;
    final float LARGOCARTA = 250.f;

    private float ANCHO_PERSONAJE=450f;
	private float LARGO_PERSONAJE=700f;
    
    final float CENTRODEMESAX;
    final float CENTRODEMESAY;

    private HiloCliente hiloCliente;
    
    private boolean menuPausaActivo = false;
    
    private final ClienteAPI clienteAPI;

    public JuegoPantalla(Game game, Juego juego, ClienteAPI clienteAPI) {
        this.game = game;
        this.juego = juego;
        this.clienteAPI = clienteAPI;
        
        // inicializamos la cámara y el viewport con 1920x1080 (virtual)
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera);
        camera.position.set(1920 / 2f, 1080 / 2f, 0);

        // centro de la mesa según la resolución virtual
        CENTRODEMESAX = (1920 / 2f) - ANCHOCARTA / 2;
        CENTRODEMESAY = 1080 / 2f;
        
         clienteAPI.setListener(this);
    }

    @Override
    public void show() {
        this.bitmapFont = new BitmapFont();

        this.Mesa = new Imagen(Recursos.MESA_PRINCIPAL);
        this.Cartel = new Imagen(Recursos.CARTEL);
        this.Enemigo = new Imagen(Recursos.RIVAL1);
        
        this.botonIzquierda = new Imagen(Recursos.BOTON_IZQUIERDA);
        this.botonDerecha = new Imagen(Recursos.BOTON_DERECHA);

        this.BarraDeTiempo = new Texture(Recursos.TEXTURA_BARRA);

        this.CartaTirada = Gdx.audio.newSound(Gdx.files.internal("CartaTirada.mp3"));

        this.cartaEspalda = new Imagen(Recursos.MAZO_CARTAS);

        this.tfp = new ThanksForPlaying();
    }

    @Override
    public void render(float delta) {
        update(delta);

        this.menuPausaActivo = false;
        if (juego.isPartidaFinalizada()) return;

        Render.limpiarPantalla();

        camera.update();
        Render.batch.setProjectionMatrix(camera.combined);

        Render.batch.begin();

        this.Mesa.dibujar();
        this.Cartel.dibujar();

        dibujarPuntos(juego.getJugadorCliente());
        dibujarInterfazJugador(Render.batch, juego.getJugadorCliente(), delta);
        dibujarMazo(Render.batch, juego.getJugadorCliente(), delta);
        dibujarJugadores(Render.batch);
        dibujarMesaCartas(Render.batch);
        dibujarBarraTiempo();

        dibujarCartasSiguientes(Render.batch);
        
        dibujarCartasParaRobarAEleccion(Render.batch,delta);

        Animaciones.actualizarYDibujarMovimientos(Render.batch, delta);

        Render.batch.end();

        this.juego.actualizar();
        actualizarMouse();
    }


    private void dibujarCartasParaRobarAEleccion(SpriteBatch batch, float delta) {
		if(juego.isHabilidadActiva(juegos.HabilidadActiva.Tipo.ROBAR_CARTAS_A_ELECCION)) {
			juego.rebarajearMesa();
			ArrayList<Carta> mazo = juego.getMazo();
			int cantidadCartas = mazo.size();

			boolean hoveredIzqBoton = Animaciones.animarHover(
	                batch,
	                botonIzquierda,
	                (1650f ), LARGOCARTA*2+100f,
	                60f, 60f,
	                mouseX, mouseY,
	                1.2f,
	                8f,
	                delta);
			
			if (hoveredIzqBoton && Gdx.input.isTouched()) {
	            long tiempoActual = TimeUtils.millis();
	            if (tiempoActual - ultimoClickTime >= cooldownMs) {
	                if(juego.getIndiceDeCartasASacar()<=0) {
	                	juego.modificarIndiceDeCartasASacar(cantidadCartas-1);
	                }else { juego.modificarIndiceDeCartasASacar(juego.getIndiceDeCartasASacar() -1);}
	                ultimoClickTime = tiempoActual;
	            }
	        }
			
			boolean hoveredDerBoton = Animaciones.animarHover(
	                batch,
	                botonDerecha,
	                1650f+(ANCHOCARTA/2), LARGOCARTA*2+100f,
	                60f, 60f,
	                mouseX, mouseY,
	                1.2f,
	                8f,
	                delta);
			if (hoveredDerBoton && Gdx.input.isTouched()) {
	            long tiempoActual = TimeUtils.millis();
	            if (tiempoActual - ultimoClickTime >= cooldownMs) {
	                if(juego.getIndiceDeCartasASacar()>=cantidadCartas-1) {
	                	juego.modificarIndiceDeCartasASacar(0);
	                }else { juego.modificarIndiceDeCartasASacar(juego.getIndiceDeCartasASacar() +1);}
	                ultimoClickTime = tiempoActual;
	            }
	        }
			
				Carta cartaActual =mazo.get(juego.getIndiceDeCartasASacar());

			float x=1650f,y=350f;
				
				boolean hovered = Animaciones.animarHover(
		                batch,
		                cartaActual.getImagenCarta(),
		                x, y,
		                ANCHOCARTA, LARGOCARTA,
		                mouseX, mouseY,
		                1.2f,
		                8f,
		                delta);

		        if (hovered && Gdx.input.isTouched()) {
		            long tiempoActual = TimeUtils.millis();
		            if (tiempoActual - ultimoClickTime >= cooldownMs) {
		                juego.agregarCartaAJugador(cartaActual,juego.getHabilidad(juegos.HabilidadActiva.Tipo.ROBAR_CARTAS_A_ELECCION).getObjetivo());
		                mazo.remove(cartaActual);
		                ultimoClickTime = tiempoActual;
		                juego.restarcantidadDeCartasASacar();
		                if(juego.getIndiceDeCartasASacar()+1<cantidadCartas) {
		                	juego.modificarIndiceDeCartasASacar(juego.getIndiceDeCartasASacar()+1);
		                }else {
		                	juego.modificarIndiceDeCartasASacar(juego.getIndiceDeCartasASacar()-1);
		                }
		            }
		        }
		   if(juego.getcantidadDeCartasASacar()<=0){
			   System.out.println("Se elimina la carta");
			   juego.removeHabilidadActiva(juego.getHabilidad(juegos.HabilidadActiva.Tipo.ROBAR_CARTAS_A_ELECCION));
		   }
		}
	}

	private void update(float delta) {
    	if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
	        if (!menuPausaActivo) {
	            game.setScreen(new MenuOpciones(game, this));
	            menuPausaActivo = true;
	        }
	    }
       /* if (juego.isPartidaFinalizada()) {
            game.setScreen(new MenuFinPartida(game,hiloCliente));
            return;
        }*/
    }

    private void actualizarMouse() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouse); // convierte a coords virtuales
        mouseX = mouse.x;
        mouseY = mouse.y;
    }
    
    private void dibujarJugadores(SpriteBatch batch) {

	    Entidad jugadorActual = juego.getJugadorCliente();   // puede ser null

	    int total    = juego.getJugadores().size();

	    int visibles = (jugadorActual == null) ? total : total - 1;

	    if (visibles <= 0) return;
	    
	    float paso        = (float) camera.viewportWidth / (visibles + 1);
	    float alturaBase  = 550f;
	    float delta       = Gdx.graphics.getDeltaTime();   

	    int indiceVisible = 0;

	    for (Entidad jugador : juego.getJugadores()) {

	        if (jugador == jugadorActual) continue;         // saltamos turno

	        float x = paso * (indiceVisible + 1) - LARGO_PERSONAJE / 2f;
	        float y = alturaBase + (((indiceVisible % 2 == 0)&& total>3 )? 75f : 0f);


	        jugador.getCuerpo().draw(batch,                         //se dibuja por frames
	                     (x), (camera.viewportHeight -y),
	                     LARGO_PERSONAJE, ANCHO_PERSONAJE,
	                     delta);
	        
	        indiceVisible++;

	        if (juego.isHabilidadActiva(juegos.HabilidadActiva.Tipo.MOSTRAR_PUNTOS)) {
	            // pequeña etiqueta arriba del enemigo
	            float etiquetaY = (camera.viewportHeight - y) + ANCHO_PERSONAJE + 20f;
	            bitmapFont.draw(
	                batch,
	                jugador.getNombre() + ": " + jugador.getPuntos(),
	                x, 
	                etiquetaY
	            );
	            System.out.println(jugador.getNombre() + ": " + jugador.getPuntos()+" Habilidad de invisible activada");
	        }
	    }
	}

    private void dibujarMazo(SpriteBatch batch, Entidad jugador, float delta) {
        float y = 150f;
        float x = 1650f;
        float anchoCarta = 150f;
        float alturaCarta = 250f;

        boolean esMiTurno = Cliente.getTurnoActual() == Cliente.getPlayerIndex() ;
        
        boolean hovered = Animaciones.animarHover(
                batch,
                this.cartaEspalda,
                x, y,
                anchoCarta, alturaCarta,
                mouseX, mouseY,
                1.2f,
                8f,
                delta);
        
        if (hovered && Gdx.input.isTouched() && !esperandoRobo && esMiTurno) {
            long tiempoActual = TimeUtils.millis();
            if (tiempoActual - ultimoClickTime >= cooldownMs) {
                esperandoRobo = true;
                clienteAPI.solicitarRoboCarta();
                ultimoClickTime = tiempoActual;
            }
        }
    }

    private void dibujarBarraTiempo() {
        float progreso = juego.getProgresoTiempo(); // de 1.0 a 0.0
        float anchoMax = 400;
        float altoBarra = 20;

        float anchoActual = anchoMax * progreso;

        float centroX = camera.viewportWidth / 2f;
        float y = 50;

        float x = centroX - (anchoActual / 2f);

        Render.batch.draw(BarraDeTiempo, x, y, anchoActual, altoBarra);
    }

    private void dibujarInterfazJugador(SpriteBatch batch, Entidad jugadorActual, float delta) {
        dibujarMano(Render.batch, jugadorActual, delta);
        dibujarPuntos(jugadorActual);
    }

    private void dibujarPuntos(Entidad jugador) {
        if (jugador == null) return;
        Color colorCorporation = new Color(0.36f, 0.09f, 0.09f, 1f);
        PersonalizarTexto.configurarFuente(bitmapFont, 2.0f, colorCorporation, Color.BLACK);
        float posX = camera.viewportWidth - 300f;
        float posY = camera.viewportHeight - 250f;
        bitmapFont.draw(Render.batch, jugador.getNombre() + ": " + jugador.getPuntos(), posX, posY);
    }

    private void dibujarMesaCartas(SpriteBatch batch) {
        float anchoCarta = 150f, alturaCarta = 250f;

        if (!juego.getMesa().isEmpty()) {
            Carta cartaSuperior = juego.getMesa().get(juego.getMesa().size() - 1);
            cartaSuperior.getTexturaCarta().dibujar(batch, this.CENTRODEMESAX, this.CENTRODEMESAY, anchoCarta, alturaCarta);
        }
    }

    public void dibujarMano(SpriteBatch batch, Entidad jugador, float delta) {
        ArrayList<Carta> mano = jugador.getMano();
        if(mano.size()>=1) {
        	
        float anchoCarta = 150f, alturaCarta = 250f, esp = 10f;
        float total = mano.size() * anchoCarta + (mano.size() - 1) * esp;
        float inicioX = (camera.viewportWidth - total) / 2f;

        boolean clicProcesado = false;

        int indice = 0;
        
        //CLICK DE SONAANBULO
        if (juego.isHabilidadActivaEnJugador(juegos.HabilidadActiva.Tipo.SONAMBULO, jugador)
        		&& TimeUtils.timeSinceMillis(ultimoClickTime) >= cooldownMs
        		&& !clicProcesado
        		&& esMiTurno()){
        	
        	Carta carta = jugador.getMano().get((int)(Math.random() * mano.size()));
        	
        	 float destinoX = this.CENTRODEMESAX;
             float destinoY = this.CENTRODEMESAY;

             float x = inicioX + indice * (anchoCarta + esp);
             float y = 150f;
             
             Animaciones.iniciarMovimiento(
                     carta.getImagenCarta(),
                     x, y,
                     destinoX, destinoY,
                     anchoCarta, alturaCarta,
                     0.25f,
                     new Runnable() {
                         @Override
                         public void run() {
                             sonidoCartaTirada();
                             if (carta.getHabilidad() == cartas.Habilidad.BLOQUEO && juego.hayCartaPendiente()) {
                         	    // Bloqueo debe ser inmediato (sin delay) si hay carta esperando resolución
                         	    juego.activarBloqueoActivo(jugador);
                         	    juego.agregarCartaMesa(carta);
                         	    juego.sumarRonda();
                         	    jugador.removerCarta(carta); // si en ese bloque ya removías, mantenelo
                         	} else {
                         	    // el resto de las cartas usan delay
                         		clienteAPI.enviarJugarCarta(mano.indexOf(carta));
                         	    juego.sumarRonda();
                         	   jugador.removerCarta(carta);
                         	    // (mantener tu removerCarta si lo hacías acá)
                         	}
                         }
                     });
             ultimoClickTime = TimeUtils.millis();
             clicProcesado = true;

         indice++;
        }
        else 
        {
        	//CLICK DE CARTA NORMAL
        	 for (Carta carta : mano) {

                 float x = inicioX + indice * (anchoCarta + esp);
                 float y = 150f;

                 boolean hovered = Animaciones.animarHover(
                         batch, carta.getImagenCarta(),
                         x, y,
                         anchoCarta, alturaCarta,
                         mouseX, mouseY,
                         1.2f,
                         8f,
                         delta);

                 if (hovered) {
                     bitmapFont.draw(batch, carta.getDescripcion(), 20f, camera.viewportHeight - 20f);
                 }
                 
                 if (hovered) {
                     Color colorCorporation = new Color(0.36f, 0.09f, 0.09f, 1f);
                     PersonalizarTexto.configurarFuente(bitmapFont, 2.0f, colorCorporation, Color.BLACK);
                     
                     bitmapFont.draw(batch, carta.getDescripcion(), 20f, camera.viewportHeight - 20f);
                 }
                 
                 if (!clicProcesado
                         && hovered
                         && Gdx.input.justTouched()
                         && TimeUtils.timeSinceMillis(ultimoClickTime) >= cooldownMs
                         && !juego.isHabilidadActivaEnJugador(juegos.HabilidadActiva.Tipo.COLERA, jugador)
                         && esMiTurno()) {

                     float destinoX = this.CENTRODEMESAX;
                     float destinoY = this.CENTRODEMESAY;

                     Animaciones.iniciarMovimiento(
                             carta.getImagenCarta(),
                             x, y,
                             destinoX, destinoY,
                             anchoCarta, alturaCarta,
                             0.25f,
                             new Runnable() {
                                 @Override
                                 public void run() {
                                     sonidoCartaTirada();
                                     if (carta.getHabilidad() == cartas.Habilidad.BLOQUEO && juego.hayCartaPendiente()) {
                                    	    // Bloqueo debe ser inmediato (sin delay) si hay carta esperando resolución
                                    	    juego.activarBloqueoActivo(jugador);
                                    	    juego.agregarCartaMesa(carta);
                                    	    juego.sumarRonda();
                                    	    jugador.removerCarta(carta); // si en ese bloque ya removías, mantenelo
                                    	} else {
                                    	    // el resto de las cartas usan delay
                                    		clienteAPI.enviarJugarCarta(mano.indexOf(carta));
                                    	    juego.sumarRonda();
                                    	    // (mantener tu removerCarta si lo hacías acá)
                                    	}
                                 }
                             });

                     ultimoClickTime = TimeUtils.millis();
                     clicProcesado = true;
                 	}

                 indice++;
        	 	}
        	}
        }
    }
    
    private void dibujarCartasSiguientes(SpriteBatch batch) {
    	
        // Asegurate de usar el mismo Tipo que definiste en HabilidadActiva
        if (juego.isHabilidadActiva(juegos.HabilidadActiva.Tipo.MOSTRAR_CARTAS_SIGUIENTES)){

        float anchoCarta = 150f, alturaCarta = 250f, esp = 50f;
        //juego.rebarajearMesa();
        ArrayList<Carta> mazo = juego.getMazo();
        //prevenir quedarse sin cartas
        int camtidadCartas=mazo.size();
        Carta cartas[]=  new Carta[camtidadCartas>3? 3:camtidadCartas];
        
        float total = cartas.length * anchoCarta + (cartas.length - 1) * esp;
        float inicioX = (camera.viewportWidth - total) / 2f;
        
        for(int n=0;n<3 && n<camtidadCartas;n++) {
        	cartas[n]= mazo.get(n);
        }
        
        float x = camera.viewportWidth / 2f; // ajustar si hace falta
        float y = camera.viewportHeight / 2f + 60f;
        for(int i=0;i<cartas.length ;i++) {
        	cartas[i].getImagenCarta().dibujar(batch, inicioX + i * (anchoCarta + esp), y, anchoCarta, alturaCarta);;
        }
        }
    }

    private boolean esMiTurno(){
        // soy el jugador local?
        int miIndice = Cliente.getPlayerIndex();
        int turnoActual = Cliente.getTurnoActual();
        return miIndice == turnoActual;
    }

    public void sonidoCartaTirada() {
        SonidoManager.i().playSfx(this.CartaTirada);
    }
    
    @Override
    public void onCartaGlobalRecibida(String cartaId) {
	    Carta carta = Util.crearCartaDesdeId(cartaId);

	    if (carta != null) {
	        // ejemplo:
	        try {
				juego.getMesa().add(carta);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        System.out.println("[CLIENTE] Carta global agregada: " + cartaId);
	    }
	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height); // ajusta la vista cuando cambia la pantalla
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}

	@Override
	public void onMesaRecibida(int jugadorIndex, String cartaId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCartaRobadaLocal(String cartaId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRivalRoboCarta() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTurnoActualizado(int turno) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onModificacionDePuntos(int indiceJugador, int puntos, boolean esPorcentual) {

	    Entidad objetivo = juego.getJugadores().get(indiceJugador);
	    if (objetivo == null) return;
  
	    objetivo.modificarPuntos(puntos,esPorcentual);

	    System.out.println("[CLIENTE] Modificación de puntos: jugador=" + indiceJugador 
	        + " puntos=" + puntos + " porcentual=" + esPorcentual);
	}

	@Override
	public void onCartaJugada(int jugadorIndex, String cartaId) {

	    Carta carta = Util.crearCartaDesdeId(cartaId);
	    if (carta == null) return;

	    Entidad jugador = juego.getJugadores().get(jugadorIndex);

	    // Si el cliente tiene la misma mano, podés intentar buscarla por ID
	    Carta cartaReal = null;
	    for (Carta c : jugador.getMano()) {
	        if (c.getId().equals(cartaId)) {
	            cartaReal = c;
	            break;
	        }
	    }

	    // Si no está en mano, usamos la nueva (por si el cliente no guarda mano del rival)
	    if (cartaReal == null) cartaReal = carta;

	    // Ejecutar “igual que server”
	    juego.jugarCarta(cartaReal, jugador);

	    jugador.getMano().remove(cartaReal);
	    
	    System.out.println("[CLIENTE] Ejecuté carta desde server: " + cartaId + " jugador=" + jugadorIndex);
	}

	@Override
	public void onRoboCartaSelf(String cartaId) {
	    // crear la carta por ID (cliente visual)
	    Carta carta = Util.crearCartaDesdeId(cartaId);
	    if (carta == null) return;

	    int miIndex = Cliente.getPlayerIndex(); // o donde guardes tu playerIndex del INIT
	    Entidad yo = juego.getJugadores().get(miIndex);

	    yo.getMano().add(carta);

	    esperandoRobo = false;  // ya puede volver a robar si el server lo permite
	}


	@Override
	public void onRoboCartaRival(int jugadorIndex) {
	    // NO sabemos cuál carta es (bien)
	    // podés aumentar contador visual del rival o disparar animación
	    System.out.println("[CLIENTE] Rival " + jugadorIndex + " robó una carta.");
	}

	@Override
	public void onRoboDenegado(String razon) {
	    System.out.println("[CLIENTE] Robo denegado: " + razon);
	    esperandoRobo = false;
	}

	@Override
	public void onEliminarCartaJugador(int jugadorIndex, int indiceCarta) {

	    Entidad jugador = juego.getJugadores().get(jugadorIndex);
	    if (jugador == null) return;

	    if (indiceCarta < 0 || indiceCarta >= jugador.getMano().size()) return;

	    jugador.getMano().remove(indiceCarta);

	    System.out.println(
	        "[CLIENTE] Carta eliminada -> jugador=" + jugadorIndex +
	        " indice=" + indiceCarta
	    );
	}

	@Override
	public void onActualizaTiempo(float tiempoJuego) {
		juego.setTimepoJuegoServer(tiempoJuego);
	}

	@Override
	public void onEliminarJugador(int idxJugador) {
		if(idxJugador== Cliente.getPlayerIndex()) {
			game.setScreen(new MenuFinPartida(game, hiloCliente,false));
			 System.out.println(
				        "[CLIENTE] Fuiste eliminado -> jugador=" + idxJugador 
				    );
		}else{
			juego.eliminarYReacomodarJugador(juego.getJugadorPorIndice(idxJugador));
			 System.out.println(
				        "[CLIENTE] Jugador eliminado -> jugador=" + idxJugador 
				    );
		}
	}

	@Override
	public void onjugadorGanador() {
		juego.setGanadorTrue();
		game.setScreen(new MenuFinPartida(game, hiloCliente,true));
	}







}
