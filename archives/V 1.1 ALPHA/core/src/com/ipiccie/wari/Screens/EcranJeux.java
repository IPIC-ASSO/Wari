package com.ipiccie.wari.Screens;


//TODO: uniformiser les échelles

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.utils.StringBuilder;
import com.ipiccie.wari.ActiviteduJeu;

public class EcranJeux extends ScreenAdapter {

    ActiviteduJeu activiteduJeu;
    private static final float GRAVITE = -12;
    private static final float LATERAL_GRAVITE = 20;
    private static final float VAL_IMPULSION_SAUT = 1000;
    private static final float VITESSE_MAX_X = 200;
    private static final float VITESSE_MAX_Y = 420;
    private float MONDE_LARGEUR;
    private float MONDE_HAUTEUR;
    private float PERSO_DEBUT_X;
    private float PERSO_DEBUT_Y = 10;
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
    private BitmapFont police;
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

    public EcranJeux(ActiviteduJeu activiteduJeu){
        this.activiteduJeu = activiteduJeu;
    }

    @java.lang.Override
    public void show () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        police = new BitmapFont();
        batch = new SpriteBatch();
        laCarte = new TmxMapLoader().load("carte1.2.tmx");
        rendu = new OrthogonalTiledMapRenderer(laCarte, 1); //met à l'échelle de la carte--> 1 unité = X px
        MONDE_HAUTEUR = (float) laCarte.getProperties().get("height", Integer.class);
        MONDE_LARGEUR = (float) laCarte.getProperties().get("width", Integer.class);
        echelle = laCarte.getProperties().get("tilewidth", Integer.class);
        PERSO_DEBUT_Y = PERSO_DEBUT_Y * echelle;
        Texture notreHeros = new Texture("notre_heros_ninja.png");
        Texture notreHerosAvecStyle = new Texture("perso_truc_trainee.png");
        TextureRegion[] regions = TextureRegion.split(notreHeros,128,128)[0];
        TextureRegion[] regionsT = TextureRegion.split(notreHerosAvecStyle,30,20)[0];
        attend = new Animation<>(0,regions[3]);
        marche = new Animation<>(0.1F,regions[0],regions[1],regions[2]);
        saute = new Animation<>(0, regions[4]);
        attendT = new Animation<>(0,regionsT[3]);
        marcheT = new Animation<>(0.1F,regionsT[0],regionsT[1],regionsT[2]);
        sauteT = new Animation<>(0, regionsT[3]);
        marche.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        marcheT.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        Perso.HEIGHT = regions[0].getRegionHeight()/2F;
        Perso.WIDTH = regions[0].getRegionHeight()/2F;

        personnage = new Perso();
        personnage.etat = Perso.Etat.Attend;

        ready = new TextureRegion(new Texture("pret.png"));
        gameOver = new TextureRegion(new Texture("game_over.png"));

