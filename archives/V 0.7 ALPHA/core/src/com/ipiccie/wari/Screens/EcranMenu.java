package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ipiccie.wari.ActiviteduJeu;

public class EcranMenu extends ScreenAdapter {
    ActiviteduJeu jeu;
    private TiledMap carte;
    private MapLayers layers;
    private float MONDE_LARGEUR;
    private float MONDE_HAUTEUR;
    private int echelle;
    private OrthogonalTiledMapRenderer rendu;
    private OrthographicCamera camera;
    private Perso personnage;
    private Texture jouer;
    private Texture boutique;
    private Texture equipement;
    private Texture infini;
    private Texture defi;
    private Texture histoire;
    private Animation<TextureRegion> attend;
    private Animation<TextureRegion> marche;
    private Animation<TextureRegion> attendT;
    private Animation<TextureRegion> marcheT;
    private int GRAVITE_LATERAL = 550;
    private EtatDuJeu etatDuJeu;
    private boolean mouvement = false;
    private final Pool<Rectangle> piscineRectangle = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return  new Rectangle();
        }
    };


    public EcranMenu (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        carte = new TiledMap();
        layers = carte.getLayers();
        float largeur = Gdx.graphics.getWidth();
        float hauteur = Gdx.graphics.getHeight();
        TiledMapTileLayer calque1 = new TiledMapTileLayer((int) ((largeur/hauteur)*544), 544, 4, 4);
        calque1.setName("calque1");
        echelle = calque1.getTileWidth();
        MONDE_HAUTEUR = (float) calque1.getHeight()/echelle;
        MONDE_LARGEUR = (float) calque1.getWidth()/echelle;
        TiledMapTileLayer calque2 = new TiledMapTileLayer((int) (2*MONDE_LARGEUR), (int)(MONDE_HAUTEUR), echelle, echelle);
        calque2.setName("calque2");
        creerCarte(calque1, calque2);
        layers.add(calque1);
        layers.add(calque2);
        rendu = new OrthogonalTiledMapRenderer(carte, 1); //met à l'échelle de la carte--> 1 unité = X px
        personnage = new Perso();
        personnage.etat = Perso.Etat.Attend;
        Texture notreHeros = new Texture("notre_heros_ninja.png");
        Texture notreHerosAvecStyle = new Texture("perso_truc_trainee.png");
        TextureRegion[] regions = TextureRegion.split(notreHeros,128,128)[0];
        TextureRegion[] regionsT = TextureRegion.split(notreHerosAvecStyle,30,20)[0];
        attend = new Animation<>(0,regions[3]);
        marche = new Animation<>(0.1F,regions[0],regions[1],regions[2]);
        attendT = new Animation<>(0,regionsT[3]);
        marcheT = new Animation<>(0.1F,regionsT[0],regionsT[1],regionsT[2]);
        marche.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        marcheT.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        EcranJeux.Perso.HEIGHT = regions[0].getRegionHeight()/2F;
        EcranJeux.Perso.WIDTH = regions[0].getRegionHeight()/2F;
        personnage.position.x = 32F;
        personnage.position.y = 32F;
        personnage.etat = Perso.Etat.Attend;
        etatDuJeu = EtatDuJeu.Accueil;


        camera = new OrthographicCamera();
        camera.setToOrtho(false,MONDE_HAUTEUR*echelle*(largeur / hauteur),MONDE_HAUTEUR*echelle);
    }

    @java.lang.Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float deltaTime = Gdx.graphics.getDeltaTime();
        personnage.etatTemporel += deltaTime;
        ecouteur();
        majJeu(deltaTime);
        camera.update();
        rendu.setView(camera);
        rendu.render();
        jeu.batch.setProjectionMatrix(camera.combined);
        jeu.batch.begin();
        renduPerso();
        jeu.batch.end();
    }

    public void renduPerso(){
        TextureRegion img  = null;
        TextureRegion img2  = null;
        switch (personnage.etat){
            case Attend:
                img = attend.getKeyFrame(personnage.etatTemporel);
                img2 = marcheT.getKeyFrame(personnage.etatTemporel);
                break;
            case Marche:
                img = marche.getKeyFrame(personnage.etatTemporel);
                img2 = marcheT.getKeyFrame(personnage.etatTemporel);
                break;
            default:
                break;
        }
        jeu.batch.draw(img2,personnage.position.x - EcranJeux.Perso.WIDTH/2,personnage.position.y, EcranJeux.Perso.WIDTH*3/2, EcranJeux.Perso.HEIGHT);
        jeu.batch.draw(img,personnage.position.x,personnage.position.y, EcranJeux.Perso.WIDTH, EcranJeux.Perso.HEIGHT);
    }

    public void majJeu(float deltaTime){
        if (mouvement){
            personnage.etat = Perso.Etat.Marche;
            switch (etatDuJeu){
                case ListeJeux:
                    personnage.position.mulAdd(new Vector2(GRAVITE_LATERAL,0),deltaTime);	//perso avance
                    camera.position.mulAdd(new Vector3(GRAVITE_LATERAL,0,0),deltaTime);   //camera avance
            }
            Gdx.app.log("debug"," "+personnage.position.x / echelle+" "+(int)((personnage.position.x/echelle)%MONDE_LARGEUR));
            if ((personnage.position.x / echelle) > MONDE_LARGEUR + echelle){
                mouvement = false;
                personnage.etat = Perso.Etat.Attend;
            }
        }
        //ne pas sortir de la carte
        //camera.position.x = MathUtils.clamp(camera.position.x,camera.viewportWidth/2F,MONDE_LARGEUR*echelle - camera.viewportWidth/2F);
        //camera.position.y = MathUtils.clamp(camera.position.y,camera.viewportHeight/2F,MONDE_HAUTEUR*echelle - camera.viewportHeight/2F);
    }

    public void ecouteur(){
        if (Gdx.input.justTouched()){
            TiledMapTileLayer calque;
            int X = 0;
            int Y = 0;
            switch (etatDuJeu){
                case Accueil:
                    calque = (TiledMapTileLayer) layers.get("calque1");
                    break;
                case ListeJeux:
                    calque = (TiledMapTileLayer) layers.get("calque2");
                    X += MONDE_LARGEUR;
                    break;
                default:
                    calque = (TiledMapTileLayer) layers.get("calque1");
                    break;
            }
            X += (int) (Gdx.input.getX() / (Gdx.graphics.getWidth()/MONDE_LARGEUR));
            Y += MONDE_HAUTEUR -  (Gdx.input.getY() / (Gdx.graphics.getHeight()/MONDE_HAUTEUR));
            Gdx.app.log("debug","ecouteur"+X+" "+Y);
            for (int x = X-1; x < X + 1; x++) {
                for (int y = Y - 10; y < Y+ 10; y++) {
                    if (calque.getCell(x,y) != null){
                        if (calque.getCell(x,y).getTile().getTextureRegion().getTexture()== jouer){
                            etatDuJeu = EtatDuJeu.ListeJeux;
                            mouvement = true;
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()== infini){
                            jeu.setScreen(new EcranInfini(jeu));
                        }
                        break;
                    }
                }
            }
        }
    }

    @java.lang.Override
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

    @java.lang.Override
    public void dispose() {
        jeu.batch.dispose();
        carte.dispose();
        rendu.dispose();
    }

    public void creerCarte(TiledMapTileLayer calque1, TiledMapTileLayer calque2){
        TiledMapTileLayer.Cell cell;
        jouer = new Texture("Bouton_jouer.png");
        equipement = new Texture("Bouton_equipement.png");
        boutique = new Texture("Bouton_boutique.png");
        infini = new Texture("Bouton_infini.png");
        defi = new Texture("bouton défi.PNG");
        histoire = new Texture("mode histoire.PNG");
        Texture logo = new Texture( ("logo_sans_fond_2.png"));
        int debLogoX = (int) (MONDE_LARGEUR/2F - (logo.getWidth()/2F )/echelle);
        int debLogoY = (508/echelle);
        int debJouerX =(int) (MONDE_LARGEUR/2F - (jouer.getWidth()/2F)/echelle);
        int debJouerY = (380/echelle);
        int debEquipementY = (288/echelle);
        int debBoutiqueY = (192/echelle);
        for ( int y = 0; y < logo.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(logo, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debLogoX,debLogoY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(jouer, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debJouerX,debJouerY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(equipement, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debJouerX,debEquipementY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(boutique, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debJouerX,debBoutiqueY - y, cell);
            }
        }
        Gdx.app.log("debug","creer carte"+debJouerX+" "+debJouerY);
        debLogoX += MONDE_LARGEUR;
        debJouerX+= MONDE_LARGEUR;
        for ( int y = 0; y < logo.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(logo, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque2.setCell(x+debLogoX,debLogoY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(infini, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque2.setCell(x+debJouerX,debJouerY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(histoire, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque2.setCell(x+debJouerX,debEquipementY - y, cell);
            }
        }
        for ( int y = 0; y < jouer.getHeight() / echelle; y++) {
            int x=0;
            for (TextureRegion tuile : TextureRegion.split(defi, calque1.getTileWidth(), calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque2.setCell(x+debJouerX,debBoutiqueY - y, cell);
            }
        }

        TextureRegion sol = new TextureRegion(new Texture("couleurs.png"),echelle,echelle);
        for (int y = 16/echelle; y < 32/echelle; y++){
            for (int x = 0; x < calque2.getWidth(); x++){
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(sol));
                calque2.setCell(x,y, cell);
            }
        }
    }

    public enum EtatDuJeu{
        Accueil,ListeJeux,Boutique,Equipement
    }

    private static class Perso{
        float WIDTH;
        float HEIGHT;

        enum Etat {
            Attend,Marche,Vole
        }
        final Vector2 position = new Vector2();
        Etat etat = Etat.Marche;
        float etatTemporel = 0;
    }
}
