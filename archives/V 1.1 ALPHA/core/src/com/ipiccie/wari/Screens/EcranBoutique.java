package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ipiccie.wari.ActiviteduJeu;
import com.ipiccie.wari.Toast;

public class EcranBoutique extends ScreenAdapter {
    ActiviteduJeu jeu;
    private Stage etage;
    private Texture notreHeros;
    private Preferences prefs;
    private Image image;
    private Integer[] prix = {10,20,30,40,50,50};
    Toast.ToastFactory usineDeBiscotteGrillées;
    Toast tartine;

    public EcranBoutique (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        etage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(etage);
        prefs = Gdx.app.getPreferences("Parametres");
        Skin skin = new Skin(Gdx.files.internal("skin.json"));
        float hauteur = Gdx.graphics.getHeight();
        float largeur = Gdx.graphics.getWidth();
        final int tuileH = 280;
        final int tuileL = 200;
        int pad = 20;
        notreHeros = new Texture("notre_heros.png");
        final Table tableDefillante1 = new Table();
        final Table tableDefillante2 = new Table();
        final ScrollPane defile1 = new ScrollPane(tableDefillante1);
        final Table table = new Table();
        final Table grosseTable = new Table();
        final Table petiteTable = new Table();
        TextButton btnRetour = new TextButton("<--",skin);
        TextButton btnHeros = new TextButton("HEROS",skin);
        TextButton btnTrainee = new TextButton("TRAINEE",skin);
        TextButton btnSpecial = new TextButton("OFFRES SPECIALES",skin);
        TextButton btnmission = new TextButton("QUESTES",skin);
        btnRetour.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                jeu.setScreen(new EcranMenu(jeu));
                return true;
            }});
        btnHeros.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillées.create("indisponible", Toast.Length.SHORT);
                return true;
            }});
        btnTrainee.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillées.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        btnmission.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillées.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        btnSpecial.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillées.create("indisponible",Toast.Length.SHORT);
                return true;
            }});
        for(int y = 0; y<notreHeros.getHeight()/tuileH; y++ ){
            image = new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[y][7]);
            final int finalY = y;
            image.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x, float zygrecque, int pointer, int button) {
                    table.getCells().get(1).setActor(new Image(TextureRegion.split(notreHeros,tuileL,tuileH)[finalY][7])).expandY();
                    prefs.putInteger("perso", finalY);
                    table.invalidate();
                    prefs.flush();
                    return true;
                }
            });
            tableDefillante1.add(image).fill().height(hauteur/4).width((hauteur/3)*(tuileL/(float)tuileH));
        }
        tableDefillante1.row().height(pad);
        for(int y = 0; y<notreHeros.getHeight()/tuileH; y++ ) {
            tableDefillante1.add(new Label(prix[y].toString(), skin));
        }


        petiteTable.add(btnRetour).expand().getActor().getLabel().setFontScale(1.5F);
        petiteTable.add(btnHeros).expand().getActor().getLabel().setFontScale(1.5F);
        petiteTable.add(btnTrainee).expand().getActor().getLabel().setFontScale(1.5F);
        petiteTable.add(btnSpecial).expand().getActor().getLabel().setFontScale(1.5F);
        petiteTable.add(btnmission).expand().getActor().getLabel().setFontScale(1.5F);
        table.setFillParent(true);
        table.setSkin(skin);
        table.add(petiteTable).expandX().colspan(2).height(hauteur/5F).row();
        table.add(new Image(TextureRegion.split(notreHeros,200,280)[prefs.getInteger("perso",0)][7])).expandX().expandY().fill().size((largeur-pad)/2.5F,(largeur-pad)/2.5F );
        table.add(defile1).expandX().padLeft(pad/2F);

        table.pad(pad);
        table.debug();
        //table.setWidth(largeur);
        usineDeBiscotteGrillées = new Toast.ToastFactory.Builder().font(new BitmapFont()).build();
        this.etage.addActor(table);
    }

    @java.lang.Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        etage.act(delta);
        etage.draw();
        if (tartine!=null){
            tartine.render(delta);
        }
    }

    @java.lang.Override
    public void resize(int width, int height) {
        etage.getViewport().update(width, height, true);
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
    }

    class MonActeur extends Actor {
        @java.lang.Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }
    }
}


