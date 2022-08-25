package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ipiccie.wari.ActiviteduJeu;
import com.ipiccie.wari.Toast;

import java.util.Objects;

public class  EcranMenu extends ScreenAdapter {
    ActiviteduJeu jeu;
    private Batch batch;
    private Stage etage;
    private Preferences prefs;
    private TiledMap carte;
    private MapLayers layers;
    private float MONDE_LARGEUR;
    private float MONDE_HAUTEUR;
    private int echelle;
    private OrthogonalTiledMapRenderer rendu;
    private OrthographicCamera camera;
    private OrthographicCamera camera2;
    private Perso personnage;
    private Texture jouer;
    private Texture boutique;
    private Texture equipement;
    private Texture infini;
    private Texture defi;
    private Texture histoire;
    private Texture retour;
    private Texture infos;
    private Texture param;
    private Skin skin;
    private Animation<TextureRegion> attend;
    private Animation<TextureRegion> marche;
    private Animation<TextureRegion> saute;
    private Animation<TextureRegion> attendT;
    private Animation<TextureRegion> marcheT;
    private Animation<TextureRegion> sauteT;
    private boolean dialogue = false;
    private int GRAVITE = -8;
    private int GRAVITE_LATERAL = 550;
    private EtatDuJeu etatDuJeu;
    private boolean mouvement = false;
    private final Pool<Rectangle> piscineRectangle = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return  new Rectangle();
        }
    };
    Toast.ToastFactory usineDeBiscotteGrillées;
    Toast tartine;


    public EcranMenu (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        prefs = Gdx.app.getPreferences("Parametres");
        skin = new Skin(Gdx.files.internal("skin.json"));
        carte = new TiledMap();
        layers = carte.getLayers();
        if(prefs.getBoolean("nouveau",true)) defval();
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
        Texture notreHeros = new Texture("notre_heros.png");
        Texture notreHerosAvecStyle = new Texture("perso_truc_trainee.png");
        TextureRegion[] regions = TextureRegion.split(notreHeros,200,280)[prefs.getInteger("perso",0)];
        TextureRegion[] regionsT = TextureRegion.split(notreHerosAvecStyle,30,20)[prefs.getInteger("traine",0)];
        attend = new Animation<>(0,regions[7]);
        marche = new Animation<>(0.1F,regions[0],regions[1],regions[2],regions[3],regions[4],regions[5]);
        saute = new Animation<>(0, regions[6]);
        attendT = new Animation<>(0,regionsT[3]);
        marcheT = new Animation<>(0.1F,regionsT[0],regionsT[1],regionsT[2]);
        sauteT = new Animation<>(0, regionsT[3]);
        marche.setPlayMode(Animation.PlayMode.LOOP);
        marcheT.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        personnage.position.x = 32F;
        personnage.position.y = 32F;
        personnage.etat = Perso.Etat.Attend;
        etatDuJeu = EtatDuJeu.Accueil;
        EcranJeux.Perso.HEIGHT = 90;
        EcranJeux.Perso.WIDTH = (90*200)/260F;


        camera = new OrthographicCamera();
        camera.setToOrtho(false,MONDE_HAUTEUR*echelle*(largeur / hauteur),MONDE_HAUTEUR*echelle);
        camera2 = new OrthographicCamera((MONDE_LARGEUR*echelle),(MONDE_HAUTEUR*echelle));
        Viewport porteVue = new StretchViewport(camera2.viewportWidth, camera2.viewportHeight, camera2);
        etage = new Stage(porteVue);
        etage.getViewport().apply();
        batch = etage.getBatch();
        etage.getCamera().position.set(MONDE_LARGEUR/2,MONDE_HAUTEUR/2,0F);
        Gdx.app.log("debug","viewport"+camera2.position+" "+MONDE_LARGEUR);
        usineDeBiscotteGrillées = new Toast.ToastFactory.Builder().font(new BitmapFont()).build();
    }

    private void defval() {
        prefs.putInteger("argent",50).flush();
        prefs.putInteger("perso",0).flush();
        prefs.putInteger("perso_possedes",2).flush();
        prefs.putBoolean("nouveau",false).flush();
    }

    @java.lang.Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float deltaTime = Gdx.graphics.getDeltaTime();
        personnage.etatTemporel += deltaTime;
        if (!dialogue) ecouteur();
        majJeu(deltaTime);
        etage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }});

        camera.update();
        rendu.setView(camera);
        rendu.render();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renduPerso();
        batch.end();
        if (tartine != null){
            tartine.render(delta);
        }
        etage.getBatch().setProjectionMatrix(etage.getCamera().combined);
        etage.getCamera().update();
        etage.act();
        etage.draw();
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
            case Vole:
                img = saute.getKeyFrame(personnage.etatTemporel);
                img2 = sauteT.getKeyFrame(personnage.etatTemporel);
                break;
            default:
                break;
        }
        if (personnage.retourne){
            if (!img.isFlipX()){
                img.flip(true,false);
                img2.flip(true,false);
            }
        }else{
            if(img2.isFlipX()){
                img2.flip(true,false);
            }
            if (img.isFlipX()){
                img.flip(true,false);
            }
        }
        //TODO: allonger img trainnée pur flip
        batch.draw(img2,personnage.position.x - EcranJeux.Perso.WIDTH/2,personnage.position.y, EcranJeux.Perso.WIDTH*3/2, EcranJeux.Perso.HEIGHT);
        batch.draw(img,personnage.position.x,personnage.position.y, EcranJeux.Perso.WIDTH, EcranJeux.Perso.HEIGHT);
    }

    public void majJeu(float deltaTime){
        if (mouvement){
            switch (etatDuJeu){
                case Accueil:
                    if (personnage.retourne){
                        personnage.etat = Perso.Etat.Marche;
                        personnage.position.mulAdd(new Vector2(-GRAVITE_LATERAL,0),deltaTime);	//perso avance
                        camera.position.mulAdd(new Vector3(-GRAVITE_LATERAL,0,0),deltaTime);   //camera avance
                        break;
                    }
                case ListeJeux:
                    personnage.etat = Perso.Etat.Marche;
                    personnage.position.mulAdd(new Vector2(GRAVITE_LATERAL,0),deltaTime);	//perso avance
                    camera.position.mulAdd(new Vector3(GRAVITE_LATERAL,0,0),deltaTime);   //camera avance
                    break;
                case Boutique:
                    if (personnage.retourne){
                        personnage.etat = Perso.Etat.Marche;
                        personnage.position.mulAdd(new Vector2(-GRAVITE_LATERAL/3F,0),deltaTime);	//perso avance
                        //camera.position.mulAdd(new Vector3(-GRAVITE_LATERAL,0,0),deltaTime);   //camera avance
                        break;
                    }
                case Equipement:
                    personnage.position.mulAdd(personnage.velocite,deltaTime);	//tombe
                    personnage.velocite.y += GRAVITE;
                    break;
            }
            if ((personnage.position.x / echelle) > MONDE_LARGEUR + echelle){
                mouvement = false;
                personnage.etat = Perso.Etat.Attend;
            }
            if (etatDuJeu == EtatDuJeu.Accueil && personnage.position.x <= 32F){
                personnage.position.x = 32F;
                camera.position.x = camera.viewportWidth/2F;
                mouvement = false;
                personnage.etat = Perso.Etat.Attend;
                personnage.retourne = false;
            }
            if (etatDuJeu == EtatDuJeu.Equipement && personnage.position.y+ personnage.HEIGHT<0){
                jeu.setScreen(new EcranEquipement(jeu));
            }
            if(etatDuJeu == EtatDuJeu.Boutique && personnage.position.x + EcranJeux.Perso.WIDTH<0){
                jeu.setScreen(new EcranBoutique(jeu));
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
            int t = 0;
            for (int x = X-1; x < X + 1; x++) {
                for (int y = Y - 1; y < Y+ 1; y++) {
                    if (calque.getCell(x,y) != null){
                        if (calque.getCell(x,y).getTile().getTextureRegion().getTexture()== jouer){
                            etatDuJeu = EtatDuJeu.ListeJeux;
                            mouvement = true;
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()== infini){
                            jeu.setScreen(new EcranInfini(jeu));
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture() == equipement){
                            etatDuJeu = EtatDuJeu.Equipement;
                            personnage.etat = Perso.Etat.Vole;
                            personnage.velocite.x = GRAVITE_LATERAL/5F;
                            personnage.velocite.y = 200;
                            mouvement =true;
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()== boutique){
                            etatDuJeu = EtatDuJeu.Boutique;
                            personnage.retourne = true;
                            mouvement = true;
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()==retour){
                            if (etatDuJeu == EtatDuJeu.ListeJeux){
                                etatDuJeu = EtatDuJeu.Accueil;
                                personnage.retourne = true;
                                mouvement = true;

                            }else if (X < MONDE_LARGEUR- (80F/echelle)){
                                infos();
                            }else{
                                param();
                            }
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()==histoire){
                            tartine  = usineDeBiscotteGrillées.create("Indisponible", Toast.Length.LONG);
                        }else if(calque.getCell(x,y).getTile().getTextureRegion().getTexture()==defi) {
                            tartine = usineDeBiscotteGrillées.create("Indisponible", Toast.Length.LONG);
                        }

                        t=1;
                        break;
                    }
                }
                if (t==1){
                    break;
                }
            }
        }
    }

    @java.lang.Override
    public void resize(int width, int height) {
        etage.getCamera().update();
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
        batch.dispose();
        carte.dispose();
        rendu.dispose();
    }

    public void creerCarte(TiledMapTileLayer calque1, TiledMapTileLayer calque2){
        TiledMapTileLayer.Cell cell;
        jouer = new Texture("Bouton_jouer.png");
        equipement = new Texture("Bouton_equipement.png");
        boutique = new Texture("Bouton_boutique.png");
        infini = new Texture("Bouton_infini.png");
        defi = new Texture("Bouton_defi.png");
        histoire = new Texture("Bouton_histoire.png");
        retour = new Texture("icons.png");
        Texture logo = new Texture( ("logo_sans_fond_2.png"));
        int debLogoX = (int) (MONDE_LARGEUR/2F - (logo.getWidth()/2F )/echelle);
        int debLogoY = (508/echelle);
        int debJouerX =(int) (MONDE_LARGEUR/2F - (jouer.getWidth()/2F)/echelle);
        int debJouerY = (380/echelle);
        int debEquipementY = (288/echelle);
        int debBoutiqueY = (192/echelle);
        int debRetourY = (int) (MONDE_HAUTEUR+2*echelle);
        int debRetourX = (32/echelle);
        int debParamX =(int) (MONDE_LARGEUR - (64F/echelle));
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

        for (int y = 64/echelle; y < (96/echelle);y++){
            int x = (int) MONDE_LARGEUR;
            for (TextureRegion tuile : TextureRegion.split(retour,calque1.getTileWidth(),calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque2.setCell(x+debRetourX,debRetourY - y, cell);
            }
        }

        for (int y = 96/echelle; y < (128/echelle);y++){
            int x =0;
            for (TextureRegion tuile : TextureRegion.split(retour,calque1.getTileWidth(),calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debParamX-(64/echelle),debRetourY - y+(32/echelle), cell);
            }
        }
        for (int y = 128/echelle; y < (160/echelle);y++){
            int x =0;
            for (TextureRegion tuile : TextureRegion.split(retour,calque1.getTileWidth(),calque1.getTileHeight())[y]) {
                x+=1;
                cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(tuile));
                calque1.setCell(x+debParamX,debRetourY - y+(64/echelle), cell);
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
        boolean retourne = false;

        enum Etat {
            Attend,Marche,Vole
            }
        final Vector2 position = new Vector2();
        final Vector2 velocite = new Vector2();
        Etat etat = Etat.Marche;
        float etatTemporel = 0;
    }

    public void infos(){
        dialogue = true;
        final Dialog dialog = new Dialog("", skin);
        Gdx.input.setInputProcessor(etage);
        Table table = new Table();
        Label texte = new Label("Le Projet W.A.R.I, version ALPHA.\n Developpe par IPIC&cie",skin);
        final ScrollPane defile = new ScrollPane(texte);
        TextButton btnYes = new TextButton("Ok", skin);
        table.add(defile).grow().row();
        table.add(btnYes);
        table.pad(20);
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.getContentTable().add(table).grow();
        btnYes.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dialog.hide();
                dialog.cancel();
                dialog.remove();
                dialogue = false;
                return true;
            }
        });
        dialog.setName("infoDialog");
        dialog.show(etage).setPosition(etage.getCamera().position.x-dialog.getWidth()/2F,etage.getCamera().position.y);
    }
    public void param() {
        dialogue = true;
        final Dialog dialog = new Dialog("", skin);
        Gdx.input.setInputProcessor(etage);
        Table table = new Table();
        final TextField codeBonus = new TextField("Code Bonus",skin);
        TextButton reinitialiser = new TextButton("Réinitialiser l'application",skin);
        TextButton valider = new TextButton("Valider",skin);
        TextButton annuler = new TextButton("Annuler", skin);
        reinitialiser.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                prefs.clear();
                prefs.flush();
                Gdx.app.exit();
                dialogue = false;
                return true;
            }
        });
        annuler.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dialog.hide();
                dialog.cancel();
                dialog.remove();
                dialogue = false;
                return true;
            }
        });
        valider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (Objects.equals(codeBonus.getText(), "argent")){
                    prefs.putInteger("argent",1000).flush();
                    tartine = usineDeBiscotteGrillées.create("c'est votre jour de chance", Toast.Length.SHORT);
                }
                dialog.hide();
                dialog.cancel();
                dialog.remove();
                dialogue = false;
                return true;
            }
        });
        table.add(new Label("Parametres",skin)).center().align(Align.top).size(10).row();
        table.pad(20);
        table.add(codeBonus).growX().row();
        table.add(reinitialiser).growX().row();
        table.add(valider).align(Align.left);
        table.add(annuler).align(Align.right);
        table.setSize(camera2.viewportWidth,camera2.viewportHeight);
        dialog.getContentTable().add(table).grow();
        dialog.show(etage).setPosition(etage.getCamera().position.x-dialog.getWidth()/2F,etage.getCamera().position.y);
    }
}
