package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ipiccie.wari.ActiviteduJeu;
import com.ipiccie.wari.Toast;

public class EcranBoutique extends ScreenAdapter {
    ActiviteduJeu jeu;
    private Stage etage;
    private OrthographicCamera camera;
    private Viewport porteVue;
    private Texture notreHeros;
    private static Preferences prefs;
    private Image image;
    private final Integer[] prix = {0,200,300,400,800,50000};
    private final Integer[] nbPrem = {1,2,3,5,7,11,13,17,19,23,29};
    private int choix;
    static final int tuileH = 280;
    static final int tuileL = 200;
    private ScrollPane defile1;
    private final Table table = new Table();
    private final Table moyenneTable = new Table();
    private final Table petiteTable = new Table();
    private final Table miniTable = new Table();
    private final Table microTable = new Table();
    private Skin skin;
    private TextButton btnAchat;
    float hauteur;
    float largeur;
    static final int pad = 20;
    Toast.ToastFactory usineDeBiscotteGrillees;
    Toast tartine;

    public EcranBoutique (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        prefs = Gdx.app.getPreferences("Parametres");
        skin = new Skin(Gdx.files.internal("skin2.json"));
        hauteur = Gdx.graphics.getHeight();
        largeur = Gdx.graphics.getWidth();
        camera = new OrthographicCamera(800,800*(hauteur/largeur));
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        porteVue = new StretchViewport(camera.viewportWidth, camera.viewportHeight, camera);
        etage = new Stage(porteVue);
        etage.getViewport().apply();
        Gdx.input.setInputProcessor(etage);

        notreHeros = new Texture("notre_heros.png");
        TextButton btnRetour = new TextButton("<--",skin);
        TextButton btnHeros = new TextButton("HEROS",skin);
        TextButton btnTrainee = new TextButton("TRAINEE",skin);
        TextButton btnSpecial = new TextButton("OFFRES SPECIALES",skin);
        TextButton btnmission = new TextButton("QUESTES",skin);
        btnAchat = new TextButton("ACHETER",skin);
        btnRetour.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                jeu.setScreen(new EcranMenu(jeu));
                return true;
            }});
        btnHeros.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("indisponible", Toast.Length.SHORT);
                return true;
            }});
        btnTrainee.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("en attente de réapprovisonnement",Toast.Length.SHORT);
                return true;
            }});
        btnmission.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        btnSpecial.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        btnAchat.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                final Dialog dialog = new Dialog("", skin);
                Label message = new Label("Voulez vous acheter ce personnage pour "+prix[choix]+" pieces ?",skin);
                TextButton btnOui = new TextButton("Oui", skin);
                TextButton btnNon = new TextButton("Annuler", skin);
                btnOui.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (prefs.getInteger("argent",50)>= prix[choix]){
                            prefs.putInteger("perso_possedes",prefs.getInteger("perso_possedes",10)*nbPrem[choix]).flush();
                            prefs.putInteger("argent",prefs.getInteger("argent",50) - prix[choix]).flush();
                            miniTable.getCells().get(1).setActor(new Label(String.valueOf(prefs.getInteger("argent",50)),skin));
                            tartine = usineDeBiscotteGrillees.create("transaction réussie", Toast.Length.SHORT);
                            magasin();
                        }else{
                            tartine = usineDeBiscotteGrillees.create("pas assez d'argent", Toast.Length.SHORT);
                        }
                        dialog.hide();
                        dialog.cancel();
                        dialog.remove();
                        return true;
                    }
                });
                btnNon.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        dialog.hide();
                        dialog.cancel();
                        dialog.remove();
                        return true;
                    }
                });
                dialog.add(message).pad(pad);
                dialog.add(btnOui);
                dialog.add(btnNon);
                dialog.show(etage);
                return true;
            }
        });
        moyenneTable.add(microTable).pad(10).row();
        moyenneTable.add(new Image(TextureRegion.split(notreHeros,200,280)[choix][7])).row();
        moyenneTable.add(btnAchat);
        miniTable.add(new Image(TextureRegion.split(new Texture("icons2.png"),32,32)[7][0]));
        miniTable.add(new Label(String.valueOf(prefs.getInteger("argent",50)),skin)).padLeft(2).padRight(10);
        miniTable.add(new Image(TextureRegion.split(new Texture("icons2.png"),32,32)[8][0]));
        microTable.add(new Image(TextureRegion.split(new Texture("icons2.png"),32,32)[7][0]));
        microTable.add(new Label(String.valueOf(prix[choix]),skin));

        petiteTable.add(btnRetour).growX();
        petiteTable.add(btnHeros).growX();
        petiteTable.add(btnTrainee).growX();
        petiteTable.add(btnSpecial).growX();
        petiteTable.add(btnmission).growX();
        petiteTable.add(miniTable).growX();
        table.setFillParent(true);
        table.setSkin(skin);
        table.add(petiteTable).fillX().colspan(2).row();
        table.add(moyenneTable).getActor().getCells().get(1).setActorWidth(1);
        table.add(defile1).width(camera.viewportWidth/2);
        table.pad(pad);
        table.debug();
        table.setPosition(0,0);
        usineDeBiscotteGrillees = new Toast.ToastFactory.Builder().font(new BitmapFont()).build();
        this.etage.addActor(table);

        magasin();
        Gdx.app.debug("debug","create"+table.getHeight()+" "+camera.viewportHeight+" "+hauteur+" "+etage.getHeight());
    }

    @java.lang.Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        etage.getBatch().setProjectionMatrix(etage.getCamera().combined);
        etage.getCamera().update();
        etage.act(delta);
        etage.draw();
        if (tartine!=null){
            tartine.render(delta);
        }
    }

    @java.lang.Override
    public void resize(int width, int height) {
        etage.getViewport().update(width, height, true);
        etage.getCamera().position.set(etage.getCamera().viewportWidth / 2, etage.getCamera().viewportHeight / 2, 0);
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
        etage.dispose();
        notreHeros.dispose();
    }

    public void magasin(){
        choix = 0;
        final Table tableDefillante1 = new Table();
        defile1 = new ScrollPane(tableDefillante1);
        defile1.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                float centerX = defile1.getVisualScrollX() + defile1.getWidth() / 2;
                for (Actor a : tableDefillante1.getChildren()) {
                    float distance = Math.abs(a.getX() - centerX);
                    float scale = 1 - (distance/tableDefillante1.getWidth());
                    a.setScale(scale);
                    Gdx.app.debug("magasin",a.toString()+ String.valueOf(scale));
                }
                return false;
            }
        });
        
        int mesPersos = prefs.getInteger("perso_possedes",10);
        for(int y = 0; y<notreHeros.getHeight()/tuileH; y++ ){
            if ((mesPersos%nbPrem[y]) != 0){
                if (choix == 0) choix = y;
                image = new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[y][7]);
                final int finalY = y;
                image.addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float zygrecque, int pointer, int button) {
                        choix = finalY;
                        moyenneTable.getCells().get(1).setActor(new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[finalY][7])).expandY();
                        microTable.getCells().get(1).setActor(new Label(String.valueOf(prix[choix]),skin));
                        table.invalidate();
                        prefs.flush();
                        return true;
                    }
                });
                tableDefillante1.add(image).fill();
            }
        }
        if(choix == 0) tableDefillante1.add(new Label("Rupture de stock :/",skin)).grow();
        tableDefillante1.row().height(pad);
        for(int y = 0; y<notreHeros.getHeight()/tuileH; y++ ) {
            if ((mesPersos%nbPrem[y]) != 0) {
                tableDefillante1.add(new Label(prix[y].toString(), skin));
            }
        }
        microTable.getCells().get(1).setActor(new Label(String.valueOf(prix[choix]),skin));
        table.getCells().get(2).setActor(defile1).expandX().padLeft(pad/2F);
        moyenneTable.getCells().get(1).setActor(new Image(TextureRegion.split(notreHeros,200,280)[choix][7])).grow();
    }
}



