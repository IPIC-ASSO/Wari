package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ipiccie.wari.ActiviteduJeu;
import com.ipiccie.wari.Toast;

public class EcranEquipement extends ScreenAdapter {
    ActiviteduJeu jeu;
    private Stage etage;
    private OrthographicCamera camera;
    private Viewport porteVue;  //zone de la vue
    private Image image;
    private Image image3;
    private Preferences prefs;
    private Texture notreHeros;
    private Texture etatEquipement;
    private Integer[] nbPrem = {1,2,3,5,7,11,13,17,19,23,29};
    Toast.ToastFactory usineDeBiscotteGrillees;     //affiche des toasts !
    Toast tartine;


    public EcranEquipement (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        final float hauteur = Gdx.graphics.getHeight();
        final float largeur = Gdx.graphics.getWidth();
        Skin skin = new Skin(Gdx.files.internal("skin.json"));
        final int tuileH = 280;
        final int tuileL = 200;
        final int pad = 20;
        prefs = Gdx.app.getPreferences("Parametres");

        camera = new OrthographicCamera(800,800*(hauteur/largeur));
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        porteVue = new StretchViewport(camera.viewportWidth, camera.viewportHeight, camera);
        etage = new Stage(porteVue);
        etage.getViewport().apply();
        Gdx.input.setInputProcessor(etage);

        notreHeros = new Texture("notre_heros.png");
        etatEquipement = new Texture("icons_equipement.png");
        final Table tableDefillante1 = new Table();
        final ScrollPane defile1 = new ScrollPane(tableDefillante1);
        final Table table = new Table();
        final Table petiteTable = new Table();
        final Table miniTable = new Table();
        TextButton bouton1 = new TextButton("HEROS",skin);
        TextButton bouton2 = new TextButton("TRAINEE",skin);
        TextButton retour = new TextButton("<--",skin);
        retour.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                jeu.setScreen(new EcranMenu(jeu));
                return true;
            }});
        bouton1.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("vous y êtes dejà ;)",Toast.Length.SHORT);
                return true;
            }});
        bouton2.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillees.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        int mesPersos = prefs.getInteger("perso_possedes",10);
        Image image2;
        image3 = new Image(TextureRegion.split(etatEquipement,tuileL,tuileH)[1][0]);
        for(int y = 0; y < notreHeros.getHeight()/tuileH; y++ ){
            image = new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[y][7]);
            final int finalY = y;
            final Image imageF = image;
            Group group = new Group();
            group.debug();
            image.setSize((camera.viewportHeight/2F)*(tuileL/(float)tuileH),camera.viewportHeight/2F);
            group.addActor(image);
            if ((mesPersos%nbPrem[y]) != 0){
                image2 = new Image(TextureRegion.split(etatEquipement,tuileL,tuileH)[0][0]);
                group.addActor(image2);
            } else  if (prefs.getInteger("perso",0) == y){
                group.addActor(image3);
            }
            tableDefillante1.add(group).size((camera.viewportHeight/2F)*(tuileL/(float)tuileH),camera.viewportHeight/2F);
            image.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float zygrecque, int pointer, int button) {
                    table.getCells().get(1).setActor(new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[finalY][7])).expandY();
                    prefs.putInteger("perso", finalY);
                    table.invalidate();
                    prefs.flush();
                    imageF.getParent().addActor(image3);
                    return true;
                }
            });
        }
        Label or = new Label(String.valueOf(prefs.getInteger("argent",50)),skin);
        miniTable.add(new Image(TextureRegion.split(new Texture("icons.png"),32,32)[7][0]));
        miniTable.add(or).grow().padLeft(2).padRight(10);
        miniTable.add(new Image(TextureRegion.split(new Texture("icons.png"),32,32)[8][0]));

        table.setFillParent(true);
        table.setSkin(skin);
        petiteTable.add(retour).align(Align.left).growX();
        petiteTable.add(bouton1).align(Align.center).growX();
        petiteTable.add(bouton2).align(Align.left).growX();
        petiteTable.add(miniTable).align(Align.right);
        table.add(petiteTable).growX().colspan(2).row();
        table.add(new Image(TextureRegion.split(notreHeros,200,280)[prefs.getInteger("perso",0)][7])).size((camera.viewportHeight/1.4F)*(tuileL/(float)tuileH),camera.viewportHeight/1.4F).grow();
        table.add(defile1).padLeft(pad/2F);

        table.pad(pad);
        table.debug();
        table.setWidth(largeur);
        table.setBackground(new TextureRegionDrawable(new Texture("bois.png")));
        this.etage.addActor(table);
        usineDeBiscotteGrillees = new Toast.ToastFactory.Builder().font(new BitmapFont()).build();
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
        etatEquipement.dispose();
        notreHeros.dispose();
    }
}
