package com.ipiccie.wari;


//TODO: uniformiser les échelles

import static sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.TAG;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import org.graalvm.compiler.core.amd64.AMD64ArithmeticLIRGenerator;

public class ActiviteduJeu extends ApplicationAdapter {
	private static final float GRAVITE = -2;
	private static final float LATERAL_GRAVITE = 3	;
	private static final float VAL_IMPULSION_SAUT = 350;
	private static final float PERSO_DEBUT_Y = 15;
	private static final float VITESSE_MAX_X = 25;
	private static final float VITESSE_MAX_Y = 100;
	private float MONDE_LARGEUR;
	private float MONDE_HAUTEUR;
	private int echelle;
	private TiledMap laCarte;
	private OrthogonalTiledMapRenderer rendu;
	OrthographicCamera camera;
	SpriteBatch batch;
	TextureRegion ready;
	TextureRegion gameOver;
	Perso personnage;
	private Animation<TextureRegion> attend;
	private Animation<TextureRegion> marche;
	private Animation<TextureRegion> saute;
	private Animation<TextureRegion> attendT;
	private Animation<TextureRegion> marcheT;
	private Animation<TextureRegion> sauteT;

	Vector2 gravite = new Vector2();
	float etatTemporel = 0;
	EtatDuJeu etatDuJeu = EtatDuJeu.Lancement;
	private Array<Rectangle> tuiles = new Array<>();
	private final Pool<Rectangle> piscineRectangle = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return  new Rectangle();
		}
	};

	
	@Override
	public void create () {
		batch = new SpriteBatch();
		laCarte = new TmxMapLoader().load("truc.tmx");
		rendu = new OrthogonalTiledMapRenderer(laCarte, 1); //met à l'échelle de la carte--> 1 unité = X px
		MONDE_HAUTEUR = laCarte.getProperties().get("height", Integer.class);
		MONDE_LARGEUR = laCarte.getProperties().get("width", Integer.class);
		echelle = laCarte.getProperties().get("tilewidth", Integer.class);
		Texture notreHeros = new Texture("perso_truc.png");
		Texture notreHerosAvecStyle = new Texture("perso_truc_trainee.png");
		TextureRegion[] regions = TextureRegion.split(notreHeros,20,20)[0];
		TextureRegion[] regionsT = TextureRegion.split(notreHerosAvecStyle,30,20)[0];
		attend = new Animation<>(0,regions[0]);
		marche = new Animation<>(0.1F,regions[1],regions[2],regions[3]);
		saute = new Animation<>(0, regions[4]);
		attendT = new Animation<>(0,regionsT[3]);
		marcheT = new Animation<>(0.1F,regionsT[0],regionsT[1],regionsT[2]);
		sauteT = new Animation<>(0, regionsT[3]);
		marche.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
		marcheT.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
		Perso.HEIGHT = (float)regions[0].getRegionHeight()/echelle;
		Perso.WIDTH = (float) regions[0].getRegionWidth()/echelle;

		personnage = new Perso();
		personnage.etat = Perso.Etat.Attend;

		ready = new TextureRegion(new Texture("pret.png"));
		gameOver = new TextureRegion(new Texture("game_over.png"));

		float largeur = Gdx.graphics.getWidth();
		float hauteur = Gdx.graphics.getHeight();
		camera = new OrthographicCamera();
		camera.setToOrtho(false,70*(largeur / hauteur),70);
		leGrandReset();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		maJjeu();
		camera.update();
		rendu.setView(camera);
		rendu.render();
		dessineJeu();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		laCarte.dispose();
		rendu.dispose();
	}

	private void leGrandReset(){
		personnage = new Perso();
		personnage.position.set(camera.viewportWidth/3,PERSO_DEBUT_Y);
		personnage.velocite.set(0,0);
		personnage.etat = Perso.Etat.Marche;
		gravite.set(LATERAL_GRAVITE,GRAVITE);
		camera.position.set(camera.viewportWidth/2F, camera.viewportHeight/2F,0);
	}

	public void dessineJeu(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		renduPerso();
		if (etatDuJeu == EtatDuJeu.Lancement){	//c'est partit!
			batch.draw(ready,camera.viewportWidth/2F + camera.position.x - ready.getRegionWidth()/2F,camera.viewportHeight/2F+camera.position.y - ready.getRegionHeight());
		} else if(etatDuJeu == EtatDuJeu.Finissant){
			batch.draw(gameOver,camera.viewportWidth/2F + camera.position.x - gameOver.getRegionWidth()/2F,camera.viewportHeight/2F+camera.position.y - gameOver.getRegionHeight());
		}
		batch.end();
	}

	public void renduPerso(){
		TextureRegion img  = null;
		TextureRegion img2  = null;
		switch (personnage.etat){
			case Attend:
				img = attend.getKeyFrame(personnage.etatTemporel);
				img2 = attendT.getKeyFrame(personnage.etatTemporel);
				break;
			case Marche:
				img = marche.getKeyFrame(personnage.etatTemporel);
				img2 = marcheT.getKeyFrame(personnage.etatTemporel);
				break;
			case Vole:
				img = saute.getKeyFrame(personnage.etatTemporel);
				img2 = sauteT.getKeyFrame(personnage.etatTemporel);
				break;
			default:
				break;
		}
		batch.draw(img2,personnage.position.x - Perso.WIDTH/2,personnage.position.y,Perso.WIDTH*3/2,Perso.HEIGHT);
		batch.draw(img,personnage.position.x,personnage.position.y,Perso.WIDTH,Perso.HEIGHT);
	}

	private void maJjeu(){
		Gdx.app.log("infos perso","position "+personnage.position+" velocite "+personnage.velocite);
		float deltaTime = Gdx.graphics.getDeltaTime();
		etatTemporel += deltaTime;	//synchro
		personnage.etatTemporel += deltaTime;

		if (Gdx.input.justTouched()){ //Si on touche l'écran, alors...
			if (etatDuJeu == EtatDuJeu.Lancement){	//c'est partit!
				etatDuJeu = EtatDuJeu.Marchant;
				personnage.etat = Perso.Etat.Marche;
			} else if(etatDuJeu == EtatDuJeu.Finissant){ 	//c'est finit :(
				etatDuJeu = EtatDuJeu.Lancement;
				leGrandReset();	//remise à 0 du "monde"
			}else if (etatDuJeu == EtatDuJeu.Marchant && personnage.aTerre){
				personnage.etat = Perso.Etat.Vole;
				personnage.velocite.y += VAL_IMPULSION_SAUT;	//le perso saute
				personnage.aTerre = false;
			}
		}
		if (etatDuJeu != EtatDuJeu.Lancement) personnage.velocite.add(gravite); //implémente la gravité

		if(personnage.aTerre){
			personnage.etat = Perso.Etat.Marche;
		}
		personnage.velocite.x = MathUtils.clamp(personnage.velocite.x, 0, VITESSE_MAX_X);
		personnage.velocite.y = MathUtils.clamp(personnage.velocite.y, -VITESSE_MAX_Y,VITESSE_MAX_Y);

		personnage.position.mulAdd(new Vector2(collision(),personnage.velocite.y),deltaTime);	//perso avance

		if(personnage.position.y + Perso.WIDTH/2 < 0  || personnage.position.x + Perso.WIDTH < (camera.position.x - camera.viewportWidth/2F)){ //il est tombé ou il a disparu :O
			etatDuJeu = EtatDuJeu.Finissant;
			personnage.velocite.x = 0;
			personnage.velocite.y = -20;
		}
		// ++mouvements de caméra++
		// -personnage trop bas?
		// -personnage trop haut?
		// -suit le personnage.
		if (Math.abs(personnage.position.y+ Perso.HEIGHT*echelle - camera.position.y) < camera.viewportHeight/2F && personnage.velocite.y > 0){
			camera.position.mulAdd(new Vector3(personnage.velocite.x,0,0F),deltaTime);
		}else if(Math.abs(personnage.position.y+ Perso.HEIGHT*echelle - camera.position.y) > camera.viewportHeight/2F && personnage.velocite.y < 0){
			camera.position.mulAdd(new Vector3(personnage.velocite.x,0,0F),deltaTime);
		}
		else{
			camera.position.mulAdd(new Vector3(personnage.velocite.x,personnage.velocite.y,0F),deltaTime);
		}
		//ne pas sortir de la carte
		camera.position.x = MathUtils.clamp(camera.position.x,camera.viewportWidth/2F,MONDE_LARGEUR*echelle - camera.viewportWidth/2F);
		camera.position.y = MathUtils.clamp(camera.position.y,camera.viewportHeight/2F,MONDE_HAUTEUR*echelle - camera.viewportHeight/2F);
	}

	public float collision(){
		float velocite = personnage.velocite.x;
		Rectangle persoRect = piscineRectangle.obtain();
		persoRect.set(personnage.position.x/echelle,personnage.position.y/echelle,Perso.WIDTH/echelle,Perso.HEIGHT/echelle);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		int startX;
		int startY;
		int endX;
		int endY;
		//verification haut/bas
		if (personnage.velocite.y > 0){	//perso qui monte
			startY = (int) (personnage.position.y + Perso.HEIGHT)/echelle - 1;
			endY = (int) (personnage.position.y + Perso.HEIGHT)/echelle;
		}else{	//perso qui descend
			startY = (int) (personnage.position.y/echelle);
			endY = (int) (personnage.position.y/echelle);
		}
		startX = (int) personnage.position.x/echelle;
		endX = (int) (personnage.position.x+ Perso.WIDTH)/echelle -1;
		tuiles = obtientTuiles(startX,startY,endX,endY,tuiles);
		for (Rectangle tuile : tuiles){
			if (persoRect.overlaps(tuile)){
				if (personnage.velocite.y < 0){
					personnage.aTerre = true;
				}
				personnage.velocite.y = 0;
				break;
			}
		}

		//verification droite
		if (velocite > 0){	//droite
			startX = (int) (personnage.position.x + Perso.WIDTH)/echelle - 1;
			endX = (int) (personnage.position.x + Perso.WIDTH)/echelle;
		}
		startY = (int) (personnage.position.y/echelle)+1;
		endY = (int) ((personnage.position.y + Perso.HEIGHT)/echelle)-1;
		tuiles = obtientTuiles(startX,startY,endX,endY,tuiles);
		for (Rectangle tuile : tuiles){
			if (persoRect.overlaps(tuile)){
				velocite = 0;
				break;
			}
		}
		piscineRectangle.free(persoRect);
		return velocite;
	}

	public Array<Rectangle> obtientTuiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles){
		piscineRectangle.freeAll(tiles);
		tiles.clear();
		TiledMapTileLayer layer = (TiledMapTileLayer) laCarte.getLayers().get("obstacles");
		for (int y = startY; y <= endY; y++){
			for (int x = startX; x<= endX; x++){
				TiledMapTileLayer.Cell cellule = layer.getCell(x,y);
				if(cellule != null){
					Rectangle rect = piscineRectangle.obtain();
					rect.set(x,y,1,1);
					tiles.add(rect);
				}
			}
		}
		return tiles;

	}

	public enum EtatDuJeu{
		Lancement,Marchant,Finissant
	}

	static class Perso {
		static float WIDTH;
		static float HEIGHT;
		static float VELOCITE_X;
		static float IMPULSION_SAUT;

		enum Etat {
			Attend,Marche,Cours,Vole
		}

		final Vector2 position = new Vector2();
		final Vector2 velocite = new Vector2();
		Etat etat;
		float etatTemporel = 0;
		boolean aTerre = true;
	}
}
