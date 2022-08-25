package com.ipiccie.wari.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ipiccie.wari.ActiviteduJeu;
import com.ipiccie.wari.Toast;

public class EcranEquipement extends ScreenAdapter {
    ActiviteduJeu jeu;
    private Stage etage;
    private Image image;
    private Preferences prefs;
    private Texture notreHeros;
    Toast.ToastFactory usineDeBiscotteGrillées;
    Toast tartine;


    public EcranEquipement (ActiviteduJeu jeux){
        this.jeu = jeux;
    }

    @java.lang.Override
    public void show() {
        etage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(etage);
        prefs = Gdx.app.getPreferences("Parametres");
        Skin skin = new Skin(Gdx.files.internal("skin.json"));
        final float hauteur = Gdx.graphics.getHeight();
        final float largeur = Gdx.graphics.getWidth();
        final int tuileH = 280;
        final int tuileL = 200;
        final int pad = 20;
        notreHeros = new Texture("notre_heros.png");
        final Table tableDefillante1 = new Table();
        final ScrollPane defile1 = new ScrollPane(tableDefillante1);
        final Table table = new Table();
        final Table petiteTable = new Table();
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
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                tartine = usineDeBiscotteGrillées.create("vous y êtes déjà ;)",Toast.Length.SHORT);
                return true;
            }});
        bouton2.addListener(new ClickListener() {
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
            tableDefillante1.add(image).fill().height(hauteur/3).width((hauteur/3)*(tuileL/(float)tuileH));
        }
        table.setFillParent(true);
        table.setSkin(skin);
        petiteTable.add(retour).align(Align.left).expandX().getActor().getLabel().setFontScale(2);
        petiteTable.add(bouton1).align(Align.right).expandX().getActor().getLabel().setFontScale(2);
        petiteTable.add(bouton2).align(Align.left).expandX().getActor().getLabel().setFontScale(2);
        table.add(petiteTable).expandX().colspan(2).height(hauteur/5F).row();
        table.add(new Image(TextureRegion.split(notreHeros,200,280)[prefs.getInteger("perso",0)][7])).expandX().expandY().fill().size((largeur-pad)/2.5F,(largeur-pad)/2.5F );
        table.add(defile1).expandX().padLeft(pad/2F);

        table.pad(pad);
        table.debug();
        table.setWidth(largeur);
        table.setBackground(new TextureRegionDrawable(new Texture("bois.png")));
        this.etage.addActor(table);
        usineDeBiscotteGrillées = new Toast.ToastFactory.Builder().font(new BitmapFont()).build();
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

    class MonActeur extends Actor{
        @java.lang.Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }
    }
}