        float largeur = Gdx.graphics.getWidth();
        float hauteur = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false,15*echelle*(largeur / hauteur),15*echelle);
        leGrandReset();
    }


    @java.lang.Override
    public void render (float delta) {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.gl.glClearColor(1,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        maJjeu();
        camera.update();
        rendu.setView(camera);
        rendu.render();
        dessineJeu();
        Gdx.app.log("render", " "+delta);

    }


    public void resize(int width, int height) {
        //RàS
    }

    @java.lang.Override
    public void pause() {
        //RàS
    }

    @java.lang.Override
    public void resume() {
        //RàS
    }

    @java.lang.Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose () {
        batch.dispose();
        laCarte.dispose();
        rendu.dispose();
    }

    private void leGrandReset(){
        personnage = new Perso();
        PERSO_DEBUT_X = camera.viewportWidth/3;
        personnage.position.set(PERSO_DEBUT_X,PERSO_DEBUT_Y);
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
            batch.draw(ready, camera.position.x - ready.getRegionWidth()/2F,camera.position.y - ready.getRegionHeight());
        } else if(etatDuJeu == EtatDuJeu.Finissant){
            batch.draw(gameOver,camera.position.x - gameOver.getRegionWidth()/2F,camera.position.y - gameOver.getRegionHeight());
        }
        police.draw(batch,"score: "+(int)(personnage.position.x-PERSO_DEBUT_X),echelle+(camera.position.x-camera.viewportWidth/2F),camera.position.y+camera.viewportHeight/2F);
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
                Gdx.app.log("debug","rendu perso"+personnage.etatTemporel);
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
        float deltaTime = Gdx.graphics.getDeltaTime();
        etatTemporel += deltaTime;	//synchro
        personnage.etatTemporel += deltaTime;

        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){ //Si on touche l'écran, alors...
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

        float vel = collision(deltaTime);
        personnage.position.mulAdd(new Vector2(vel,personnage.velocite.y),deltaTime);	//perso avance

        if(personnage.position.y + Perso.WIDTH/2 < 0  || personnage.position.x + Perso.WIDTH < (camera.position.x - camera.viewportWidth/2F)){ //il est tombé ou il a disparu :O
            etatDuJeu = EtatDuJeu.Finissant;
            personnage.velocite.x = 0;
            personnage.velocite.y = -20;
        }
        // ++mouvements de caméra++
        // -personnage trop bas?
        // -personnage trop haut?
        // -suit le personnage.
        if (Math.abs(personnage.position.y+ Perso.HEIGHT ) < camera.position.y && personnage.velocite.y > 0){
            camera.position.mulAdd(new Vector3(personnage.velocite.x,0,0F),deltaTime);
        }else if(Math.abs(personnage.position.y+ Perso.HEIGHT) >  camera.position.y && personnage.velocite.y < 0){
            camera.position.mulAdd(new Vector3(personnage.velocite.x,0,0F),deltaTime);
        }
        else{
            camera.position.mulAdd(new Vector3(personnage.velocite.x,personnage.velocite.y,0F),deltaTime);
        }
        //ne pas sortir de la carte
        camera.position.x = MathUtils.clamp(camera.position.x,camera.viewportWidth/2F,MONDE_LARGEUR*echelle - camera.viewportWidth/2F);
        camera.position.y = MathUtils.clamp(camera.position.y,camera.viewportHeight/2F,MONDE_HAUTEUR*echelle - camera.viewportHeight/2F);
    }

    public float collision(float deltaTime){
        float velocite = personnage.velocite.x;
        Vector2 position = new Vector2(personnage.position.x, personnage.position.y);
        position.mulAdd(personnage.velocite,deltaTime);
        Rectangle persoRect = piscineRectangle.obtain();
        position.y-= 1;
        Perso.HEIGHT += 2;
        persoRect.set((position.x+1)/echelle,position.y/echelle,(Perso.WIDTH - 2)/echelle,Perso.HEIGHT/echelle);
        TiledMapTileLayer obstacles = (TiledMapTileLayer) laCarte.getLayers().get("obstacles");
        TiledMapTileLayer sol = (TiledMapTileLayer) laCarte.getLayers().get("sol");
        TiledMapTileLayer fin = (TiledMapTileLayer) laCarte.getLayers().get("fin");
        int startX;
        int startY;
        int endX;
        int endY;
        //verification haut/bas
        if (personnage.velocite.y > 0){	//perso qui monte
            startY = (int) (position.y + Perso.HEIGHT)/echelle;
            endY = (int) (position.y + Perso.HEIGHT)/echelle;
        }else{	//perso qui descend
            startY = (int) (position.y/echelle);
            endY = (int) (position.y/echelle);
        }
        startX = (int) position.x/echelle;
        endX = (int) ((position.x+ Perso.WIDTH)/echelle);
        tuiles = obtientTuiles(startX,startY,endX,endY,tuiles, new String[]{"obstacles","sol","fin"});
        for (Rectangle tuile : tuiles){
            if (persoRect.overlaps(tuile)){
                if(obstacles.getCell((int)tuile.x,(int)tuile.y)!= null) {
                    etatDuJeu = EtatDuJeu.Finissant;
                    personnage.velocite.x = 0;
                    personnage.velocite.y = -20;
                }else if(sol.getCell((int)tuile.x,(int)tuile.y)!= null){
                    if (personnage.velocite.y < 0){
                        personnage.aTerre = true;
                    }
                    personnage.velocite.y = 0;
                }else if(fin.getCell((int)tuile.x,(int)tuile.y)!= null){
                    activiteduJeu.setScreen(new EcranMenu(activiteduJeu));
                }
                break;
            }
        }

        //verification droite
        position = new Vector2(personnage.position.x, personnage.position.y);
        position.y += 4;
        Perso.HEIGHT -= 2;
        position.x+= 4;
        persoRect.set(position.x/echelle ,position.y/echelle,Perso.WIDTH/echelle,Perso.HEIGHT/echelle);
        if (velocite > 0){	//droite
            startX = (int) (position.x + Perso.WIDTH)/echelle;
            endX = (int) (position.x + Perso.WIDTH)/echelle;
        }
        startY = (int) (position.y/echelle);
        endY = (int) ((position.y + Perso.HEIGHT)/echelle);
        tuiles = obtientTuiles(startX,startY,endX,endY,tuiles,new String[]{"obstacles","sol","fin"});
        for (Rectangle tuile : tuiles){
            if (persoRect.overlaps(tuile)){
                if(obstacles.getCell((int)tuile.x,(int)tuile.y)!= null) {
                    etatDuJeu = EtatDuJeu.Finissant;
                    personnage.velocite.x = 0;
                    personnage.velocite.y = -20;
                }else if(sol.getCell((int)tuile.x,(int)tuile.y)!= null) {
                    velocite = 0;
                }else if(fin.getCell((int)tuile.x,(int)tuile.y)!= null) {
                    activiteduJeu.setScreen(new EcranMenu(activiteduJeu));
                }
                break;
            }
        }

        piscineRectangle.free(persoRect);
        return velocite;
    }

    public Array<Rectangle> obtientTuiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles, String[] calques){
        piscineRectangle.freeAll(tiles);
        tiles.clear();
        for (String calque: calques){
            TiledMapTileLayer layer = (TiledMapTileLayer) laCarte.getLayers().get(calque);
            for (int y = startY; y <= endY; y++){
                for (int x = startX; x<= endX; x++) {
                    TiledMapTileLayer.Cell cellule = layer.getCell(x, y);
                    if (cellule != null) {
                        Rectangle rect = piscineRectangle.obtain();
                        rect.set(x, y, 1, 1);
                        tiles.add(rect);
                    }
                }
            }
        }
        return tiles;

    }

    public enum EtatDuJeu{
        Lancement,Marchant,Finissant,P
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
