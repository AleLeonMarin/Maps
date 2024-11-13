package cr.ac.una.maps.util;

import javafx.animation.*;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class AnimationManager {

    private static AnimationManager instance;


    private AnimationManager() {
    }

    public static AnimationManager getInstance() {
        if (instance == null) {
            instance = new AnimationManager();
        }
        return instance;
    }

    public void applyFloatingAnimation(Node node) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(2));
        transition.setByY(-10);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();
    }

    /**
     TranslateTransition: Esta clase se utiliza para crear una animación de transición de traslación.

     setNode: Establece el nodo que se va a animar, en este caso, imgLogo.

     setDuration: Establece la duración de una repetición completa de la animación (2 segundos en este ejemplo).

     setByY: Establece la cantidad de traslación en el eje Y (en este caso, se moverá 20 píxeles hacia arriba).

     setAutoReverse: Hace que la animación se revierta automáticamente una vez que haya alcanzado el final de su duración.

     setCycleCount: Establece el número de veces que se repetirá la animación (INDEFINITE hace que la animación se repita indefinidamente).

     play: Inicia la animación.
     */




    public void applyFadeAnimation(Node node) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(node);
        fadeTransition.setDuration(Duration.seconds(1.0));
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.6);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.play();
    }

    public void setCustomCursor(Node node, String imagePath) {
        try {

            double width = 3;
            double height = 3;
            Image cursorImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageCursor customCursor = new ImageCursor(cursorImage, width, height);
            node.setCursor(customCursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void settleBack(Node node) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setToY(0);
    }

    public void applySlideInFromBottom(Node node) {
        node.setTranslateY(600);
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(4));
        transition.setFromY(600);
        transition.setToY(-10);
        transition.play();
    }
    public void applySlideInFromBottomSlow(Node node) {
        node.setTranslateY(600);
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(6));
        transition.setFromY(600);
        transition.setToY(-10);
        transition.play();
    }

    public void applySlideInFromLeftSideSlow(Node node) {
        node.setTranslateX(600);
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(6));
        transition.setFromX(-400);
        transition.setToX(0);
        transition.play();
    }

    public void playSound(String soundFilePath) {
        try {
            URL resource = getClass().getResource(soundFilePath);
            Media sound = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyRotationAnimation(Node node) {
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(node);
        rotateTransition.setDuration(Duration.seconds(2));
        rotateTransition.setFromAngle(-10);
        rotateTransition.setToAngle(10);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();
    }

    public void applyBreathingAnimation(Node node) {
        ScaleTransition scaleTransition = new ScaleTransition();
        scaleTransition.setNode(node);
        scaleTransition.setDuration(Duration.seconds(1));
        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToY(1.1);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }

    // animación para aclarar un StackPane
    public void fadeIn(Node node) {
        // Fade in transition
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);

        // Pausar transition for 8 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(8));

        // Fade out transicion
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);

        // Animacion sequencial el fadein -> el pause -> fadeout
        SequentialTransition sequentialTransition = new SequentialTransition(fadeIn, pause, fadeOut);
        sequentialTransition.play();
    }

    public void applyRandomRotation(Node node) {
        node.setRotate(0);
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(node);

        rotateTransition.setByAngle(150);
        rotateTransition.setDuration(Duration.seconds(1)); // Duracion de la rotación

        // Generar un angulo de rotación aleatorio entre 0 y 360 grados\
        int randomAngle = (int) (360 + Math.random() * 1120);

        rotateTransition.setByAngle(randomAngle);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);


            int finalAngle = randomAngle % 360; // Obtener el angulo final en el rango de 0 a 360



        if (finalAngle >= 126 && finalAngle < 177) {
            AppContext.getInstance().set("Criterio", "Ciencia");//corona
        } else if (finalAngle >= 74 && finalAngle < 126) {
            AppContext.getInstance().set("Criterio", "Geografia");
        } else if (finalAngle >= 24 && finalAngle < 74) {
            AppContext.getInstance().set("Criterio", "Ciencia");
        } else if (finalAngle >= 355 || finalAngle < 24) {
            AppContext.getInstance().set("Criterio", "Historia");
        } else if (finalAngle >= 280 && finalAngle < 333) {
            AppContext.getInstance().set("Criterio", "Deportes");
        } else if (finalAngle >= 230 && finalAngle < 280) {
            AppContext.getInstance().set("Criterio", "Arte");
        } else if (finalAngle >= 177 && finalAngle < 230) {
            AppContext.getInstance().set("Criterio", "Pop");
        } else if (finalAngle >= 333 && finalAngle < 355) {
            AppContext.getInstance().set("Criterio", "Historia");
        }


        rotateTransition.play();
    }

    public void moveToCoordinates(Node node, double x, double y, double durationSeconds) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setToX(x);
        transition.setToY(y);
        transition.setDuration(Duration.seconds(durationSeconds));
        transition.play();
    }

    public void animarFadeOut(Node node, Runnable onFinish) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(1500));
        fadeTransition.setNode(node);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(event -> onFinish.run());
        fadeTransition.play();
    }

    public void animateArrowFadeIn(ImageView arrowImageView, double durationSeconds) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(durationSeconds), arrowImageView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);
        fadeIn.play();
    }



    public void playBrickRemovalAnimation(ImageView imageView) {
        // Cargar las imágenes
        Image[] frames = new Image[10];
        System.out.println("frames.length = " + frames.length);
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Image(getClass().getResourceAsStream("/cr/ac/una/datos/resources/animation/" + i + ".png"));
        }

        // Crear el Timeline
        Timeline timeline = new Timeline();
        for (int i = 0; i < frames.length; i++) {
            final int index = i;
            System.out.println("index = " + index);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 100), event -> imageView.setImage(frames[index]))
            );
        }

        // Anadir un delay antes de iniciar la animación
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> timeline.play()); // Inicia la animacion despues del delay
        delay.play(); // Inicia el delay

        // Al terminar la animacion dejar la ultima imagen fija y eliminar el ImageView
        timeline.setOnFinished(event -> {
            imageView.setVisible(false); // Oculta el ImageView
        });
    }

    public void applyFloatingAnimation(Node node, Duration delay) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(2));
        transition.setFromY(0);
        transition.setToY(-8);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        if (delay != null) {
            PauseTransition pause = new PauseTransition(delay);
            pause.setOnFinished(event -> transition.play());
            pause.play();
        } else {
            transition.play();
        }
    }




}






