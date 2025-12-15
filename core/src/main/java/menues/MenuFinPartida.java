package menues;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import Pantallas.PantallaCarga;
import Utiles.PersonalizarTexto;
import Utiles.Recursos;
import Utiles.Render;
import red.HiloCliente;

public class MenuFinPartida implements Screen{

	private final Game game;
	
	private Stage  stage;
	
	private ImageButton btnMenu ,btnJugarOtraVez;
	
	private Texture texMenuUp, texMenuDn;
	private Texture texJugarOtraVezUp, texJugarOtraVezDn;;
	private HiloCliente hiloCliente;
	private boolean ganador;
	
	private OrthographicCamera camera;
	private BitmapFont bitmapFont;
	private SpriteBatch batch;
	
	public MenuFinPartida(Game game,HiloCliente hiloCliente, boolean ganador) {
		this.game =game;
		this.hiloCliente= hiloCliente;
		this.ganador = ganador;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720); // o el tamaño que uses

		batch = new SpriteBatch();
		bitmapFont = new BitmapFont(); // fuente por defecto
		
	}

	@Override
	public void show() {
		
		 stage = new Stage(new ScreenViewport());
	     Gdx.input.setInputProcessor(stage);
	     
	     
	     String textoResultado;
	     Color colorResultado;

	     if (ganador) {
	         textoResultado = "GANASTE";
	         colorResultado = new Color(0.15f, 0.7f, 0.2f, 1f); // verde victoria
	     } else {
	         textoResultado = "PERDISTE";
	         colorResultado = new Color(0.8f, 0.1f, 0.1f, 1f); // rojo derrota
	     }
	     
	     PersonalizarTexto.configurarFuente(
	    	        bitmapFont,
	    	        3.0f,                 // tamaño grande
	    	        colorResultado,       // color dinámico
	    	        Color.BLACK           // borde
	    	);

	     
	     texMenuUp = new Texture(Gdx.files.internal(Recursos.BOTON_VOLVER_MENU));
	     texMenuDn = new Texture(Gdx.files.internal(Recursos.BOTON_VOLVER_MENU));
	     
	     texJugarOtraVezUp = new Texture(Gdx.files.internal(Recursos.BOTON_REINICIAR));
	     texJugarOtraVezDn = new Texture(Gdx.files.internal(Recursos.BOTON_REINICIAR));
	     
	     ImageButtonStyle backStyle = new ImageButtonStyle();
	        backStyle.up   = new TextureRegionDrawable(texMenuUp);
	        backStyle.down = new TextureRegionDrawable(texMenuDn);
	        
	        btnMenu = new ImageButton(backStyle);
	        btnMenu.addListener(new ChangeListener() {
	            @Override public void changed(ChangeEvent event, Actor actor) {
	                game.setScreen(new MenuPrincipal(game)); // vuelve al menu principal
	                dispose();                        // liberamos recursos
	            }
	     });
	        
	        ImageButtonStyle backStyle2 = new ImageButtonStyle();
	        backStyle2.up   = new TextureRegionDrawable(texJugarOtraVezUp);
	        backStyle2.down = new TextureRegionDrawable(texJugarOtraVezDn);
	        
	        btnJugarOtraVez = new ImageButton(backStyle2);
	        btnJugarOtraVez.addListener(new ChangeListener() {
	            @Override public void changed(ChangeEvent event, Actor actor) {
	                game.setScreen(new PantallaCarga(game,hiloCliente)); // vuelve al menu principal
	                dispose();                        // liberamos recursos
	            }
	     });
	        
	     Table root = new Table();
	     root.setFillParent(true);
	     root.defaults().pad(15);
	        
	     root.add(btnMenu).padTop(80).size(220, 80);
	     
	     root.add(btnJugarOtraVez).padTop(80).size(220, 80);
	     
	     stage.addActor(root);
	}

	@Override
	public void render(float delta) {
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

	    String textoResultado;
	    Color colorResultado;

	    if (ganador) {
	        textoResultado = "GANASTE";
	        colorResultado = new Color(0.15f, 0.7f, 0.2f, 1f);
	    } else {
	        textoResultado = "PERDISTE";
	        colorResultado = new Color(0.8f, 0.1f, 0.1f, 1f);
	    }

	    PersonalizarTexto.configurarFuente(
	            bitmapFont,
	            3.0f,
	            colorResultado,
	            Color.BLACK
	    );

	    GlyphLayout layout = new GlyphLayout(bitmapFont, textoResultado);

	    float x = (camera.viewportWidth - layout.width) / 2f;
	    float y = (camera.viewportHeight + layout.height) / 2f;

	    bitmapFont.draw(batch, layout, x, y);

	    batch.end();
		stage.act(delta);
        stage.draw();
	}

	@Override
	public void resize(int w, int h) { stage.getViewport().update(w, h, true);}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		 stage.dispose();
		 texMenuUp.dispose();
		 texMenuDn.dispose();
		 texJugarOtraVezUp.dispose();
		 texJugarOtraVezDn.dispose();
		 bitmapFont.dispose();
	}

}

