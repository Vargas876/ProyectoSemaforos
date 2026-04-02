package uptc.edu.co;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CarWest implements Car{
    private String name;
    private double spawnX;
    private double spawnY;
    private double speed;
    private double currentX;
    private double currentY;
    private double rotate;
    private ImageView imageView;
    private ImageView semaphoreView;
    
    public CarWest(Image imageView, ImageView semaphoreView) {
        this.name = "Car West";
        this.spawnX = 40; 
        this.spawnY = 350; 
        this.speed = 20;
        this.currentX = spawnX;
        this.currentY = spawnY;
        this.rotate = 180;
        this.imageView = new ImageView(imageView);
        this.semaphoreView = semaphoreView;
        this.imageView.setFitWidth(100);
        this.imageView.setFitHeight(50);
        this.imageView.setRotate(rotate);
        this.imageView.setLayoutX(spawnX);
        this.imageView.setLayoutY(spawnY);
        
    }
    
    @Override
    public void move() {
        imageView.setLayoutX(imageView.getLayoutX() + speed);
        currentX = imageView.getLayoutX();
    }
    
    // Getters y Setters
    @Override
    public String getName() { return name; }
    
    @Override
    public double getSpawnX() { return spawnX; }
    
    @Override
    public double getSpawnY() { return spawnY; }
    
    @Override
    public double getSpeed() { return speed; }
    
    @Override
    public double getCurrentX() { return currentX; }
    
    @Override
    public double getCurrentY() { return currentY; }

    @Override
    public double getRotate() { return rotate; }
    
    @Override
    public ImageView getImageView() { return imageView; }

    @Override
    public ImageView getSemaphoreView() { return semaphoreView; }
    
    public void setCurrentX(double x) { this.currentX = x; }
    
    public void setCurrentY(double y) { this.currentY = y; }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpawnX(double spawnX) {
        this.spawnX = spawnX;
    }

    public void setSpawnY(double spawnY) {
        this.spawnY = spawnY;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    public void setImageRoute(ImageView imageView) {
        this.imageView = imageView;
    }
    
    @Override
    public void setSemaphoreView(ImageView semaphoreView) {
        this.semaphoreView = semaphoreView;
    }
}
