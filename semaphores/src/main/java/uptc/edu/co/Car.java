package uptc.edu.co;

import javafx.scene.image.ImageView;

public interface Car {
    void move();
    String getName();
    double getSpawnX();
    double getSpawnY();
    double getSpeed();
    double getCurrentX();
    double getCurrentY();
    double getRotate();
    ImageView getImageView();
    ImageView getSemaphoreView();

    void setSemaphoreView(ImageView semaphoreView);

}
